package org.lmarek.memory.refresher

import org.lmarek.memory.refresher.commands.Main
import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val commandResult = CommandLine(Main()).execute(*args)
    exitProcess(commandResult)
}