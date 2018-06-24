package guepardoapps.lucahome.common.databases.wirelesssocket

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import guepardoapps.lucahome.common.extensions.toBoolean
import guepardoapps.lucahome.common.extensions.toInteger
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import java.util.*

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

class DbWirelessSocket(context: Context, factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DatabaseName, factory, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createDataTable = (
                "CREATE TABLE $DatabaseTable" +
                        "(" +
                        "$ColumnId INTEGER PRIMARY KEY," +
                        "$ColumnUuid TEXT," +
                        "$ColumnRoomUuid TEXT," +
                        "$ColumnName TEXT," +
                        "$ColumnCode TEXT," +
                        "$ColumnState INT," +
                        "$ColumnLastTriggerDateTime INT," +
                        "$ColumnLastTriggerUser TEXT," +
                        "$ColumnIsOnServer INT," +
                        "$ColumnServerAction INT," +
                        "$ColumnChangeCount INT," +
                        "$ColumnShowInNotification INT)")

        database.execSQL(createDataTable)

        val createLastChangeTable = (
                "CREATE TABLE $DatabaseTableLastChange" +
                        "(" +
                        "$ColumnId INTEGER PRIMARY KEY," +
                        "$ColumnLastChangeDateTime INT)")

        database.execSQL(createLastChangeTable)

        val createSettingsTable = (
                "CREATE TABLE $DatabaseTableSettings" +
                        "(" +
                        "$ColumnId INTEGER PRIMARY KEY," +
                        "$ColumnReloadEnabled INT," +
                        "$ColumnReloadTimeoutMs INT," +
                        "$ColumnNotificationEnabled INT)")

        database.execSQL(createSettingsTable)

        // Add initial service settings
        val values = ContentValues().apply {
            put(ColumnReloadEnabled, true.toInteger())
            put(ColumnReloadTimeoutMs, 15 * 60 * 1000)
            put(ColumnNotificationEnabled, true.toInteger())
        }
        database.insert(DatabaseTableSettings, null, values)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTable")
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTableLastChange")
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTableSettings")
        onCreate(database)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    fun getList(): MutableList<WirelessSocket> {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnUuid,
                ColumnRoomUuid,
                ColumnName,
                ColumnCode,
                ColumnState,
                ColumnLastTriggerDateTime,
                ColumnLastTriggerUser,
                ColumnIsOnServer,
                ColumnServerAction,
                ColumnChangeCount,
                ColumnShowInNotification)

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<WirelessSocket>()
        with(cursor) {
            while (moveToNext()) {
                val uuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnUuid)))
                val roomUuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnRoomUuid)))
                val name = getString(getColumnIndexOrThrow(ColumnName))
                val code = getString(getColumnIndexOrThrow(ColumnCode))
                val state = getInt(getColumnIndexOrThrow(ColumnState)).toBoolean()

                val lastTriggerDateTime = Calendar.getInstance()
                lastTriggerDateTime.timeInMillis = getLong(getColumnIndexOrThrow(ColumnLastTriggerDateTime))
                val lastTriggerUser = getString(getColumnIndexOrThrow(ColumnLastTriggerUser))

                val isOnServer = getInt(getColumnIndexOrThrow(ColumnIsOnServer)).toBoolean()
                val serverAction = ServerDatabaseAction.values()[getInt(getColumnIndexOrThrow(ColumnServerAction))]

                val changeCount = getInt(getColumnIndexOrThrow(ColumnChangeCount))
                val showInNotification = getInt(getColumnIndexOrThrow(ColumnShowInNotification)).toBoolean()

                val entry = WirelessSocket(uuid, roomUuid, name, code, state, lastTriggerDateTime, lastTriggerUser, isOnServer, serverAction, changeCount, showInNotification)

                list.add(entry)
            }
        }

        database.close()
        return list
    }

    fun get(uuid: UUID): WirelessSocket? {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnUuid,
                ColumnRoomUuid,
                ColumnName,
                ColumnCode,
                ColumnState,
                ColumnLastTriggerDateTime,
                ColumnLastTriggerUser,
                ColumnIsOnServer,
                ColumnServerAction,
                ColumnChangeCount,
                ColumnShowInNotification)

        val sortOrder = "$ColumnId ASC"

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(uuid.toString())

        val cursor = database.query(
                DatabaseTable, projection, selection, selectionArgs,
                null, null, sortOrder)

        val list = mutableListOf<WirelessSocket>()
        with(cursor) {
            while (moveToNext()) {
                val roomUuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnRoomUuid)))
                val name = getString(getColumnIndexOrThrow(ColumnName))
                val code = getString(getColumnIndexOrThrow(ColumnCode))
                val state = getInt(getColumnIndexOrThrow(ColumnState)).toBoolean()

                val lastTriggerDateTime = Calendar.getInstance()
                lastTriggerDateTime.timeInMillis = getLong(getColumnIndexOrThrow(ColumnLastTriggerDateTime))
                val lastTriggerUser = getString(getColumnIndexOrThrow(ColumnLastTriggerUser))

                val isOnServer = getInt(getColumnIndexOrThrow(ColumnIsOnServer)).toBoolean()
                val serverAction = ServerDatabaseAction.values()[getInt(getColumnIndexOrThrow(ColumnServerAction))]

                val changeCount = getInt(getColumnIndexOrThrow(ColumnChangeCount))
                val showInNotification = getInt(getColumnIndexOrThrow(ColumnShowInNotification)).toBoolean()

                val entry = WirelessSocket(uuid, roomUuid, name, code, state, lastTriggerDateTime, lastTriggerUser, isOnServer, serverAction, changeCount, showInNotification)

                list.add(entry)
            }
        }

        database.close()
        return list.firstOrNull()
    }

    fun add(entity: WirelessSocket): Long {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnRoomUuid, entity.roomUuid.toString())
            put(ColumnName, entity.name)
            put(ColumnCode, entity.code)
            put(ColumnState, entity.state)
            put(ColumnLastTriggerDateTime, entity.lastTriggerDateTime.timeInMillis)
            put(ColumnLastTriggerUser, entity.lastTriggerUser)
            put(ColumnIsOnServer, entity.isOnServer.toInteger())
            put(ColumnServerAction, entity.serverDatabaseAction.ordinal)
            put(ColumnChangeCount, entity.changeCount)
            put(ColumnShowInNotification, entity.showInNotification.toInteger())
        }

        val database = this.writableDatabase
        val newRowId = database.insert(DatabaseTable, null, values)
        database.close()

        return newRowId
    }

    fun update(entity: WirelessSocket): Int {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnRoomUuid, entity.roomUuid.toString())
            put(ColumnName, entity.name)
            put(ColumnCode, entity.code)
            put(ColumnState, entity.state)
            put(ColumnLastTriggerDateTime, entity.lastTriggerDateTime.timeInMillis)
            put(ColumnLastTriggerUser, entity.lastTriggerUser)
            put(ColumnIsOnServer, entity.isOnServer.toInteger())
            put(ColumnServerAction, entity.serverDatabaseAction.ordinal)
            put(ColumnChangeCount, entity.changeCount)
            put(ColumnShowInNotification, entity.showInNotification.toInteger())
        }

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        val database = this.writableDatabase
        val count = database.update(DatabaseTable, values, selection, selectionArgs)
        database.close()

        return count
    }

    fun delete(entity: WirelessSocket): Int {
        val database = this.writableDatabase

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        val deletedRows = database.delete(DatabaseTable, selection, selectionArgs)

        database.close()
        return deletedRows
    }

    fun getLastChangeDateTime(): Calendar? {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnLastChangeDateTime)

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTableLastChange, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<Calendar>()
        with(cursor) {
            while (moveToNext()) {
                val lastChangeDateTime = Calendar.getInstance()
                lastChangeDateTime.timeInMillis = getLong(getColumnIndexOrThrow(ColumnLastChangeDateTime))
                list.add(lastChangeDateTime)
            }
        }

        database.close()
        return list.firstOrNull()
    }

    fun setLastChangeDateTime(entity: Calendar): Long {
        val values = ContentValues().apply {
            put(ColumnLastChangeDateTime, entity.timeInMillis)
        }

        val database = this.writableDatabase
        val newRowId = database.insert(DatabaseTableLastChange, null, values)
        database.close()

        return newRowId
    }

    fun getServiceSettings(): ServiceSettings? {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnReloadEnabled,
                ColumnReloadTimeoutMs,
                ColumnNotificationEnabled)

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTableSettings, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<ServiceSettings>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(ColumnId))
                val reloadEnabled = getInt(getColumnIndexOrThrow(ColumnReloadEnabled)).toBoolean()
                val reloadTimeoutMs = getInt(getColumnIndexOrThrow(ColumnReloadTimeoutMs))
                val notificationEnabled = getInt(getColumnIndexOrThrow(ColumnNotificationEnabled)).toBoolean()
                list.add(ServiceSettings(id, reloadEnabled, reloadTimeoutMs, notificationEnabled))
            }
        }

        database.close()
        return list.firstOrNull()
    }

    fun setServiceSettings(entity: ServiceSettings): Long {
        val values = ContentValues().apply {
            put(ColumnReloadEnabled, entity.reloadEnabled.toInteger())
            put(ColumnReloadTimeoutMs, entity.reloadTimeoutMs)
            put(ColumnNotificationEnabled, entity.notificationEnabled.toInteger())
        }

        val database = this.writableDatabase
        val newRowId = database.insert(DatabaseTableSettings, null, values)
        database.close()

        return newRowId
    }

    companion object {
        private const val DatabaseVersion = 1
        private const val DatabaseName = "guepardoapps-lucahome.db"

        private const val DatabaseTable = "wireless-socket"

        private const val ColumnId = "_id"
        private const val ColumnUuid = "uuid"
        private const val ColumnRoomUuid = "roomUuid"
        private const val ColumnName = "name"
        private const val ColumnCode = "code"
        private const val ColumnState = "state"
        private const val ColumnLastTriggerDateTime = "lastTriggerDateTime"
        private const val ColumnLastTriggerUser = "lastTriggerUser"
        private const val ColumnIsOnServer = "isOnServer"
        private const val ColumnServerAction = "serverAction"
        private const val ColumnChangeCount = "changeCount"
        private const val ColumnShowInNotification = "showInNotification"

        private const val DatabaseTableLastChange = "wireless-socket-last-change"
        private const val ColumnLastChangeDateTime = "lastChangeDateTime"

        private const val DatabaseTableSettings = "wireless-socket-settings"
        private const val ColumnReloadEnabled = "reloadEnabled"
        private const val ColumnReloadTimeoutMs = "reloadTimeoutMs"
        private const val ColumnNotificationEnabled = "notificationEnabled"
    }
}