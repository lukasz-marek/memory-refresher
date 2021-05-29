package org.lmarek.memory.refresher.document.register

import kotlinx.coroutines.channels.ReceiveChannel
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath

interface PathsWriteOnlyRepository {
    suspend fun register(document: Document)
    suspend fun unregister(path: DocumentPath)
    suspend fun unregister(paths: ReceiveChannel<DocumentPath>)
}