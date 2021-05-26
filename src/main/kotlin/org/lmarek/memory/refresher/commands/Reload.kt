package org.lmarek.memory.refresher.commands

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "reload")
class Reload : Callable<Int> {
    override fun call(): Int {
        println("Executing reload command")
        return 0
    }
}