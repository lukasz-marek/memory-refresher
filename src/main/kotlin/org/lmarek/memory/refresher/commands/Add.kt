package org.lmarek.memory.refresher.commands

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import org.lmarek.memory.refresher.document.DocumentLoader
import org.lmarek.memory.refresher.document.LuceneDocumentRepository
import picocli.CommandLine
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Callable

@CommandLine.Command(name = "add")
class Add : Callable<Int> {

    @CommandLine.Parameters(index = "0", description = ["File to be added to index"])
    private lateinit var fileToBeIndexed: File

    private val documentLoader = DocumentLoader()

    override fun call(): Int {
        createIndexDirectoryIfNotExists()
        createIndexWriter().use {
            val documentRepository = LuceneDocumentRepository(it)
            val newDocument = documentLoader.load(fileToBeIndexed)
            documentRepository.save(newDocument)
        }
        println("${fileToBeIndexed.absolutePath} loaded")
        return 0
    }

    private fun getIndexDirectoryPath(): String {
        val homeDir = System.getProperty("user.home")
        return homeDir + File.separator + ".memory_refresher" + File.separator + "index"
    }

    private fun createIndexDirectoryIfNotExists() {
        val indexDirectory = File(getIndexDirectoryPath())
        indexDirectory.mkdirs()
        if (!(indexDirectory.canWrite() && indexDirectory.canRead()))
            throw CommandException("Cannot read or write to ${indexDirectory.absolutePath}")
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