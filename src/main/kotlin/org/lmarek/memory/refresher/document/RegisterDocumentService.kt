package org.lmarek.memory.refresher.document

import kotlinx.coroutines.channels.ReceiveChannel

interface RegisterDocumentService {
    suspend fun register(document: Document)
    suspend fun unregister(path: DocumentPath)
    suspend fun unregister(paths: ReceiveChannel<DocumentPath>)
}