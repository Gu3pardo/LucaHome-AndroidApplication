package guepardoapps.lucahome.common.controller

interface ICommandController {
    fun rebootDevice(timeoutMs: Long): Boolean
    fun shutDownDevice(timeoutMs: Long): Boolean

    @Throws(InterruptedException::class)
    fun simulateKeyPress(keys: IntArray, timeout: Int)
}