package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lmarek.memory.refresher.document.loader.DocumentLoader
import org.lmarek.memory.refresher.document.register.PathsWriteOnlyRepository
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@KoinApiExtension
@CommandLine.Command(name = "add")
class Add : Callable<Int>, KoinComponent {
    private val writeOnlyRepository by inject<PathsWriteOnlyRepository>()
    private val documentLoader by inject<DocumentLoader>()

    @CommandLine.Parameters(index = "0", description = ["File to be added to index"])
    private lateinit var fileToBeIndexed: File

    override fun call(): Int {
        runBlocking {
            val newDocument = documentLoader.load(fileToBeIndexed)
            writeOnlyRepository.register(newDocument)
        }
        println("${fileToBeIndexed.canonicalPath} loaded")
        return 0
    }
}