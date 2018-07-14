package guepardoapps.lucahome.common.databases.room

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import guepardoapps.lucahome.common.databases.common.DbHandler
import guepardoapps.lucahome.common.enums.room.RoomType
import guepardoapps.lucahome.common.extensions.common.toInteger
import guepardoapps.lucahome.common.models.room.Room
import java.util.*

class DbRoom(context: Context) : DbHandler<Room>(context, DatabaseTableLastChange, DatabaseTableSettings) {

    override fun onCreate(database: SQLiteDatabase) {
        val createDataTable = (
                "CREATE TABLE $DatabaseTable" +
                        "(" +
                        "$columnId INTEGER PRIMARY KEY," +
                        "$ColumnUuid TEXT," +
                        "$ColumnName TEXT," +
                        "$ColumnType INT" +
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

    override fun getList(): MutableList<Room> {
        val database = this.readableDatabase

        val projection = arrayOf(
                columnId,
                ColumnUuid,
                ColumnName,
                ColumnType)

        val sortOrder = "$columnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<Room>()
        with(cursor) {
            while (moveToNext()) {
                val room = Room()

                room.uuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnUuid)))
                room.name = getString(getColumnIndexOrThrow(ColumnName))
                room.type = RoomType.values()[getInt(getColumnIndexOrThrow(ColumnType))]

                list.add(room)
            }
        }

        return list
    }

    override fun get(uuid: UUID): Room? {
        val list = getList()
        return list.find { value -> value.uuid == uuid }
    }

    override fun add(entity: Room): Long {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnName, entity.name)
            put(ColumnType, entity.type.ordinal)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTable, null, values)
    }

    override fun update(entity: Room): Int {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnName, entity.name)
            put(ColumnType, entity.type.ordinal)
        }

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        val database = this.writableDatabase
        return database.update(DatabaseTable, values, selection, selectionArgs)
    }

    override fun delete(entity: Room): Int {
        val database = this.writableDatabase

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    companion object {
        private const val DatabaseTable = "room"
        private const val DatabaseTableLastChange = "room-last-change"
        private const val DatabaseTableSettings = "room-settings"

        private const val ColumnUuid = "uuid"
        private const val ColumnName = "name"
        private const val ColumnType = "type"
    }
}