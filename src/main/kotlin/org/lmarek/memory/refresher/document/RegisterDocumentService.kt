package org.lmarek.memory.refresher.document

interface RegisterDocumentService {
    suspend fun register(document: Document)
}