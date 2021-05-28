package org.lmarek.memory.refresher.document

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import test.utils.createIndexReader
import test.utils.createIndexWriter
import java.nio.file.Path

class LuceneRegisterDocumentServiceTest {

    @Nested
    inner class TestSave {

        @Test
        fun `save 1 document should be successful`(@TempDir tempDir: Path) = runBlocking<Unit> {
            // given
            val indexWriter = createIndexWriter(tempDir)
            val tested = LuceneRegisterDocumentService(indexWriter)
            val document = Document("/path/to/file", "this is content of a document")

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
                val tested = LuceneRegisterDocumentService(indexWriter)
                val document = Document("/path/to/file", "this is content of a document")

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
                val tested = LuceneRegisterDocumentService(indexWriter)

                // when
                for (i in 1..10) {
                    tested.register(Document("/path/to/document$i", "identical content"))
                }

                // then
                val indexReader = createIndexReader(tempDir)
                expectThat(indexReader.numDocs()).isEqualTo(10)
            }
    }
}