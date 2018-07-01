package guepardoapps.lucahome.common.utils

import android.content.Context
import android.support.annotation.NonNull
import android.util.Log
import guepardoapps.lucahome.common.databases.logging.*
import java.sql.Date
import java.util.*

class Logger private constructor() {
    var loggingEnabled: Boolean = true
    var writeToDatabaseEnabled: Boolean = true

    private var _dbHandler: DbHandler? = null

    init {
    }

    private object Holder {
        val instance: Logger = Logger()
    }

    companion object {
        val instance: Logger by lazy { Holder.instance }
    }

    fun initialize(context: Context) {
        if (_dbHandler != null) {
            return
        }
        _dbHandler = DbHandler(context, null)
    }

    fun <T> verbose(@NonNull tag: String, @NonNull description: T) {
        if (loggingEnabled) {
            Log.v(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Verbose)
        }
    }

    fun <T> debug(@NonNull tag: String, @NonNull description: T) {
        if (loggingEnabled) {
            Log.d(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Debug)
        }
    }

    fun <T> info(@NonNull tag: String, @NonNull description: T) {
        if (loggingEnabled) {
            Log.i(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Info)
        }
    }

    fun <T> warning(@NonNull tag: String, @NonNull description: T) {
        if (loggingEnabled) {
            Log.w(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Warning)
        }
    }

    fun <T> error(@NonNull tag: String, @NonNull description: T) {
        if (loggingEnabled) {
            Log.e(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Error)
        }
    }

    private fun <T> tryToWriteToDatabase(
            @NonNull tag:
            String, @NonNull description: T,
            severity: Severity) {
        if (writeToDatabaseEnabled && _dbHandler != null) {
            _dbHandler?.addLog(
                    DbLog(-1,
                            Date(Calendar.getInstance().timeInMillis),
                            severity,
                            tag,
                            description.toString()))
        }
    }
}