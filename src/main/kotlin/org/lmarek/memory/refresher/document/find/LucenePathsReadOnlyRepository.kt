package org.lmarek.memory.refresher.document.find

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
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
    private val indexSearcherProvider: () -> IndexSearcher?
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
        // IO to make sure there is always a thread available in case of blocking calls
        return withContext(Dispatchers.IO) {
            // fresh instance for each search to make sure we get fresh results
            val indexSearcher = indexSearcherProvider() ?: return@withContext emptyFlow()

            flow {
                val firstPage = indexSearcher.search(query, min(resultsPerPage, limit))
                firstPage.scoreDocs.forEach { emit(indexSearcher.fetchPath(it)) }
                if (shouldFetchMultiplePages(firstPage, limit)) {
                    val remaining = limit - firstPage.scoreDocs.size
                    val previousPage = firstPage.scoreDocs
                    indexSearcher.fetchRemainingDocs(query, remaining, previousPage, this)
                }
            }
        }
    }

    private tailrec suspend fun IndexSearcher.fetchRemainingDocs(
        query: Query,
        remaining: Int,
        previousPage: Array<out ScoreDoc>,
        searchResults: FlowCollector<DocumentPath>,
    ) {
        if (remaining <= 0 || previousPage.isEmpty())
            return

        val limit = min(resultsPerPage, remaining)
        val currentPage = fetchMore(previousPage.last(), query, limit)
        currentPage.onEach { searchResults.emit(fetchPath(it)) }

        fetchRemainingDocs(
            query = query,
            remaining = remaining - currentPage.size,
            previousPage = currentPage,
            searchResults = searchResults
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