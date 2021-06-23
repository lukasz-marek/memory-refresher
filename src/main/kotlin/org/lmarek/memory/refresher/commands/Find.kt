package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lmarek.memory.refresher.document.repository.read.DocumentQuery
import org.lmarek.memory.refresher.document.repository.read.PathsReadOnlyRepository
import picocli.CommandLine
import java.util.concurrent.Callable

@KoinApiExtension
@CommandLine.Command(name = "find")
class Find : Callable<Int>, KoinComponent {
    private val readOnlyRepository by inject<PathsReadOnlyRepository>()

    @CommandLine.Parameters(index = "0", description = ["search query"], arity = "1..*")
    private lateinit var query: List<String>

    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    override fun call(): Int {
        runBlocking {
            val documentQuery = DocumentQuery(query.joinToString(separator = " "), Int.MAX_VALUE)
            val searchResults = readOnlyRepository.findMatching(documentQuery)
            searchResults.collect { spec.commandLine().out.println(it.value) }
        }
        return 0
    }
}