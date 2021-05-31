package org.lmarek.memory.refresher.document.register

import kotlinx.coroutines.flow.Flow
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath

interface PathsWriteOnlyRepository {
    suspend fun register(document: Document)
    suspend fun register(documents: Flow<Document>)
    suspend fun unregister(path: DocumentPath)
    suspend fun unregister(paths: Flow<DocumentPath>)
}