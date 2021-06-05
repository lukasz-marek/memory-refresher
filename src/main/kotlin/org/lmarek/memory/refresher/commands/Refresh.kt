package org.lmarek.memory.refresher.commands

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lmarek.memory.refresher.document.service.refresh.RefreshDocumentsService
import org.lmarek.memory.refresher.document.service.refresh.RefreshType
import picocli.CommandLine
import java.util.concurrent.Callable

@KoinApiExtension
@CommandLine.Command(name = "refresh")
class Refresh : Callable<Int>, KoinComponent {
    private val refreshDocumentsService by inject<RefreshDocumentsService>()

    @ExperimentalCoroutinesApi
    override fun call(): Int {
        runBlocking {
            refreshDocumentsService.refreshAll().collect { refreshResult ->
                when (refreshResult.type) {
                    RefreshType.DELETE -> println("Deleted ${refreshResult.documentPath.value}")
                    RefreshType.RELOAD -> println("Reloaded ${refreshResult.documentPath.value}")
                }
            }
        }
        return 0
    }
}