package org.lmarek.memory.refresher.document.service.add.loader

import org.lmarek.memory.refresher.document.Document
import java.io.File

interface DocumentLoader {
    class DocumentLoaderException(cause: Throwable) : Exception(cause)

    suspend fun load(file: File): Document
}