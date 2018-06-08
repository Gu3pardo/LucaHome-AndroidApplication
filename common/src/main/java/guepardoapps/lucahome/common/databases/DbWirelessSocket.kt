package guepardoapps.lucahome.common.databases

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import java.sql.Date

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

class DbWirelessSocket(context: Context, factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DatabaseName, factory, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createTable = (
                "CREATE TABLE " + DatabaseTable
                        + "("
                        + ColumnId + " INTEGER PRIMARY KEY,"
                        + ColumnUuid + " TEXT,"
                        + ColumnRoomUuid + " TEXT,"
                        + ColumnName + " TEXT,"
                        + ColumnCode + " TEXT,"
                        + ColumnState + " INT,"
                        + ColumnLastTriggerDateTime + " INT,"
                        + ColumnLastTriggerUser + " TEXT,"
                        + ColumnIsOnServer + " INT,"
                        + ColumnServerAction + " INT"
                        + ")")
        database.execSQL(createTable)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTable")
        onCreate(database)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    fun add(entity: T): Long {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid)
            put(ColumnRoomUuid, entity.roomUuid)
            put(ColumnName, entity.name)
            put(ColumnCode, entity.code)
            put(ColumnState, entity.state)
            put(ColumnLastTriggerDateTime, entity.lastTriggerDateTime)
            put(ColumnLastTriggerUser, entity.lastTriggerUser)
            put(ColumnIsOnServer, entity.isOnServer)
            put(ColumnServerAction, entity.serverAction)
        }

        val database = this.writableDatabase
        val newRowId = database.insert(DatabaseTable, null, values)
        database.close()

        return newRowId
    }

    fun update(entity: T): Int {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid)
            put(ColumnRoomUuid, entity.roomUuid)
            put(ColumnName, entity.name)
            put(ColumnCode, entity.code)
            put(ColumnState, entity.state)
            put(ColumnLastTriggerDateTime, entity.lastTriggerDateTime)
            put(ColumnLastTriggerUser, entity.lastTriggerUser)
            put(ColumnIsOnServer, entity.isOnServer)
            put(ColumnServerAction, entity.serverAction)
        }

        val selection = "$ColumnId LIKE ?"
        val selectionArgs = arrayOf(entity.id.toString())

        val database = this.writableDatabase
        val count = database.update(DatabaseTable, values, selection, selectionArgs)
        database.close()

        return count
    }

    fun delete(entity: T): Int {
        val database = this.writableDatabase

        val selection = "$ColumnId LIKE ?"
        val selectionArgs = arrayOf(entity.id.toString())

        val deletedRows = database.delete(DatabaseTable, selection, selectionArgs)

        database.close()
        return deletedRows
    }

    fun loadList(): MutableList<T> {
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
                ColumnServerAction)

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<T>()
        with(cursor) {
            while (moveToNext()) {
                val dateTimeString = getString(getColumnIndexOrThrow(ColumnDateTime))
                val severityInteger = getInt(getColumnIndexOrThrow(ColumnSeverity))
                val tag = getString(getColumnIndexOrThrow(ColumnTag))
                val description = getString(getColumnIndexOrThrow(ColumnDescription))

                val dateTime = Date.valueOf(dateTimeString)
                val severity = Severity.values()[severityInteger]

                val entry = Entry(id, dateTime, severity, tag, description)

                list.add(entry)
            }
        }

        database.close()
        return list
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
    }
}