package org.lmarek.memory.refresher.document

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import kotlin.math.min

private const val PATH_FIELD = "path"
private const val CONTENT_FIELD = "content"

class LuceneFindRegisteredPathsService(
    analyzer: Analyzer,
    private val indexSearcherProvider: () -> IndexSearcher
) : FindRegisteredPathsService {
    private val queryParser = QueryParser(CONTENT_FIELD, analyzer)
    private val resultsPerPage = 5

    override suspend fun findMatching(query: DocumentQuery): ReceiveChannel<RegisteredPath> {
        val luceneQuery = queryParser.parse(query.query)
        return find(luceneQuery, query.maxResults)
    }

    override suspend fun listAll(): ReceiveChannel<RegisteredPath> {
        return find(MatchAllDocsQuery(), Int.MAX_VALUE)
    }

    private suspend fun find(query: Query, limit: Int): ReceiveChannel<RegisteredPath> {
        val results = Channel<RegisteredPath>(capacity = Channel.UNLIMITED)
        withContext(Dispatchers.IO) { // IO to make sure there is always a thread available in case of blocking calls
            val indexSearcher =
                indexSearcherProvider() // fresh instance for each search to make sure we get fresh results
            val firstPageDeferred = async {
                indexSearcher.search(query, min(resultsPerPage, limit))
            }

            val searchResults = Channel<ScoreDoc>(capacity = resultsPerPage)
            launch {
                val firstPage = firstPageDeferred.await()
                firstPage.scoreDocs.forEach { searchResults.send(it) }
                if (shouldFetchMultiplePages(firstPage, limit)) {
                    val remaining = limit - firstPage.scoreDocs.size
                    val previousPage = firstPage.scoreDocs
                    fetchRemainingDocs(query, remaining, previousPage, searchResults, indexSearcher)
                }
                searchResults.close()
            }

            launch {
                searchResults.consumeEach { results.send(indexSearcher.fetchPath(it)) }
                results.close()
            }
        }
        return results
    }

    private tailrec suspend fun fetchRemainingDocs(
        query: Query,
        remaining: Int,
        previousPage: Array<out ScoreDoc>,
        searchResults: SendChannel<ScoreDoc>,
        indexSearcher: IndexSearcher
    ) {
        if (remaining <= 0 || previousPage.isEmpty())
            return

        val limit = min(resultsPerPage, remaining)
        val currentPage = indexSearcher.fetchMore(previousPage.last(), query, limit)
        for (scoreDoc in currentPage)
            searchResults.send(scoreDoc)

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

    private fun IndexSearcher.fetchPath(scoreDoc: ScoreDoc): RegisteredPath {
        val document = doc(scoreDoc.doc)
        val path = document[PATH_FIELD]
        val id = scoreDoc.doc
        return RegisteredPath(id, path)
    }
}