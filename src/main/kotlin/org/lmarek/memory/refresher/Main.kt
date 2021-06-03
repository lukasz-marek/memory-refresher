package org.lmarek.memory.refresher

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.lmarek.memory.refresher.commands.CommandException
import org.lmarek.memory.refresher.commands.Main
import org.lmarek.memory.refresher.document.find.LucenePathsReadOnlyRepository
import org.lmarek.memory.refresher.document.find.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.loader.DocumentLoader
import org.lmarek.memory.refresher.document.loader.DocumentLoaderImpl
import org.lmarek.memory.refresher.document.refresh.RefreshDocumentsService
import org.lmarek.memory.refresher.document.refresh.RefreshDocumentsServiceImpl
import org.lmarek.memory.refresher.document.register.LucenePathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.register.PathsWriteOnlyRepository
import picocli.CommandLine
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess


@KoinApiExtension
fun main(args: Array<String>) {
    createIndexDirectoryIfNotExists()
    initDependencies()

    val commandResult = CommandLine(Main()).execute(*args)
    exitProcess(commandResult)
}

private fun initDependencies() {
    val luceneModule = module {
        single { createIndexWriter() }
        factory { createIndexReader() }
        single<Analyzer> { StandardAnalyzer() }
        factory { { IndexSearcher(createIndexReader()) } }
    }
    val repositoryModule = module {
        single<PathsReadOnlyRepository> { LucenePathsReadOnlyRepository(get(), get()) }
        single<PathsWriteOnlyRepository> { LucenePathsWriteOnlyRepository(get()) }
    }
    val serviceModule = module {
        single<DocumentLoader> { DocumentLoaderImpl() }
        single<RefreshDocumentsService> { RefreshDocumentsServiceImpl(get(), get(), get()) }
    }
    startKoin {
        modules(luceneModule, repositoryModule, serviceModule)
    }
}

private fun getIndexDirectoryPath(): String {
    val homeDir = System.getProperty("user.home")
    return homeDir + File.separator + ".memory_refresher" + File.separator + "index"
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
    return IndexWriter(directory, indexWriterConfig)
}

private fun createIndexReader(): IndexReader {
    return DirectoryReader.open(FSDirectory.open(Paths.get(getIndexDirectoryPath())))
}