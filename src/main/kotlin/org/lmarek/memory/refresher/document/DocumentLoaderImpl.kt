package org.lmarek.memory.refresher.document

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class DocumentLoaderImpl : DocumentLoader {

    override suspend fun load(file: File): Document {
        return withContext(Dispatchers.IO) {
            try {
                Document(DocumentPath(file.canonicalPath), file.readText())
            } catch (exception: IOException) {
                throw DocumentLoader.DocumentLoaderException(exception)
            }
        }
    }
}