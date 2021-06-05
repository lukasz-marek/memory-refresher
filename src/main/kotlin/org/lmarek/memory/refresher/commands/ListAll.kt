package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lmarek.memory.refresher.document.repository.read.PathsReadOnlyRepository
import picocli.CommandLine
import java.util.concurrent.Callable

@KoinApiExtension
@CommandLine.Command(name = "list")
class ListAll : Callable<Int>, KoinComponent {
    private val readOnlyRepository by inject<PathsReadOnlyRepository>()

    override fun call(): Int {
        runBlocking {
            val searchResults = readOnlyRepository.listAll()
            searchResults.collect { println(it.value) }
        }
        return 0
    }
}