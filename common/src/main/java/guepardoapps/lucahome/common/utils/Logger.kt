package guepardoapps.lucahome.common.utils

import android.content.Context
import android.support.annotation.NonNull
import android.util.Log
import guepardoapps.lucahome.common.databases.logging.DbHandler
import guepardoapps.lucahome.common.databases.logging.DbLog
import guepardoapps.lucahome.common.databases.logging.Severity
import java.sql.Date
import java.util.*

class Logger private constructor() {
    private var _loggingEnabled: Boolean = true
    private var _writeToDatabaseEnabled: Boolean = true

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

    fun setLoggingEnabled(loggingEnabled: Boolean) {
        _loggingEnabled = loggingEnabled
    }

    fun isLoggingEnabled(): Boolean {
        return _loggingEnabled
    }

    fun setWriteToDatabaseEnabled(writeToDatabaseEnabled: Boolean) {
        _writeToDatabaseEnabled = writeToDatabaseEnabled
    }

    fun isWriteToDatabaseEnabled(): Boolean {
        return _writeToDatabaseEnabled
    }

    fun <T> verbose(@NonNull tag: String, @NonNull description: T) {
        if (_loggingEnabled) {
            Log.v(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Verbose)
        }
    }

    fun <T> debug(@NonNull tag: String, @NonNull description: T) {
        if (_loggingEnabled) {
            Log.d(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Debug)
        }
    }

    fun <T> info(@NonNull tag: String, @NonNull description: T) {
        if (_loggingEnabled) {
            Log.i(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Info)
        }
    }

    fun <T> warning(@NonNull tag: String, @NonNull description: T) {
        if (_loggingEnabled) {
            Log.w(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Warning)
        }
    }

    fun <T> error(@NonNull tag: String, @NonNull description: T) {
        if (_loggingEnabled) {
            Log.e(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Error)
        }
    }

    private fun <T> tryToWriteToDatabase(
            @NonNull tag:
            String, @NonNull description: T,
            severity: Severity) {
        if (_writeToDatabaseEnabled && _dbHandler != null) {
            _dbHandler?.addLog(
                    DbLog(-1,
                            Date(Calendar.getInstance().timeInMillis),
                            severity,
                            tag,
                            description.toString()))
        }
    }
}