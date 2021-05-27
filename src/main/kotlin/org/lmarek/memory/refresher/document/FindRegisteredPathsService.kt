package org.lmarek.memory.refresher.document

interface FindRegisteredPathsService {
    fun findMatching(query: DocumentQuery): List<String>
}