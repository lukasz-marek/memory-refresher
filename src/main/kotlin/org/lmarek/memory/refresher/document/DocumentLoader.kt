package org.lmarek.memory.refresher.document

import java.io.File
import java.io.IOException

class DocumentLoader {

    class DocumentLoaderException(cause: Throwable) : Exception(cause)

    fun load(file: File): Document {
        try {
            return Document(file.absolutePath, file.readText())
        } catch (exception: IOException) {
            throw DocumentLoaderException(exception)
        }
    }
}