package test.utils

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.*
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.lmarek.memory.refresher.commands.CommandException
import org.lmarek.memory.refresher.document.repository.read.LucenePathsReadOnlyRepository
import org.lmarek.memory.refresher.document.repository.read.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.repository.write.LucenePathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.add.PersistFileInRepositoryService
import org.lmarek.memory.refresher.document.service.add.PersistFileService
import org.lmarek.memory.refresher.document.service.add.loader.CanonicalPathResolvingDocumentLoader
import org.lmarek.memory.refresher.document.service.add.loader.DocumentLoader
import org.lmarek.memory.refresher.document.service.refresh.ConcurrentRefreshDocumentsService
import org.lmarek.memory.refresher.document.service.refresh.RefreshDocumentsService
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

fun initDependencies() {
    startKoin {
        modules(luceneModule, repositoryModule, serviceModule)
    }
}

val luceneModule = module {
    single { createIndexWriter(get<Analyzer>()) }
    single<Analyzer> { StandardAnalyzer() }
    factory { { createIndexSearcher(createIndexReader()) } }
}

val repositoryModule = module {
    single<PathsReadOnlyRepository> { LucenePathsReadOnlyRepository(get(), get()) }
    single<PathsWriteOnlyRepository> { LucenePathsWriteOnlyRepository(get()) }
}

val serviceModule = module {
    single<DocumentLoader> { CanonicalPathResolvingDocumentLoader() }
    single<RefreshDocumentsService> { ConcurrentRefreshDocumentsService(get(), get(), get()) }
    single<PersistFileService> { PersistFileInRepositoryService(get(), get()) }
}

private fun createIndexSearcher(indexReader: IndexReader?): IndexSearcher? =
    if (indexReader == null) null else IndexSearcher(indexReader)

private val temporaryIndexDirectory = createTempDirectory().pathString
private fun getIndexDirectoryPath(): String {
    return temporaryIndexDirectory + File.separator + ".memory_refresher" + File.separator + "index"
}

private fun createIndexDirectoryIfNotExists() {
    val indexDirectory = File(getIndexDirectoryPath())
    indexDirectory.mkdirs()
    if (!(indexDirectory.canWrite() && indexDirectory.canRead()))
        throw CommandException("Cannot read or write to ${indexDirectory.canonicalPath}")
}

private fun createIndexWriter(analyzer: Analyzer): IndexWriter {
    val directory = FSDirectory.open(Paths.get(getIndexDirectoryPath()))
    val indexWriterConfig = IndexWriterConfig(analyzer)
    indexWriterConfig.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND
    indexWriterConfig.ramBufferSizeMB = 128.0
    return IndexWriter(directory, indexWriterConfig)
}

private fun createIndexReader(): IndexReader? = try {
    DirectoryReader.open(FSDirectory.open(Paths.get(getIndexDirectoryPath())))
} catch (indexNotExists: IndexNotFoundException) {
    null
}
