package org.lmarek.memory.refresher.document.find

import kotlinx.coroutines.flow.Flow
import org.lmarek.memory.refresher.document.DocumentPath

interface PathsReadOnlyRepository {
    suspend fun findMatching(query: DocumentQuery): Flow<DocumentPath>
    suspend fun listAll(): Flow<DocumentPath>
}