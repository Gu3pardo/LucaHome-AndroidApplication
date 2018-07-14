package guepardoapps.lucahome.common.databases.puckjs

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import guepardoapps.lucahome.common.databases.common.DbHandler
import guepardoapps.lucahome.common.extensions.common.toInteger
import guepardoapps.lucahome.common.models.puckjs.PuckJs
import java.util.*

class DbPuckJs(context: Context) : DbHandler<PuckJs>(context, DatabaseTableLastChange, DatabaseTableSettings) {

    override fun onCreate(database: SQLiteDatabase) {
        val createDataTable = (
                "CREATE TABLE $DatabaseTable" +
                        "(" +
                        "$columnId INTEGER PRIMARY KEY," +
                        "$ColumnUuid TEXT," +
                        "$ColumnRoomUuid TEXT," +
                        "$ColumnName TEXT," +
                        "$ColumnMac TEXT)")

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
            put(columnNotificationEnabled, false.toInteger())
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

    override fun getList(): MutableList<PuckJs> {
        val database = this.readableDatabase

        val projection = arrayOf(
                columnId,
                ColumnUuid,
                ColumnRoomUuid,
                ColumnName,
                ColumnMac)

        val sortOrder = "$columnId ASC"

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

    override fun get(uuid: UUID): PuckJs? {
        val list = getList()
        return list.find { value -> value.uuid == uuid }
    }

    override fun add(entity: PuckJs): Long {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnRoomUuid, entity.roomUuid.toString())
            put(ColumnName, entity.name)
            put(ColumnMac, entity.mac)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTable, null, values)
    }

    override fun update(entity: PuckJs): Int {
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

    override fun delete(entity: PuckJs): Int {
        val database = this.writableDatabase

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    companion object {
        private const val DatabaseTable = "puckjs"
        private const val DatabaseTableLastChange = "puckjs-last-change"
        private const val DatabaseTableSettings = "puckjs-settings"

        private const val ColumnUuid = "uuid"
        private const val ColumnRoomUuid = "roomUuid"
        private const val ColumnName = "name"
        private const val ColumnMac = "mac"
    }
}