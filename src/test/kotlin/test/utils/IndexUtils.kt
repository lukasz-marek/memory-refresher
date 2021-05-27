package test.utils

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path

fun createAnalyzer(): Analyzer = StandardAnalyzer()

fun createIndexWriter(indexDirectory: Path): IndexWriter {
    val directory = FSDirectory.open(indexDirectory)
    val indexWriterConfig = IndexWriterConfig(createAnalyzer())
    indexWriterConfig.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND
    return IndexWriter(directory, indexWriterConfig)
}

fun createIndexReader(indexDirectory: Path): IndexReader {
    return DirectoryReader.open(FSDirectory.open(indexDirectory))
}