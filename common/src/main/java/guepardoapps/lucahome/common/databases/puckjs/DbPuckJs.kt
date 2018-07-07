package guepardoapps.lucahome.common.databases.puckjs

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import guepardoapps.lucahome.common.extensions.common.toBoolean
import guepardoapps.lucahome.common.extensions.common.toInteger
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.puckjs.PuckJs
import java.util.*

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

class DbPuckJs(context: Context, factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DatabaseName, factory, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createDataTable = (
                "CREATE TABLE $DatabaseTable" +
                        "(" +
                        "$ColumnId INTEGER PRIMARY KEY," +
                        "$ColumnUuid TEXT," +
                        "$ColumnRoomUuid TEXT," +
                        "$ColumnName TEXT," +
                        "$ColumnMac TEXT)")

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
                        "$ColumnReloadTimeoutMs INT)")

        database.execSQL(createSettingsTable)

        // Add initial service settings
        val values = ContentValues().apply {
            put(ColumnReloadEnabled, true.toInteger())
            put(ColumnReloadTimeoutMs, 15 * 60 * 1000)
        }
        database.insert(DatabaseTableSettings, null, values)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTable")
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTableSettings")
        onCreate(database)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    fun getList(): MutableList<PuckJs> {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnUuid,
                ColumnRoomUuid,
                ColumnName,
                ColumnMac)

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<PuckJs>()
        with(cursor) {
            while (moveToNext()) {
                val puckJs = PuckJs()

                puckJs.uuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnUuid)))
                puckJs.roomUuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnRoomUuid)))
                puckJs.name = getString(getColumnIndexOrThrow(ColumnName))
                puckJs.mac = getString(getColumnIndexOrThrow(ColumnMac))

                list.add(puckJs)
            }
        }

        return list
    }

    fun get(uuid: UUID): PuckJs? {
        val list = getList()
        return list.find { value -> value.uuid == uuid }
    }

    fun add(entity: PuckJs): Long {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnRoomUuid, entity.roomUuid.toString())
            put(ColumnName, entity.name)
            put(ColumnMac, entity.mac)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTable, null, values)
    }

    fun update(entity: PuckJs): Int {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnRoomUuid, entity.roomUuid.toString())
            put(ColumnName, entity.name)
            put(ColumnMac, entity.mac)
        }

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        val database = this.writableDatabase
        return database.update(DatabaseTable, values, selection, selectionArgs)
    }

    fun delete(entity: PuckJs): Int {
        val database = this.writableDatabase

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
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

        return list.firstOrNull()
    }

    fun setLastChangeDateTime(entity: Calendar): Long {
        val values = ContentValues().apply {
            put(ColumnLastChangeDateTime, entity.timeInMillis)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTableLastChange, null, values)
    }

    fun getServiceSettings(): ServiceSettings? {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnReloadEnabled,
                ColumnReloadTimeoutMs)

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
                list.add(ServiceSettings(id, reloadEnabled, reloadTimeoutMs, false))
            }
        }

        return list.firstOrNull()
    }

    fun setServiceSettings(entity: ServiceSettings): Long {
        val values = ContentValues().apply {
            put(ColumnReloadEnabled, entity.reloadEnabled.toInteger())
            put(ColumnReloadTimeoutMs, entity.reloadTimeoutMs)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTableSettings, null, values)
    }

    companion object {
        private const val DatabaseVersion = 1
        private const val DatabaseName = "guepardoapps-lucahome.db"

        private const val DatabaseTable = "puckjs"

        private const val ColumnId = "_id"
        private const val ColumnUuid = "uuid"
        private const val ColumnRoomUuid = "roomUuid"
        private const val ColumnName = "name"
        private const val ColumnMac = "mac"

        private const val DatabaseTableLastChange = "puckjs-last-change"
        private const val ColumnLastChangeDateTime = "lastChangeDateTime"

        private const val DatabaseTableSettings = "puckjs-settings"
        private const val ColumnReloadEnabled = "reloadEnabled"
        private const val ColumnReloadTimeoutMs = "reloadTimeoutMs"
    }
}