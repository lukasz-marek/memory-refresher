package org.lmarek.memory.refresher.commands

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(subcommands = [Init::class, Add::class, Refresh::class, Find::class])
class Main : Callable<Int> {
    override fun call(): Int {
        println("Executing main command")
        return 0
    }
}