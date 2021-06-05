package org.lmarek.memory.refresher.document.repository.write

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import test.utils.createIndexReader
import test.utils.createIndexWriter
import java.nio.file.Path

class LucenePathsWriteOnlyRepositoryTest {

    @Nested
    inner class TestSave {

        @Test
        fun `save 1 document should be successful`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val document = Document(DocumentPath("/path/to/file"), "this is content of a document")

            // when
            tested.save(document)

            // then
            val indexReader = createIndexReader(tempDir)
            expectThat(indexReader.numDocs()).isEqualTo(1)
        }

        @Test
        fun `save document multiple times with the same path should overwrite it`(@TempDir tempDir: Path) =
            runBlocking<Unit> {
                // given
                val indexWriter = createIndexWriter(tempDir)
                val tested = LucenePathsWriteOnlyRepository(indexWriter)
                val document = Document(DocumentPath("/path/to/file"), "this is content of a document")

                // when
                tested.save(document)
                for (i in 1..10) {
                    tested.save(document.copy(content = i.toString()))
                }

                // then
                val indexReader = createIndexReader(tempDir)
                expectThat(indexReader.numDocs()).isEqualTo(1)
            }

        @Test
        fun `save documents with different paths should create new documents`(@TempDir tempDir: Path) =
            runBlocking<Unit> {
                // given
                val indexWriter = createIndexWriter(tempDir)
                val tested = LucenePathsWriteOnlyRepository(indexWriter)

                // when
                for (i in 1..10) {
                    tested.save(Document(DocumentPath("/path/to/document$i"), "identical content"))
                }

                // then
                val indexReader = createIndexReader(tempDir)
                expectThat(indexReader.numDocs()).isEqualTo(10)
            }
    }

    @Nested
    inner class TestRemove {

        @Test
        fun `unregister should be successful if file is not registered`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val path = DocumentPath("/does/not/exist")

            // when / then
            tested.delete(path) // should not throw
        }

        @Test
        fun `unregister should be idempotent when file is not registered`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val path = DocumentPath("/does/not/exist")

            // when / then
            tested.delete(path) // should not throw
            tested.delete(path) // should not throw
        }

        @Test
        fun `unregister should remove file from index`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val document = Document(DocumentPath("/path/to/file"), "this is content of a document")

            // when
            tested.save(document)
            tested.delete(document.path)

            // then
            val indexReader = createIndexReader(tempDir)
            expectThat(indexReader.numDocs()).isEqualTo(0)
        }
    }

    @Nested
    inner class TestRemoveMany {

        @Test
        fun `should remove all passed documents`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val documentPaths = (1..100).map { DocumentPath("/path/to/document$it") }
            for (path in documentPaths) {
                tested.save(Document(path, "identical content"))
            }
            val toRemove = flow {
                for (documentPath in documentPaths.take(21)) {
                    emit(documentPath)
                }
            }

            // when
            tested.delete(toRemove)

            // then
            val indexReader = createIndexReader(tempDir)
            expectThat(indexReader.numDocs()).isEqualTo(79)
        }
    }

    @Nested
    inner class TestSaveMany {

        @TempDir
        lateinit var tempDir: Path

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 10, 15, 150])
        fun `save x documents should be successful`(count: Int) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val toRegister = flow {
                for (i in 1..count) {
                    emit(Document(DocumentPath("/path/to/document$i"), "identical content"))
                }
            }

            // when
            tested.save(toRegister)

            // then
            val indexReader = createIndexReader(tempDir)
            expectThat(indexReader.numDocs()).isEqualTo(count)
        }
    }
}