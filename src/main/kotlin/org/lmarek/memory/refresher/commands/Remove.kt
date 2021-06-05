package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lmarek.memory.refresher.document.DocumentPath
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@KoinApiExtension
@CommandLine.Command(name = "remove")
class Remove : Callable<Int>, KoinComponent {
    private val writeOnlyRepository by inject<PathsWriteOnlyRepository>()

    @CommandLine.Parameters(index = "0", description = ["File to be removed from index"])
    private lateinit var fileToBeRemovedFromIndex: File

    override fun call(): Int {
        runBlocking {
            writeOnlyRepository.delete(DocumentPath(fileToBeRemovedFromIndex.canonicalPath))
        }
        println("${fileToBeRemovedFromIndex.canonicalPath} removed")
        return 0
    }
}