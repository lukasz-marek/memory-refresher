package org.lmarek.memory.refresher.document

import java.io.File

interface DocumentLoader {
    class DocumentLoaderException(cause: Throwable) : Exception(cause)

    suspend fun load(file: File): Document
}