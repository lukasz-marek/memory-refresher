package org.lmarek.memory.refresher.commands

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "refresh")
class Refresh : Callable<Int> {
    override fun call(): Int {
        println("Executing refresh command")
        return 0
    }
}