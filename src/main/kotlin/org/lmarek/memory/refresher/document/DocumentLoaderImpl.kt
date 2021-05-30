package org.lmarek.memory.refresher.document

import java.io.File
import java.io.IOException

class DocumentLoaderImpl : DocumentLoader {

    override fun load(file: File): Document {
        try {
            return Document(DocumentPath(file.canonicalPath), file.readText())
        } catch (exception: IOException) {
            throw DocumentLoader.DocumentLoaderException(exception)
        }
    }
}