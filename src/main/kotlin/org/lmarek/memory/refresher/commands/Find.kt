package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.runBlocking
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.lmarek.memory.refresher.document.DocumentQuery
import org.lmarek.memory.refresher.document.LuceneFindRegisteredPathsService
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable

@CommandLine.Command(name = "find")
class Find : Callable<Int> {

    @CommandLine.Parameters(index = "0", description = ["search query"], arity = "1..*")
    private lateinit var query: List<String>

    override fun call(): Int {
        createIndexReader(Paths.get(getIndexDirectoryPath())).use {
            val registeredPathsService =
                LuceneFindRegisteredPathsService(StandardAnalyzer(), IndexSearcher(it))
            val documentQuery = DocumentQuery(query.joinToString(separator = " "), Int.MAX_VALUE)
            runBlocking {
                val searchResults = registeredPathsService.findMatching(documentQuery)
                for (result in searchResults)
                    println(result.path)
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