package org.lmarek.memory.refresher.document

import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.IndexSearcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import test.utils.createAnalyzer
import test.utils.createIndexReader
import test.utils.createIndexWriter
import java.nio.file.Path

class LuceneFindRegisteredPathsServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var indexWriter: IndexWriter

    private lateinit var registerDocumentService: RegisterDocumentService

    @BeforeEach
    fun setup() {
        indexWriter = createIndexWriter(tempDir)
        registerDocumentService = LuceneRegisterDocumentService(indexWriter)
    }

    @ParameterizedTest
    @ValueSource(strings = ["name", "empty"])
    fun `should find a single matching document`(queryValue: String) = runBlocking<Unit> {
        // given
        registerDocumentService.register(Document(path = "/document/with/name", content = "I have name inside"))
        registerDocumentService.register(Document(path = "/document/with/empty", content = "I'm empty"))

        val indexSearcher = IndexSearcher(createIndexReader(tempDir))
        val tested = LuceneFindRegisteredPathsService(createAnalyzer(), indexSearcher)
        val query = DocumentQuery(queryValue, 1)

        // when
        val results = tested.findMatching(query).toList()

        // then
        expectThat(results).hasSize(1).and {
            get { first() }.get { path }.isEqualTo("/document/with/$queryValue")
        }
    }

    @Test
    fun `should return empty list for unmatched document`() = runBlocking<Unit> {
        // given
        registerDocumentService.register(Document("/unmatched", ""))
        val indexSearcher = IndexSearcher(createIndexReader(tempDir))
        val tested = LuceneFindRegisteredPathsService(createAnalyzer(), indexSearcher)
        val query = DocumentQuery("nothing", 1)

        // when
        val results = tested.findMatching(query).toList()

        // then
        expectThat(results).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 21, 27, 33, 37, 49, 91, 99])
    fun `should return some out of 100 existing unique documents`(count: Int) = runBlocking<Unit> {
        // given
        for (i in 1..100) {
            registerDocumentService.register(Document("/document_$i", "match"))
        }
        val indexSearcher = IndexSearcher(createIndexReader(tempDir))
        val tested = LuceneFindRegisteredPathsService(createAnalyzer(), indexSearcher)
        val query = DocumentQuery("match", count)

        // when
        val results = tested.findMatching(query).toList()

        // then
        expectThat(results).hasSize(count)
            .and { get { toSet() }.hasSize(count) }
    }

    @Test
    fun `shouldn't fail when there are fewer documents than requested`() = runBlocking<Unit> {
        // given
        for (i in 1..5) {
            registerDocumentService.register(Document("/document_$i", "match"))
        }
        val indexSearcher = IndexSearcher(createIndexReader(tempDir))
        val tested = LuceneFindRegisteredPathsService(createAnalyzer(), indexSearcher)
        val query = DocumentQuery("match", 100)

        // when
        val results = tested.findMatching(query).toList()

        // then
        expectThat(results).hasSize(5)
            .and { get { toSet() }.hasSize(5) }
    }
}