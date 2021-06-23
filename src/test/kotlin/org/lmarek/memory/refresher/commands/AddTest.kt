package org.lmarek.memory.refresher.commands

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.component.KoinApiExtension
import picocli.CommandLine
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@KoinApiExtension
class AddTest : CommandTestBase() {
    private val app = Main()
    private val command = CommandLine(app)
    private val output = StringWriter()
    private val errorOutput = StringWriter()


    @BeforeEach
    fun setup() {
        command.out = PrintWriter(output)
        command.err = PrintWriter(errorOutput)
    }

    @Test
    fun `Should add a file by canonical path when it exists`(@TempDir directory: Path) {
        // given
        val existingFilePath = hasExistingFile("file_that_exists_1", directory, listOf("first line", "second line"))
        val realPath = existingFilePath.toRealPath()

        // when
        val status = command.execute("add", realPath.toString())

        // then
        expectThat(status).isEqualTo(0)
        expectThat(output.toString()).isEqualTo("$realPath loaded\n")
        expectThat(errorOutput.toString()).isEmpty()
    }

    @Test
    fun `Should add a file by relative path when it exists`(@TempDir directory: Path) {
        // given
        val existingFilePath = hasExistingFile("file_that_exists_2", directory, listOf("first line", "second line"))
        val relativePath = Paths.get("").toAbsolutePath().relativize(existingFilePath)

        // when
        val status = command.execute("add", relativePath.toString())

        // then
        expectThat(status).isEqualTo(0)
        expectThat(output.toString()).isEqualTo("${relativePath.toRealPath()} loaded\n")
        expectThat(errorOutput.toString()).isEmpty()
    }

    @Test
    fun `Should throw when a file does not exist`(@TempDir directory: Path) {
        // given
        // when
        val status = command.execute("add", UUID.randomUUID().toString())

        // then
        expectThat(status).isEqualTo(-1)
        expectThat(output.toString()).isEmpty()
        expectThat(errorOutput.toString()).isNotEmpty()
    }
}