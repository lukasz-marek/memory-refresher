package org.lmarek.memory.refresher.commands

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import picocli.CommandLine
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Callable

@CommandLine.Command(name = "add")
class Add : Callable<Int> {
    override fun call(): Int {
        println("Executing add command")
        createIndexDirectoryIfNotExists()
        createIndexWriter().use {

        }
        return 0
    }

    private fun getIndexDirectoryPath(): String {
        val homeDir = System.getProperty("user.home")
        return homeDir + File.separator + ".memory_refresher" + File.separator + "index"
    }

    private fun createIndexDirectoryIfNotExists() {
        val indexDirectory = File(getIndexDirectoryPath())
        indexDirectory.mkdirs()
        if(!(indexDirectory.canWrite() && indexDirectory.canRead()))
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