package org.lmarek.memory.refresher.document

import kotlinx.coroutines.channels.ReceiveChannel

interface FindRegisteredPathsService {
    suspend fun findMatching(query: DocumentQuery): ReceiveChannel<RegisteredPath>
}