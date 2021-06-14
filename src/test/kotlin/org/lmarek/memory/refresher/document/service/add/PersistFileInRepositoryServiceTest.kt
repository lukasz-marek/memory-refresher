package org.lmarek.memory.refresher.document.service.add

import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.add.loader.DocumentLoader
import strikt.api.expectThrows

class PersistFileInRepositoryServiceTest {
    private val loader = mockk<DocumentLoader>()
    private val repository = mockk<PathsWriteOnlyRepository>()

    private val tested = PersistFileInRepositoryService(loader, repository)

    @Test
    fun `should throw when loader throws`() = runBlocking<Unit> {
        // given
        coEvery { loader.load(any()) } throws DocumentLoader.DocumentLoaderException(Exception())

        // when / then
        expectThrows<PersistFileService.PersistFileServiceException> { tested.persist(mockk()) }
    }

    @Test
    fun `should persist when loader is successful`() = runBlocking<Unit> {
        // given
        coEvery { loader.load(any()) } returns Document(DocumentPath("/some/path"), content = "some content")
        coEvery { repository.save(ofType<Document>()) } returns Unit

        // when
        tested.persist(mockk())

        // then
        coVerifyOrder {
            loader.load(any())
            repository.save(ofType<Document>())
        }
    }
}