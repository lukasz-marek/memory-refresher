package test.utils

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path


fun createIndexWriter(indexDirectory: Path): IndexWriter {
    val directory = FSDirectory.open(indexDirectory)
    val analyzer = StandardAnalyzer()
    val indexWriterConfig = IndexWriterConfig(analyzer)
    indexWriterConfig.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND
    return IndexWriter(directory, indexWriterConfig)
}

fun createIndexReader(indexDirectory: Path): IndexReader {
    return DirectoryReader.open(FSDirectory.open(indexDirectory))
}