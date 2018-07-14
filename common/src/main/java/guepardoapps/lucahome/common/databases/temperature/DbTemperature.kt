package guepardoapps.lucahome.common.databases.temperature

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import guepardoapps.lucahome.common.databases.common.DbHandler
import guepardoapps.lucahome.common.enums.temperature.TemperatureType
import guepardoapps.lucahome.common.extensions.common.toInteger
import guepardoapps.lucahome.common.models.temperature.Temperature
import java.util.*

class DbTemperature(context: Context) : DbHandler<Temperature>(context, DatabaseTableLastChange, DatabaseTableSettings) {

    override fun onCreate(database: SQLiteDatabase) {
        val createDataTable = (
                "CREATE TABLE $DatabaseTable" +
                        "(" +
                        "$columnId INTEGER PRIMARY KEY," +
                        "$ColumnUuid TEXT," +
                        "$ColumnValue REAL," +
                        "$ColumnArea TEXT," +
                        "$ColumnSensorPath TEXT," +
                        "$ColumnGraphPath TEXT," +
                        "$ColumnType TEXT," +
                        "$ColumnDateTime INT" +
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
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTableSettings")
        onCreate(database)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    override fun getList(): MutableList<Temperature> {
        val database = this.readableDatabase

        val projection = arrayOf(
                columnId,
                ColumnUuid,
                ColumnValue,
                ColumnArea,
                ColumnSensorPath,
                ColumnGraphPath,
                ColumnType,
                ColumnDateTime)

        val sortOrder = "$columnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<Temperature>()
        with(cursor) {
            while (moveToNext()) {
                val temperature = Temperature()

                temperature.uuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnUuid)))
                temperature.value = getDouble(getColumnIndexOrThrow(ColumnValue))
                temperature.area = getString(getColumnIndexOrThrow(ColumnArea))
                temperature.sensorPath = getString(getColumnIndexOrThrow(ColumnSensorPath))
                temperature.graphPath = getString(getColumnIndexOrThrow(ColumnGraphPath))
                temperature.type = TemperatureType.valueOf(getString(getColumnIndexOrThrow(ColumnType)))
                temperature.dateTime.timeInMillis = getLong(getColumnIndexOrThrow(ColumnDateTime))

                list.add(temperature)
            }
        }

        return list
    }

    override fun get(uuid: UUID): Temperature? {
        val list = getList()
        return list.find { value -> value.uuid == uuid }
    }

    override fun add(entity: Temperature): Long {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnValue, entity.value)
            put(ColumnArea, entity.area)
            put(ColumnSensorPath, entity.sensorPath)
            put(ColumnGraphPath, entity.graphPath)
            put(ColumnType, entity.type.toString())
            put(ColumnDateTime, entity.dateTime.timeInMillis)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTable, null, values)
    }

    override fun update(entity: Temperature): Int {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnValue, entity.value)
            put(ColumnArea, entity.area)
            put(ColumnSensorPath, entity.sensorPath)
            put(ColumnGraphPath, entity.graphPath)
            put(ColumnType, entity.type.toString())
            put(ColumnDateTime, entity.dateTime.timeInMillis)
        }

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        val database = this.writableDatabase
        return database.update(DatabaseTable, values, selection, selectionArgs)
    }

    override fun delete(entity: Temperature): Int {
        val database = this.writableDatabase

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    companion object {
        private const val DatabaseTable = "temperature"
        private const val DatabaseTableLastChange = "temperature-last-change"
        private const val DatabaseTableSettings = "temperature-settings"

        private const val ColumnUuid = "uuid"
        private const val ColumnValue = "value"
        private const val ColumnArea = "area"
        private const val ColumnSensorPath = "sensorPath"
        private const val ColumnGraphPath = "graphPath"
        private const val ColumnType = "type"
        private const val ColumnDateTime = "dateTime"
    }
}