package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.lmarek.memory.refresher.document.find.LucenePathsReadOnlyRepository
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable

@CommandLine.Command(name = "list")
class ListAll : Callable<Int> {

    override fun call(): Int {
        createIndexReader(Paths.get(getIndexDirectoryPath())).use {
            val registeredPathsService = LucenePathsReadOnlyRepository(StandardAnalyzer()) { IndexSearcher(it) }
            runBlocking {
                val searchResults = registeredPathsService.listAll()
                searchResults.consumeEach { println(it.value) }
            }
        }
        return 0
    }

    private fun createIndexReader(indexDirectory: Path): IndexReader {
        return DirectoryReader.open(FSDirectory.open(indexDirectory))
    }

    private fun getIndexDirectoryPath(): String {
        val homeDir = System.getProperty("user.home")
        return homeDir + File.separator + ".memory_refresher" + File.separator + "index"
    }
}