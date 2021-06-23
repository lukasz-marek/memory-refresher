package org.lmarek.memory.refresher.commands

import test.utils.initDependencies

abstract class CommandTestBase {
    companion object{
        init {
            initDependencies()
        }
    }
}