package org.lmarek.memory.refresher.document.find

import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.IndexSearcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.register.LucenePathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.register.PathsWriteOnlyRepository
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import test.utils.createAnalyzer
import test.utils.createIndexReader
import test.utils.createIndexWriter
import java.nio.file.Path

class LucenePathsReadOnlyRepositoryTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var indexWriter: IndexWriter

    private lateinit var pathsWriteOnlyRepository: PathsWriteOnlyRepository

    @BeforeEach
    fun setup() {
        indexWriter = createIndexWriter(tempDir)
        pathsWriteOnlyRepository = LucenePathsWriteOnlyRepository(indexWriter)
    }

    @Nested
    inner class TestListAll {

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 21, 27, 33, 37, 49, 91, 99, 150, 500, 900])
        fun `should return all documents`(count: Int) = runBlocking<Unit> {
            // given
            for (i in 1..count) {
                pathsWriteOnlyRepository.register(Document(DocumentPath("/document_$i"), "match"))
            }
            val tested =
                LucenePathsReadOnlyRepository(createAnalyzer()) { IndexSearcher(createIndexReader(tempDir)) }

            // when
            val results = tested.listAll().toList()

            // then
            expectThat(results).hasSize(count)
                .and { get { toSet() }.hasSize(count) }
        }
    }

    @Nested
    inner class TestSearch {

        @ParameterizedTest
        @ValueSource(strings = ["name", "empty"])
        fun `should find a single matching document`(queryValue: String) = runBlocking<Unit> {
            // given
            pathsWriteOnlyRepository.register(
                Document(
                    path = DocumentPath("/document/with/name"),
                    content = "I have name inside"
                )
            )
            pathsWriteOnlyRepository.register(
                Document(
                    path = DocumentPath("/document/with/empty"),
                    content = "I'm empty"
                )
            )

            val tested =
                LucenePathsReadOnlyRepository(createAnalyzer()) { IndexSearcher(createIndexReader(tempDir)) }
            val query = DocumentQuery(queryValue, 1)

            // when
            val results = tested.findMatching(query).toList()

            // then
            expectThat(results).hasSize(1).and {
                get { first() }.get { value }.isEqualTo("/document/with/$queryValue")
            }
        }

        @Test
        fun `should return empty list for unmatched document`() = runBlocking<Unit> {
            // given
            pathsWriteOnlyRepository.register(Document(DocumentPath("/unmatched"), ""))
            val tested =
                LucenePathsReadOnlyRepository(createAnalyzer()) { IndexSearcher(createIndexReader(tempDir)) }
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
                pathsWriteOnlyRepository.register(Document(DocumentPath("/document_$i"), "match"))
            }
            val tested =
                LucenePathsReadOnlyRepository(createAnalyzer()) { IndexSearcher(createIndexReader(tempDir)) }
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
                pathsWriteOnlyRepository.register(Document(DocumentPath("/document_$i"), "match"))
            }
            val tested =
                LucenePathsReadOnlyRepository(createAnalyzer()) { IndexSearcher(createIndexReader(tempDir)) }
            val query = DocumentQuery("match", 100)

            // when
            val results = tested.findMatching(query).toList()

            // then
            expectThat(results).hasSize(5)
                .and { get { toSet() }.hasSize(5) }
        }
    }

}