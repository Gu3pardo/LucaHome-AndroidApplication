package guepardoapps.lucahome.common.databases.common

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import guepardoapps.lucahome.common.extensions.common.toBoolean
import guepardoapps.lucahome.common.extensions.common.toInteger
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.utils.Logger
import java.util.*

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

abstract class DbHandler<T>(context: Context, private val tableLastChange: String? = null, private val tableSettings: String? = null)
    : SQLiteOpenHelper(context, databaseName, null, databaseVersion), IDbHandler<T> {

    private val tag: String = DbHandler::class.java.simpleName

    override fun getLastChangeDateTime(): Calendar? {
        if (tableLastChange.isNullOrEmpty()) {
            Logger.instance.info(tag, "getLastChangeDateTime not supported")
            return null
        }

        val database = this.readableDatabase
        val projection = arrayOf(columnId, columnLastChangeDateTime)
        val sortOrder = "$columnId ASC"
        val cursor = database.query(tableLastChange, projection, null, null, null, null, sortOrder)

        val list = mutableListOf<Calendar>()
        with(cursor) {
            while (moveToNext()) {
                val lastChangeDateTime = Calendar.getInstance()
                lastChangeDateTime.timeInMillis = getLong(getColumnIndexOrThrow(columnLastChangeDateTime))
                list.add(lastChangeDateTime)
            }
        }

        return list.firstOrNull()
    }

    override fun setLastChangeDateTime(entity: Calendar): Long {
        if (tableLastChange.isNullOrEmpty()) {
            Logger.instance.info(tag, "setLastChangeDateTime not supported")
            return -1
        }

        val values = ContentValues().apply {
            put(columnLastChangeDateTime, entity.timeInMillis)
        }
        val database = this.writableDatabase
        return database.insert(tableLastChange, null, values)
    }

    override fun getServiceSettings(): ServiceSettings? {
        if (tableSettings.isNullOrEmpty()) {
            Logger.instance.info(tag, "getServiceSettings not supported")
            return null
        }

        val database = this.readableDatabase
        val projection = arrayOf(columnId, columnReloadEnabled, columnReloadTimeoutMs, columnNotificationEnabled)
        val sortOrder = "$columnId ASC"
        val cursor = database.query(tableSettings, projection, null, null, null, null, sortOrder)

        val list = mutableListOf<ServiceSettings>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(columnId))
                val reloadEnabled = getInt(getColumnIndexOrThrow(columnReloadEnabled)).toBoolean()
                val reloadTimeoutMs = getInt(getColumnIndexOrThrow(columnReloadTimeoutMs))
                val notificationEnabled = getInt(getColumnIndexOrThrow(columnNotificationEnabled)).toBoolean()
                list.add(ServiceSettings(id, reloadEnabled, reloadTimeoutMs, notificationEnabled))
            }
        }

        return list.firstOrNull()
    }

    override fun setServiceSettings(entity: ServiceSettings): Long {
        if (tableSettings.isNullOrEmpty()) {
            Logger.instance.info(tag, "setServiceSettings not supported")
            return -1
        }

        val values = ContentValues().apply {
            put(columnReloadEnabled, entity.reloadEnabled.toInteger())
            put(columnReloadTimeoutMs, entity.reloadTimeoutMs)
            put(columnNotificationEnabled, entity.notificationEnabled.toInteger())
        }
        val database = this.writableDatabase
        return database.insert(tableSettings, null, values)
    }

    companion object {
        const val databaseVersion = 1
        const val databaseName = "guepardoapps-lucahome.db"
        const val columnId = "_id"
        const val columnLastChangeDateTime = "lastChangeDateTime"
        const val columnReloadEnabled = "reloadEnabled"
        const val columnReloadTimeoutMs = "reloadTimeoutMs"
        const val columnNotificationEnabled = "notificationEnabled"
    }
}