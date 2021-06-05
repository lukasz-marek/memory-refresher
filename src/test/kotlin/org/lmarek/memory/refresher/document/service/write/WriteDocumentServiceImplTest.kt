package org.lmarek.memory.refresher.document.service.write

import io.mockk.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
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
    private val repository = spyk(object : PathsWriteOnlyRepository {
        override suspend fun register(document: Document) {}

        override suspend fun register(documents: Flow<Document>) {
            documents.collect()
        }

        override suspend fun unregister(path: DocumentPath) {}

        override suspend fun unregister(paths: Flow<DocumentPath>) {
            paths.collect()
        }

    })
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
            coVerify { loader.load(File("/some/path")) }
        }

        @Test
        fun `should write when document loader returns file content`() = runBlocking<Unit> {
            // given
            coEvery { loader.load(any()) } returns Document(DocumentPath("/some/path"), "some content")

            // when
            tested.write(DocumentPath("/some/path"))

            // then
            coVerify { loader.load(File("/some/path")) }
            coVerify { repository.register(Document(DocumentPath("/some/path"), "some content")) }
        }
    }

    @Nested
    inner class TestWriteMany {

        @Test
        fun `should throw when document loader throws and onMissing is set to THROW`() = runBlocking<Unit> {
            // given
            coEvery { loader.load(File("/some/path1")) } returns Document(DocumentPath("/some/path1"), "some content 1")
            coEvery { loader.load(File("/some/path2")) } throws DocumentLoader.DocumentLoaderException(Exception())
            val documents = flowOf(DocumentPath("/some/path1"), DocumentPath("/some/path2"))

            // when // then
            expectThrows<WriteDocumentService.WriteDocumentServiceException> {
                tested.write(documents, OnMissing.THROW)
            }
            coVerifyAll {
                loader.load(File("/some/path1"))
                loader.load(File("/some/path2"))
            }
        }

        @Test
        fun `should remove document when document loader throws and onMissing is set to REMOVE`() = runBlocking<Unit> {
            // given
            coEvery { loader.load(File("/some/path1")) } returns Document(DocumentPath("/some/path1"), "some content 1")
            coEvery { loader.load(File("/some/path2")) } throws DocumentLoader.DocumentLoaderException(Exception())
            val documents = flowOf(DocumentPath("/some/path1"), DocumentPath("/some/path2"))

            // when
            tested.write(documents, OnMissing.REMOVE)

            // then
            coVerifyAll {
                loader.load(File("/some/path1"))
                loader.load(File("/some/path2"))
                repository.unregister(ofType<Flow<DocumentPath>>())
                repository.register(ofType<Flow<Document>>())
            }
        }
    }
}