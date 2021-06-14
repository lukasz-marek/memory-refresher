package org.lmarek.memory.refresher.document.service.add

import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.add.loader.DocumentLoader
import java.io.File

class PersistFileInRepositoryService(
    private val documentLoader: DocumentLoader,
    private val writeOnlyRepository: PathsWriteOnlyRepository
) : PersistFileService {
    override suspend fun persist(file: File) {
        try {
            val newDocument = documentLoader.load(file)
            writeOnlyRepository.save(newDocument)
        } catch (loaderException: PersistFileService.PersistFileServiceException) {
            throw PersistFileService.PersistFileServiceException("Failed to persist file", loaderException)
        }
    }
}