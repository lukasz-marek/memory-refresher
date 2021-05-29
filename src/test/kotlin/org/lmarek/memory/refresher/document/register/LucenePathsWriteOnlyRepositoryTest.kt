package org.lmarek.memory.refresher.document.register

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
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
            tested.register(document)

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
                tested.register(document)
                for (i in 1..10) {
                    tested.register(document.copy(content = i.toString()))
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
                    tested.register(Document(DocumentPath("/path/to/document$i"), "identical content"))
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
            tested.unregister(path) // should not throw
        }

        @Test
        fun `unregister should be idempotent when file is not registered`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val path = DocumentPath("/does/not/exist")

            // when / then
            tested.unregister(path) // should not throw
            tested.unregister(path) // should not throw
        }

        @Test
        fun `unregister should remove file from index`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LucenePathsWriteOnlyRepository(indexWriter)
            val document = Document(DocumentPath("/path/to/file"), "this is content of a document")

            // when
            tested.register(document)
            tested.unregister(document.path)

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
                tested.register(Document(path, "identical content"))
            }
            val toRemove = Channel<DocumentPath>(Channel.UNLIMITED)
            for (documentPath in documentPaths.take(21)) {
                toRemove.send(documentPath)
            }
            toRemove.close()

            // when
            tested.unregister(toRemove)

            // then
            val indexReader = createIndexReader(tempDir)
            expectThat(indexReader.numDocs()).isEqualTo(79)
        }
    }
}