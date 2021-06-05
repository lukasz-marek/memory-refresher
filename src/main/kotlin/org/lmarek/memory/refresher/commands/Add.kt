package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lmarek.memory.refresher.document.repository.write.PathsWriteOnlyRepository
import org.lmarek.memory.refresher.document.service.loader.DocumentLoader
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

    override fun call(): Int = runBlocking {
        try {
            val newDocument = documentLoader.load(fileToBeIndexed)
            writeOnlyRepository.register(newDocument)
            println("${fileToBeIndexed.canonicalPath} loaded")
            return@runBlocking 0
        } catch (ex: DocumentLoader.DocumentLoaderException) {
            System.err.println(ex.message)
            return@runBlocking -1
        }
    }
}