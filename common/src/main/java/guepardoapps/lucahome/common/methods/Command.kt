package guepardoapps.lucahome.common.methods

import guepardoapps.lucahome.common.utils.Logger

fun performCommand(command: Array<String>): Boolean {
    if (!hasRoot()) {
        Logger.instance.warning("Command", "Device is not rooted!")
        return false
    }

    return try {
        val process = Runtime.getRuntime().exec(command)
        process.waitFor()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        false
    }

}