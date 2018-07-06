package guepardoapps.lucahome.common.methods

import guepardoapps.lucahome.common.utils.Logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

fun hasRoot(): Boolean {
    return checkRootMethodBuildTags()
            || checkRootMethodFilePath()
            || checkRootMethodProcess()
}

fun checkRootMethodBuildTags(): Boolean {
    val buildTags: String = android.os.Build.TAGS
    return buildTags.contains("test-keys")
}

fun checkRootMethodFilePath(): Boolean {
    val paths = arrayListOf(
            "/data/local/bin/su",
            "/data/local/su",
            "/data/local/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/app/Superuser.apk",
            "/system/bin/failsafe/su",
            "/system/bin/su",
            "/system/sd/xbin/su",
            "/system/xbin/su")
    return paths.any { value -> File(value).exists() }
}

fun checkRootMethodProcess(): Boolean {
    var process: Process? = null
    return try {
        process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        val bufferedReader = BufferedReader(InputStreamReader(process!!.inputStream))
        bufferedReader.readLine() != null
    } catch (throwable: Throwable) {
        Logger.instance.debug("Root", "No root detected with method process and error detected!")
        false
    } finally {
        process?.destroy()
    }
}