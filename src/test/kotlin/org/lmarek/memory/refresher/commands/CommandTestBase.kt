package org.lmarek.memory.refresher.commands

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import picocli.CommandLine
import test.utils.createLuceneModule
import test.utils.createRepositoryModule
import test.utils.createServiceModule
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Callable

@KoinApiExtension
abstract class CommandTestBase {
    private lateinit var app: Callable<*>
    protected lateinit var command: CommandLine
    protected lateinit var output: StringWriter
    protected lateinit var errorOutput: StringWriter

    @BeforeEach
    protected fun setup() {
        startKoin {
            modules(createLuceneModule(), createRepositoryModule(), createServiceModule())
        }
        app = Main()
        command = CommandLine(app)
        output = StringWriter()
        errorOutput = StringWriter()

        command.out = PrintWriter(output)
        command.err = PrintWriter(errorOutput)
    }

    @AfterEach
    protected fun teardown() {
        stopKoin()
    }

    protected fun hasExistingFile(name: String, directory: Path, lines: List<String>): Path {
        val existingFilePath = directory.resolve(name)
        Files.write(existingFilePath, lines)
        return existingFilePath
    }
}