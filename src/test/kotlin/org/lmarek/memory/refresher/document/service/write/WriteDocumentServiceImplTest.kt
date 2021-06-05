package org.lmarek.memory.refresher.document.service.write

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.loader.DocumentLoader
import strikt.api.expectThrows
import java.io.File

class WriteDocumentServiceImplTest {
    private val loader = mockk<DocumentLoader>()
    private val repository = mockk<PathsWriteOnlyRepository>()
    private val tested = WriteDocumentServiceImpl(repository, loader)

    @Nested
    inner class TestWriteOne {

        @Test
        fun `should throw when document loader throws`() = runBlocking<Unit> {
            // given
            coEvery { loader.load(any()) } throws DocumentLoader.DocumentLoaderException(Exception())

            // when // then
            expectThrows<WriteDocumentService.WriteDocumentServiceException> {
                tested.write(DocumentPath("/some/path"))
            }
        }

        @Test
        fun `should write when document loader returns file content`() = runBlocking<Unit> {
            // given
            coEvery { loader.load(any()) } returns Document(DocumentPath("/some/path"), "some content")
            coEvery { repository.register(ofType<Document>()) } returns Unit

            // when
            tested.write(DocumentPath("/some/path"))

            // then
            coVerify { loader.load(File("/some/path")) }
            coVerify { repository.register(Document(DocumentPath("/some/path"), "some content")) }
        }
    }
}