package org.lmarek.memory.refresher.commands

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "add")
class Add : Callable<Int> {
    override fun call(): Int {
        println("Executing add command")
        return 0
    }
}