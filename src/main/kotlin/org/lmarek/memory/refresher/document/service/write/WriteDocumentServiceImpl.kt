package org.lmarek.memory.refresher.document.service.write

import kotlinx.coroutines.flow.Flow
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.loader.DocumentLoader
import java.io.File

class WriteDocumentServiceImpl(
    private val pathsWriteOnlyRepository: PathsWriteOnlyRepository,
    private val documentLoader: DocumentLoader
) : WriteDocumentService {
    override suspend fun write(path: DocumentPath) {
        val document = loadFile(path)
        pathsWriteOnlyRepository.register(document)
    }

    override suspend fun write(paths: Flow<DocumentPath>, onMissing: OnMissing) {
        TODO("Not yet implemented")
    }

    private suspend fun loadFile(path: DocumentPath): Document =
        try {
            documentLoader.load(File(path.value))
        } catch (loaderException: DocumentLoader.DocumentLoaderException) {
            throw WriteDocumentService.WriteDocumentServiceException(loaderException)
        }
}