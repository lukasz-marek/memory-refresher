package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lmarek.memory.refresher.document.service.add.PersistFileService
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@KoinApiExtension
@CommandLine.Command(name = "add")
class Add : Callable<Int>, KoinComponent {
    private val service by inject<PersistFileService>()

    @CommandLine.Parameters(index = "0", description = ["File to be added to index"])
    private lateinit var fileToBeIndexed: File

    override fun call(): Int = runBlocking {
        try {
            service.persist(fileToBeIndexed)
            println("${fileToBeIndexed.canonicalPath} loaded")
            return@runBlocking 0
        } catch (ex: PersistFileService.PersistFileServiceException) {
            System.err.println(ex.message)
            return@runBlocking -1
        }
    }
}