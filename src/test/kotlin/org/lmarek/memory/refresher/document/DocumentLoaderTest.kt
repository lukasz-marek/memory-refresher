package org.lmarek.memory.refresher.document

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class DocumentLoaderTest {

    private val tested = DocumentLoader()

    @Test
    fun `should load file into a document if it exists`(@TempDir tempDir: Path) {
        // given
        val existingFilePath = tempDir.resolve("existing_file.txt")
        Files.write(existingFilePath, listOf("some content"))

        // when
        val document = tested.load(existingFilePath.toFile())

        // then
        expectThat(document) {
            get { Paths.get(path).isAbsolute }.isTrue()
            get { content }.isEqualTo("some content\n")
        }
    }

    @Test
    fun `should throw if file does not exist`(@TempDir tempDir: Path) {
        // given
        val nonExistentFilePath = tempDir.resolve("non_existent_file.txt")

        // when / then
        expectThrows<DocumentLoader.DocumentLoaderException> { tested.load(nonExistentFilePath.toFile()) }
    }
}