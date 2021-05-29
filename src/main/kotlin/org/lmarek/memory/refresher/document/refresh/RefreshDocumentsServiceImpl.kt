package org.lmarek.memory.refresher.document.refresh

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentLoader
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.find.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.register.PathsWriteOnlyRepository
import java.io.File

class RefreshDocumentsServiceImpl(
    private val readOnlyRepository: PathsReadOnlyRepository,
    private val writeOnlyRepository: PathsWriteOnlyRepository,
    private val documentLoader: DocumentLoader
) : RefreshDocumentsService {

    override suspend fun refreshAll(): Flow<RefreshResult> {
        val allDocuments = readOnlyRepository.listAll()
        return flow {
            allDocuments.collect { path ->
                emit(refresh(path))
            }
        }
    }

    private suspend fun refresh(path: DocumentPath): RefreshResult {
        val refreshedDocument = loadDocument(path)
        val refreshType = if (refreshedDocument != null) {
            writeOnlyRepository.register(refreshedDocument)
            RefreshType.RELOAD
        } else {
            writeOnlyRepository.unregister(path)
            RefreshType.DELETE
        }
        return RefreshResult(path, refreshType)
    }

    private suspend fun loadDocument(path: DocumentPath): Document? {
        return withContext(Dispatchers.IO) {
            try {
                documentLoader.load(File(path.value))
            } catch (exception: DocumentLoader.DocumentLoaderException) {
                null
            }
        }
    }
}