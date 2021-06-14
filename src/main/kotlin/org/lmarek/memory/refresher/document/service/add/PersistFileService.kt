package org.lmarek.memory.refresher.document.service.add

import java.io.File

interface PersistFileService {
    class PersistFileServiceException(message: String, cause: Throwable) : Exception(message, cause)

    suspend fun persist(file: File)
}

