package org.lmarek.memory.refresher.document.refresh

import kotlinx.coroutines.flow.Flow
import org.lmarek.memory.refresher.document.DocumentPath

enum class RefreshType {
    RELOAD, DELETE
}

data class RefreshResult(val documentPath: DocumentPath, val type: RefreshType)

interface RefreshDocumentsService {
    suspend fun refreshAll(): Flow<RefreshResult>
}