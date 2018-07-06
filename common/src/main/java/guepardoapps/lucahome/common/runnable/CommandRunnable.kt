package guepardoapps.lucahome.common.runnable

import guepardoapps.lucahome.common.methods.performCommand
import guepardoapps.lucahome.common.utils.Logger

class CommandRunnable(private val command: Array<String>) : Runnable {
    private val tag: String = CommandRunnable::class.java.simpleName

    override fun run() {
        if (command.isEmpty()) {
            Logger.instance.error(tag, "Command has no data")
            return
        }

        performCommand(command)
    }
}