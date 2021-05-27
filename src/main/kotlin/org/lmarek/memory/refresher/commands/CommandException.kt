package org.lmarek.memory.refresher.commands

class CommandException(msg: String, cause: Throwable) : Exception(msg, cause) {
    constructor(msg: String) : this(msg, Exception())
}