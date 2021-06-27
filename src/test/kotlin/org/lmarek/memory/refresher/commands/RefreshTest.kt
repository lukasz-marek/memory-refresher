package org.lmarek.memory.refresher.commands

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.component.KoinApiExtension
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.nio.file.Path

@KoinApiExtension
internal class RefreshTest : CommandTestBase() {

    @Test
    fun `Should refresh no entries when index is empty`(@TempDir directory: Path) {
        // when
        val status = command.execute("refresh")

        // then
        expectThat(status).isEqualTo(0)
        expectThat(output.toString()).isEmpty()
        expectThat(errorOutput.toString()).isEmpty()
    }

    @Test
    fun `Should refresh all entries when files exist`(@TempDir directory: Path) {
        // given
        val fileNames = setOf("file1", "file2", "file3")
        val filePaths = fileNames.map { hasIndexedFile(it, directory) }

        // when
        val status = command.execute("refresh")

        // then
        expectThat(status).isEqualTo(0)
        expectThat(output.toString().lines().dropWhile { it.endsWith("loaded") }.filter { it.isNotEmpty() })
            .containsExactlyInAnyOrder(filePaths.map { "Reloaded $it" })
        expectThat(errorOutput.toString()).isEmpty()
    }

    private fun hasIndexedFile(fileName: String, directory: Path): Path {
        val existingFilePath = hasExistingFile(fileName, directory, listOf("first line", "second line"))
        val realPath = existingFilePath.toRealPath()
        command.execute("add", realPath.toString())
        return realPath
    }
}