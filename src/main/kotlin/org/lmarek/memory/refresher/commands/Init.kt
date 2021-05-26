package org.lmarek.memory.refresher.commands

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "init")
class Init : Callable<Int> {
    override fun call(): Int {
        println("Executing init command")
        return 0
    }
}