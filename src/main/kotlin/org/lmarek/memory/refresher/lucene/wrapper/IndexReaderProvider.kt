package org.lmarek.memory.refresher.lucene.wrapper

import org.apache.lucene.index.IndexReader

class IndexReaderProvider {
    interface IndexWriterProvider {
        suspend fun <T> withIndexReader(action: suspend IndexReader.() -> T)
    }
}