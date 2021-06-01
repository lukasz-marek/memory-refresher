package org.lmarek.memory.refresher.commands

import org.koin.core.component.KoinApiExtension
import picocli.CommandLine
import java.util.concurrent.Callable

@KoinApiExtension
@CommandLine.Command(subcommands = [Add::class, Refresh::class, Find::class, Remove::class, ListAll::class])
class Main : Callable<Int> {
    override fun call(): Int {
        println("Executing main command")
        return 0
    }
}