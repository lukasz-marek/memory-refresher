package org.lmarek.memory.refresher.document.refresh

import kotlinx.coroutines.channels.ReceiveChannel
import org.lmarek.memory.refresher.document.DocumentPath

enum class RefreshType {
    RELOAD, DELETE
}

data class RefreshResult(val documentPath: DocumentPath, val type: RefreshType)

interface RefreshDocumentsService {
    suspend fun refreshAll(): ReceiveChannel<RefreshResult>
}