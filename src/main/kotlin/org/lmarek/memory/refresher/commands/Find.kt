package org.lmarek.memory.refresher.commands

import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "find")
class Find : Callable<Int> {
    override fun call(): Int {
        println("Executing find command")
        return 0
    }
}