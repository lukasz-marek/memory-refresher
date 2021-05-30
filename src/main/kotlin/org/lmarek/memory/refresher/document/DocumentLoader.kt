package org.lmarek.memory.refresher.document

import java.io.File

interface DocumentLoader {
    class DocumentLoaderException(cause: Throwable) : Exception(cause)

    fun load(file: File): Document
}