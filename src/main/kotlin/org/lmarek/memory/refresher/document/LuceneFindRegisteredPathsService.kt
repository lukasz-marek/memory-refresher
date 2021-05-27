package org.lmarek.memory.refresher.document

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import kotlin.math.min

private const val PATH_FIELD = "path"
private const val CONTENT_FIELD = "content"

class LuceneFindRegisteredPathsService(
    analyzer: Analyzer,
    private val indexSearcher: IndexSearcher
) : FindRegisteredPathsService {
    private val queryParser = QueryParser(CONTENT_FIELD, analyzer)
    private val resultsPerPage = 5

    override fun findMatching(query: DocumentQuery): List<RegisteredPath> {
        val luceneQuery = queryParser.parse(query.query)
        val firstPage = indexSearcher.search(luceneQuery, min(resultsPerPage, query.maxResults))
        val results = mutableListOf<ScoreDoc>()
        results.addAll(firstPage.scoreDocs)
        if (firstPage.totalHits.value > firstPage.scoreDocs.size && firstPage.scoreDocs.size < query.maxResults) {
            var remaining = query.maxResults - firstPage.scoreDocs.size
            var previousPage = firstPage.scoreDocs
            while (remaining > 0 && previousPage.isNotEmpty()) {
                val limit = min(resultsPerPage, remaining)
                val nextPage = fetchMore(previousPage.last(), luceneQuery, limit)
                results.addAll(nextPage)
                remaining -= nextPage.size
                previousPage = nextPage
            }
        }
        return results.map { fetchPath(it) }
    }


    private fun fetchMore(last: ScoreDoc, query: Query, limit: Int): Array<ScoreDoc> {
        val page = indexSearcher.searchAfter(last, query, limit)
        return page.scoreDocs
    }

    private fun fetchPath(scoreDoc: ScoreDoc): RegisteredPath {
        val document = indexSearcher.doc(scoreDoc.doc)
        val path = document[PATH_FIELD]
        val id = scoreDoc.doc
        return RegisteredPath(id, path)
    }
}