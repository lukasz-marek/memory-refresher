package org.lmarek.memory.refresher.document.find

import kotlinx.coroutines.channels.ReceiveChannel
import org.lmarek.memory.refresher.document.DocumentPath

interface PathsReadOnlyRepository {
    suspend fun findMatching(query: DocumentQuery): ReceiveChannel<DocumentPath>
    suspend fun listAll(): ReceiveChannel<DocumentPath>
}