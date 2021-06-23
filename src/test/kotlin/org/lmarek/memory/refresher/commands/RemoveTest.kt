package org.lmarek.memory.refresher.commands

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.component.KoinApiExtension
import picocli.CommandLine
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Path
import java.nio.file.Paths

@KoinApiExtension
class RemoveTest : CommandTestBase() {

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
    fun `Should remove file by canonical path when it exists in index`(@TempDir directory: Path) {
        // given
        val pathOfExistingFile = hasExistingFile("an_existing_file", directory, listOf("some", "text"))
        val canonicalPath = pathOfExistingFile.toRealPath()
        command.execute("add", pathOfExistingFile.toString())

        // when
        val exitCode = command.execute("remove", canonicalPath.toString())

        // then
        expectThat(exitCode).isEqualTo(0)
        expectThat(errorOutput.toString()).isEmpty()
        expectThat(output.toString().split("\n")) {
            get { get(1) }.isEqualTo("$canonicalPath removed")
        }
    }

    @Test
    fun `Should remove file by relative path when it exists in index`(@TempDir directory: Path) {
        // given
        val pathOfExistingFile = hasExistingFile("an_existing_file", directory, listOf("some", "text"))
        val relativePath = Paths.get("").toAbsolutePath().relativize(pathOfExistingFile)
        command.execute("add", pathOfExistingFile.toString())

        // when
        val exitCode = command.execute("remove", relativePath.toString())

        // then
        expectThat(exitCode).isEqualTo(0)
        expectThat(errorOutput.toString()).isEmpty()
        expectThat(output.toString().split("\n")) {
            get { get(1) }.isEqualTo("${relativePath.toRealPath()} removed")
        }
    }

    @Test
    fun `Should silently fail to remove file if it does not exist in index`(@TempDir directory: Path) {
        // given
        val nonIndexedFile = hasExistingFile("an_existing_file", directory, listOf("some", "text"))

        // when
        val exitCode = command.execute("remove", nonIndexedFile.toString())

        // then
        expectThat(exitCode).isEqualTo(0)
        expectThat(errorOutput.toString()).isEmpty()
        expectThat(output.toString().split("\n")) {
            get { get(0) }.isEqualTo("${nonIndexedFile.toRealPath()} removed")
        }
    }
}