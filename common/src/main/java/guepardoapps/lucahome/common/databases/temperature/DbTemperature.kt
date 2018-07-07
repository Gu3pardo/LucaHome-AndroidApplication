package guepardoapps.lucahome.common.databases.temperature

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import guepardoapps.lucahome.common.enums.temperature.TemperatureType
import guepardoapps.lucahome.common.extensions.common.toBoolean
import guepardoapps.lucahome.common.extensions.common.toInteger
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.temperature.Temperature
import java.util.*

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

class DbTemperature(context: Context, factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DatabaseName, factory, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createDataTable = (
                "CREATE TABLE $DatabaseTable" +
                        "(" +
                        "$ColumnId INTEGER PRIMARY KEY," +
                        "$ColumnUuid TEXT," +
                        "$ColumnValue REAL," +
                        "$ColumnArea TEXT," +
                        "$ColumnSensorPath TEXT," +
                        "$ColumnGraphPath TEXT," +
                        "$ColumnType TEXT," +
                        "$ColumnDateTime INT)")

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
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTableSettings")
        onCreate(database)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    fun getList(): MutableList<Temperature> {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnUuid,
                ColumnValue,
                ColumnArea,
                ColumnSensorPath,
                ColumnGraphPath,
                ColumnType,
                ColumnDateTime)

        val sortOrder = "$ColumnId ASC"

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

    fun get(uuid: UUID): Temperature? {
        val list = getList()
        return list.find { value -> value.uuid == uuid }
    }

    fun add(entity: Temperature): Long {
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

    fun update(entity: Temperature): Int {
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

    fun delete(entity: Temperature): Int {
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

        return list.firstOrNull()
    }

    fun setServiceSettings(entity: ServiceSettings): Long {
        val values = ContentValues().apply {
            put(ColumnReloadEnabled, entity.reloadEnabled.toInteger())
            put(ColumnReloadTimeoutMs, entity.reloadTimeoutMs)
            put(ColumnNotificationEnabled, entity.notificationEnabled.toInteger())
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTableSettings, null, values)
    }

    companion object {
        private const val DatabaseVersion = 1
        private const val DatabaseName = "guepardoapps-lucahome.db"

        private const val DatabaseTable = "temperature"

        private const val ColumnId = "_id"
        private const val ColumnUuid = "uuid"
        private const val ColumnValue = "value"
        private const val ColumnArea = "area"
        private const val ColumnSensorPath = "sensorPath"
        private const val ColumnGraphPath = "graphPath"
        private const val ColumnType = "type"
        private const val ColumnDateTime = "dateTime"

        private const val DatabaseTableLastChange = "temperature-last-change"
        private const val ColumnLastChangeDateTime = "lastChangeDateTime"

        private const val DatabaseTableSettings = "temperature-settings"
        private const val ColumnReloadEnabled = "reloadEnabled"
        private const val ColumnReloadTimeoutMs = "reloadTimeoutMs"
        private const val ColumnNotificationEnabled = "notificationEnabled"
    }
}