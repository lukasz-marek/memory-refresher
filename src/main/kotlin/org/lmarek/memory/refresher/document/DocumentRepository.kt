package org.lmarek.memory.refresher.document

interface DocumentRepository {
    fun save(documentToSave: DocumentToSave)
}