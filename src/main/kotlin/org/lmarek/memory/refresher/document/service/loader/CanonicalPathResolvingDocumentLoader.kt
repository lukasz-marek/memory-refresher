package org.lmarek.memory.refresher.document.service.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath
import java.io.File
import java.io.IOException

class CanonicalPathResolvingDocumentLoader : DocumentLoader {

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