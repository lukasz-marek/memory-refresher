package org.lmarek.memory.refresher.document.service.refresh

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.search.IndexSearcher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.lmarek.memory.refresher.document.repository.read.LucenePathsReadOnlyRepository
import org.lmarek.memory.refresher.document.repository.read.PathsReadOnlyRepository
import org.lmarek.memory.refresher.document.service.loader.DocumentLoader
import org.lmarek.memory.refresher.document.service.loader.DocumentLoaderImpl
import org.lmarek.memory.refresher.document.repository.write.LucenePathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import test.utils.createIndexReader
import test.utils.createIndexWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

@ExperimentalCoroutinesApi
class RefreshDocumentsServiceImplTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var documentLoader: DocumentLoader
    private lateinit var indexWriter: IndexWriter
    lateinit var writeOnlyRepository: PathsWriteOnlyRepository
    private lateinit var readOnlyRepository: PathsReadOnlyRepository
    private lateinit var tested: RefreshDocumentsService

    @BeforeEach
    fun setup() {
        documentLoader = DocumentLoaderImpl()
        indexWriter = createIndexWriter(tempDir)
        writeOnlyRepository = LucenePathsWriteOnlyRepository(indexWriter)
        readOnlyRepository =
            LucenePathsReadOnlyRepository(StandardAnalyzer()) { IndexSearcher(createIndexReader(tempDir)) }
        tested = RefreshDocumentsServiceImpl(readOnlyRepository, writeOnlyRepository, documentLoader)
    }

    @AfterEach
    fun teardown() {
        indexWriter.close()
    }

    @Test
    fun `should reload all documents`() = runBlocking<Unit> {
        // given
        for (i in 1..10) {
            val path = tempDir.resolve("file_$i.txt")
            Files.write(path, listOf("content_$i"))
            writeOnlyRepository.register(documentLoader.load(path.toFile()))
        }

        // when
        val refreshResults = tested.refreshAll().toList()

        // then
        expectThat(refreshResults).hasSize(10)
            .and { all { get { type }.isEqualTo(RefreshType.RELOAD) } }
        val reader = createIndexReader(tempDir)
        expectThat(reader.numDocs()).isEqualTo(10)
    }

    @Test
    fun `should delete all documents`() = runBlocking<Unit> {
        // given
        for (i in 1..10) {
            val path = tempDir.resolve("file_$i.txt")
            Files.write(path, listOf("content_$i"))
            writeOnlyRepository.register(documentLoader.load(path.toFile()))
            path.deleteIfExists()
        }

        // when
        val refreshResults = tested.refreshAll().toList()

        // then
        expectThat(refreshResults).hasSize(10)
            .and { all { get { type }.isEqualTo(RefreshType.DELETE) } }
        val reader = createIndexReader(tempDir)
        expectThat(reader.numDocs()).isEqualTo(0)
    }

    @Test
    fun `should delete some documents`() = runBlocking<Unit> {
        // given
        for (i in 1..7) {
            val path = tempDir.resolve("file_$i.txt")
            Files.write(path, listOf("content_$i"))
            writeOnlyRepository.register(documentLoader.load(path.toFile()))
        }
        for (i in 8..15) {
            val path = tempDir.resolve("file_$i.txt")
            Files.write(path, listOf("content_$i"))
            writeOnlyRepository.register(documentLoader.load(path.toFile()))
            path.deleteIfExists()
        }

        // when
        val refreshResults = tested.refreshAll().toList()

        // then
        expectThat(refreshResults).hasSize(15)
        val (reloads, deletions) = refreshResults.partition { it.type == RefreshType.RELOAD }
        expectThat(reloads).hasSize(7)
        expectThat(deletions).hasSize(8)
        val reader = createIndexReader(tempDir)
        expectThat(reader.numDocs()).isEqualTo(7)
    }
}