package org.lmarek.memory.refresher.document.find

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import org.lmarek.memory.refresher.document.DocumentPath
import kotlin.math.min

private const val PATH_FIELD = "path"
private const val CONTENT_FIELD = "content"

class LucenePathsReadOnlyRepository(
    analyzer: Analyzer,
    private val indexSearcherProvider: () -> IndexSearcher
) : PathsReadOnlyRepository {
    private val queryParser = QueryParser(CONTENT_FIELD, analyzer)
    private val resultsPerPage = 5

    override suspend fun findMatching(query: DocumentQuery): Flow<DocumentPath> {
        val luceneQuery = queryParser.parse(query.query)
        return find(luceneQuery, query.maxResults)
    }

    override suspend fun listAll(): Flow<DocumentPath> {
        return find(MatchAllDocsQuery(), Int.MAX_VALUE)
    }

    private suspend fun find(query: Query, limit: Int): Flow<DocumentPath> {
        return withContext(Dispatchers.IO) { // IO to make sure there is always a thread available in case of blocking calls
            val indexSearcher =
                indexSearcherProvider() // fresh instance for each search to make sure we get fresh results

            flow {
                val firstPage = indexSearcher.search(query, min(resultsPerPage, limit))
                firstPage.scoreDocs.forEach { emit(indexSearcher.fetchPath(it)) }
                if (shouldFetchMultiplePages(firstPage, limit)) {
                    val remaining = limit - firstPage.scoreDocs.size
                    val previousPage = firstPage.scoreDocs
                    fetchRemainingDocs(query, remaining, previousPage, this, indexSearcher)
                }
            }
        }
    }

    private tailrec suspend fun fetchRemainingDocs(
        query: Query,
        remaining: Int,
        previousPage: Array<out ScoreDoc>,
        searchResults: FlowCollector<DocumentPath>,
        indexSearcher: IndexSearcher
    ) {
        if (remaining <= 0 || previousPage.isEmpty())
            return

        val limit = min(resultsPerPage, remaining)
        val currentPage = indexSearcher.fetchMore(previousPage.last(), query, limit)
        currentPage.onEach { searchResults.emit(indexSearcher.fetchPath(it)) }

        fetchRemainingDocs(
            query = query,
            remaining = remaining - currentPage.size,
            previousPage = currentPage,
            searchResults = searchResults,
            indexSearcher = indexSearcher
        )
    }

    private fun shouldFetchMultiplePages(firstPage: TopDocs, limit: Int): Boolean {
        val moreResultsExists = firstPage.totalHits.value > firstPage.scoreDocs.size
        val queryRequestsMoreThanReturned = firstPage.scoreDocs.size < limit
        return moreResultsExists && queryRequestsMoreThanReturned
    }


    private fun IndexSearcher.fetchMore(last: ScoreDoc, query: Query, limit: Int): Array<ScoreDoc> {
        val page = searchAfter(last, query, limit)
        return page.scoreDocs
    }

    private fun IndexSearcher.fetchPath(scoreDoc: ScoreDoc): DocumentPath {
        val document = doc(scoreDoc.doc)
        val path = document[PATH_FIELD]
        return DocumentPath(path)
    }
}