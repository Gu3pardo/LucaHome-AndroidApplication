package guepardoapps.lucahome.common.databases.wirelesssocket

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import guepardoapps.lucahome.common.databases.common.DbHandler
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.extensions.common.toBoolean
import guepardoapps.lucahome.common.extensions.common.toInteger
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import java.util.*

class DbWirelessSocket(context: Context) : DbHandler<WirelessSocket>(context, DatabaseTableLastChange, DatabaseTableSettings) {

    override fun onCreate(database: SQLiteDatabase) {
        val createDataTable = (
                "CREATE TABLE $DatabaseTable" +
                        "(" +
                        "$columnId INTEGER PRIMARY KEY," +
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
                        "$ColumnShowInNotification INT" +
                        ")")

        database.execSQL(createDataTable)

        val createLastChangeTable = (
                "CREATE TABLE $DatabaseTableLastChange" +
                        "(" +
                        "$columnId INTEGER PRIMARY KEY," +
                        "$columnLastChangeDateTime INT" +
                        ")")

        database.execSQL(createLastChangeTable)

        val createSettingsTable = (
                "CREATE TABLE $DatabaseTableSettings" +
                        "(" +
                        "$columnId INTEGER PRIMARY KEY," +
                        "$columnReloadEnabled INT," +
                        "$columnReloadTimeoutMs INT," +
                        "$columnNotificationEnabled INT" +
                        ")")

        database.execSQL(createSettingsTable)

        // Add initial service settings
        val values = ContentValues().apply {
            put(columnReloadEnabled, true.toInteger())
            put(columnReloadTimeoutMs, 15 * 60 * 1000)
            put(columnNotificationEnabled, true.toInteger())
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

    override fun getList(): MutableList<WirelessSocket> {
        val database = this.readableDatabase

        val projection = arrayOf(
                columnId,
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

        val sortOrder = "$columnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<WirelessSocket>()
        with(cursor) {
            while (moveToNext()) {
                val wirelessSocket = WirelessSocket()

                wirelessSocket.uuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnUuid)))
                wirelessSocket.roomUuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnRoomUuid)))
                wirelessSocket.name = getString(getColumnIndexOrThrow(ColumnName))
                wirelessSocket.code = getString(getColumnIndexOrThrow(ColumnCode))
                wirelessSocket.state = getInt(getColumnIndexOrThrow(ColumnState)).toBoolean()

                wirelessSocket.lastTriggerDateTime.timeInMillis = getLong(getColumnIndexOrThrow(ColumnLastTriggerDateTime))
                wirelessSocket.lastTriggerUser = getString(getColumnIndexOrThrow(ColumnLastTriggerUser))

                wirelessSocket.isOnServer = getInt(getColumnIndexOrThrow(ColumnIsOnServer)).toBoolean()
                wirelessSocket.serverDatabaseAction = ServerDatabaseAction.values()[getInt(getColumnIndexOrThrow(ColumnServerAction))]

                wirelessSocket.changeCount = getInt(getColumnIndexOrThrow(ColumnChangeCount))
                wirelessSocket.showInNotification = getInt(getColumnIndexOrThrow(ColumnShowInNotification)).toBoolean()

                list.add(wirelessSocket)
            }
        }

        return list
    }

    override fun get(uuid: UUID): WirelessSocket? {
        val list = getList()
        return list.find { value -> value.uuid == uuid }
    }

    override fun add(entity: WirelessSocket): Long {
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
        return database.insert(DatabaseTable, null, values)
    }

    override fun update(entity: WirelessSocket): Int {
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
        return database.update(DatabaseTable, values, selection, selectionArgs)
    }

    override fun delete(entity: WirelessSocket): Int {
        val database = this.writableDatabase

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    companion object {
        private const val DatabaseTable = "wireless-socket"
        private const val DatabaseTableLastChange = "wireless-socket-last-change"
        private const val DatabaseTableSettings = "wireless-socket-settings"

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
    }
}