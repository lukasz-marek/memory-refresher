package org.lmarek.memory.refresher.document

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class DocumentLoaderImplTest {

    private val tested = DocumentLoaderImpl()

    @Test
    fun `should load file into a document if it exists`(@TempDir tempDir: Path) = runBlocking<Unit> {
        // given
        val existingFilePath = tempDir.resolve("existing_file.txt")
        Files.write(existingFilePath, listOf("some content"))

        // when
        val document = tested.load(existingFilePath.toFile())

        // then
        expectThat(document) {
            get { Paths.get(path.value).isAbsolute }.isTrue()
            get { content }.isEqualTo("some content\n")
        }
    }

    @Test
    fun `should throw if file does not exist`(@TempDir tempDir: Path) = runBlocking<Unit> {
        // given
        val nonExistentFilePath = tempDir.resolve("non_existent_file.txt")

        // when / then
        expectThrows<DocumentLoader.DocumentLoaderException> { tested.load(nonExistentFilePath.toFile()) }
    }
}