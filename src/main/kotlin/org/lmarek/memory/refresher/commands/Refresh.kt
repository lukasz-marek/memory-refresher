package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.lmarek.memory.refresher.document.DocumentLoaderImpl
import org.lmarek.memory.refresher.document.find.LucenePathsReadOnlyRepository
import org.lmarek.memory.refresher.document.refresh.RefreshDocumentsServiceImpl
import org.lmarek.memory.refresher.document.refresh.RefreshType
import org.lmarek.memory.refresher.document.register.LucenePathsWriteOnlyRepository
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable

@CommandLine.Command(name = "refresh")
class Refresh : Callable<Int> {
    private val documentLoader = DocumentLoaderImpl()

    override fun call(): Int {
        createIndexDirectoryIfNotExists()
        createIndexWriter().use {
            val documentWriteOnlyRepository = LucenePathsWriteOnlyRepository(it)
            createIndexReader(Paths.get(getIndexDirectoryPath())).use {
                val documentReadOnlyRepository = LucenePathsReadOnlyRepository(StandardAnalyzer()) { IndexSearcher(it) }
                val refreshDocumentsService =
                    RefreshDocumentsServiceImpl(documentReadOnlyRepository, documentWriteOnlyRepository, documentLoader)
                runBlocking {
                    refreshDocumentsService.refreshAll().collect { refreshResult ->
                        when (refreshResult.type) {
                            RefreshType.DELETE -> println("Deleted ${refreshResult.documentPath}")
                            RefreshType.RELOAD -> println("Reloaded ${refreshResult.documentPath}")
                        }
                    }
                }
            }
        }
        return 0
    }


    private fun getIndexDirectoryPath(): String {
        val homeDir = System.getProperty("user.home")
        return homeDir + File.separator + ".memory_refresher" + File.separator + "index"
    }

    private fun createIndexReader(indexDirectory: Path): IndexReader {
        return DirectoryReader.open(FSDirectory.open(indexDirectory))
    }

    private fun createIndexDirectoryIfNotExists() {
        val indexDirectory = File(getIndexDirectoryPath())
        indexDirectory.mkdirs()
        if (!(indexDirectory.canWrite() && indexDirectory.canRead()))
            throw CommandException("Cannot read or write to ${indexDirectory.canonicalPath}")
    }

    private fun createIndexWriter(): IndexWriter {
        val directory = FSDirectory.open(Paths.get(getIndexDirectoryPath()))
        val analyzer = StandardAnalyzer()
        val indexWriterConfig = IndexWriterConfig(analyzer)
        indexWriterConfig.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND
//        indexWriterConfig.ramBufferSizeMB = bufferSize
        return IndexWriter(directory, indexWriterConfig)
    }
}