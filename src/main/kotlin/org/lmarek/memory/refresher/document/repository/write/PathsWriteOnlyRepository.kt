package org.lmarek.memory.refresher.document.repository.write

import kotlinx.coroutines.flow.Flow
import org.lmarek.memory.refresher.document.Document
import org.lmarek.memory.refresher.document.DocumentPath

interface PathsWriteOnlyRepository {
    suspend fun save(document: Document)
    suspend fun save(documents: Flow<Document>)
    suspend fun delete(path: DocumentPath)
    suspend fun delete(paths: Flow<DocumentPath>)
}