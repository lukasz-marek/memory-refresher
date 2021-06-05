package org.lmarek.memory.refresher.document.service.write

import kotlinx.coroutines.flow.Flow
import org.lmarek.memory.refresher.document.DocumentPath
import java.lang.Exception

interface WriteDocumentService {
    suspend fun write(path: DocumentPath)
    suspend fun write(paths: Flow<DocumentPath>, onMissing: OnMissing)

    class WriteDocumentServiceException(cause: Throwable): Exception(cause)
}

enum class OnMissing {
    THROW, REMOVE
}