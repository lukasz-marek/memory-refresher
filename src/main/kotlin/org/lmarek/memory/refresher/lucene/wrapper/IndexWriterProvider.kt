package org.lmarek.memory.refresher.lucene.wrapper

import org.apache.lucene.index.IndexWriter

interface IndexWriterProvider {
    suspend fun <T> withIndexWriter(action: suspend IndexWriter.() -> T)
}