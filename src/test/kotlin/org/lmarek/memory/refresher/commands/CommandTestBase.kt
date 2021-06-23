package org.lmarek.memory.refresher.commands

import test.utils.initDependencies
import java.nio.file.Files
import java.nio.file.Path

abstract class CommandTestBase {

    companion object {
        init {
            initDependencies()
        }
    }

    protected fun hasExistingFile(name: String, directory: Path, lines: List<String>): Path {
        val existingFilePath = directory.resolve(name)
        Files.write(existingFilePath, lines)
        return existingFilePath
    }
}