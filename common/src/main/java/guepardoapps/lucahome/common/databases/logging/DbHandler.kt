package guepardoapps.lucahome.common.databases.logging

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import java.sql.Date

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

class DbHandler(context: Context, factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DatabaseName, factory, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createTable = (
                "CREATE TABLE $DatabaseTable"
                        + "("
                        + "$ColumnId INTEGER PRIMARY KEY,"
                        + "$ColumnDateTime INTEGER,"
                        + "$ColumnSeverity  INTEGER,"
                        + "$ColumnTag  TEXT,"
                        + "$ColumnDescription  TEXT"
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

    fun addLog(dbLog: DbLog): Long {
        val values = ContentValues().apply {
            put(ColumnDateTime, dbLog.dateTime.toString())
            put(ColumnSeverity, dbLog.severity.ordinal.toString())
            put(ColumnTag, dbLog.tag)
            put(ColumnDescription, dbLog.description)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTable, null, values)
    }

    fun updateLog(dbLog: DbLog): Int {
        val values = ContentValues().apply {
            put(ColumnDateTime, dbLog.dateTime.toString())
            put(ColumnSeverity, dbLog.severity.ordinal.toString())
            put(ColumnTag, dbLog.tag)
            put(ColumnDescription, dbLog.description)
        }

        val selection = "$ColumnId LIKE ?"
        val selectionArgs = arrayOf(dbLog.id.toString())

        val database = this.writableDatabase
        return database.update(DatabaseTable, values, selection, selectionArgs)
    }

    fun deleteLogById(id: Int): Int {
        val database = this.writableDatabase

        val selection = "$ColumnId LIKE ?"
        val selectionArgs = arrayOf(id.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    fun deleteLogBySeverity(severity: Severity): Int {
        val database = this.writableDatabase

        val selection = "$ColumnSeverity LIKE ?"
        val selectionArgs = arrayOf(severity.ordinal.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    fun deleteLogByTag(tag: String): Int {
        val database = this.writableDatabase

        val selection = "$ColumnTag LIKE ?"
        val selectionArgs = arrayOf(tag)

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    fun findLogById(id: Int): MutableList<DbLog> {
        val database = this.readableDatabase

        val projection = arrayOf(ColumnId, ColumnDateTime, ColumnSeverity, ColumnTag, ColumnDescription)

        val selection = "$ColumnId = ?"
        val selectionArgs = arrayOf(id.toString())

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, selection, selectionArgs,
                null, null, sortOrder)

        val dbLogList = mutableListOf<DbLog>()
        with(cursor) {
            while (moveToNext()) {
                val dateTimeString = getString(getColumnIndexOrThrow(ColumnDateTime))
                val severityInteger = getInt(getColumnIndexOrThrow(ColumnSeverity))
                val tag = getString(getColumnIndexOrThrow(ColumnTag))
                val description = getString(getColumnIndexOrThrow(ColumnDescription))

                val dateTime = Date.valueOf(dateTimeString)
                val severity = Severity.values()[severityInteger]

                val dbLog = DbLog(id, dateTime, severity, tag, description)

                dbLogList.add(dbLog)
            }
        }

        return dbLogList
    }

    fun findLogBySeverity(severity: Severity): MutableList<DbLog> {
        val database = this.readableDatabase

        val projection = arrayOf(ColumnId, ColumnDateTime, ColumnSeverity, ColumnTag, ColumnDescription)

        val selection = "$ColumnSeverity = ?"
        val selectionArgs = arrayOf(severity.ordinal.toString())

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, selection, selectionArgs,
                null, null, sortOrder)

        val dbLogList = mutableListOf<DbLog>()
        with(cursor) {
            while (moveToNext()) {
                val itemId = getInt(getColumnIndexOrThrow(ColumnId))
                val dateTimeString = getString(getColumnIndexOrThrow(ColumnDateTime))
                val tag = getString(getColumnIndexOrThrow(ColumnTag))
                val description = getString(getColumnIndexOrThrow(ColumnDescription))

                val dateTime = Date.valueOf(dateTimeString)

                val dbLog = DbLog(itemId, dateTime, severity, tag, description)

                dbLogList.add(dbLog)
            }
        }

        return dbLogList
    }

    fun findLogByTag(tag: String): MutableList<DbLog> {
        val database = this.readableDatabase

        val projection = arrayOf(ColumnId, ColumnDateTime, ColumnSeverity, ColumnTag, ColumnDescription)

        val selection = "$ColumnTag = ?"
        val selectionArgs = arrayOf(tag)

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, selection, selectionArgs,
                null, null, sortOrder)

        val dbLogList = mutableListOf<DbLog>()
        with(cursor) {
            while (moveToNext()) {
                val itemId = getInt(getColumnIndexOrThrow(ColumnId))
                val dateTimeString = getString(getColumnIndexOrThrow(ColumnDateTime))
                val severityInteger = getInt(getColumnIndexOrThrow(ColumnSeverity))
                val description = getString(getColumnIndexOrThrow(ColumnDescription))

                val dateTime = Date.valueOf(dateTimeString)
                val severity = Severity.values()[severityInteger]

                val dbLog = DbLog(itemId, dateTime, severity, tag, description)

                dbLogList.add(dbLog)
            }
        }

        return dbLogList
    }

    companion object {
        private const val DatabaseVersion = 1
        private const val DatabaseName = "guepardoapps-openweather.db"
        private const val DatabaseTable = "logging"

        private const val ColumnId = "_id"
        private const val ColumnDateTime = "dateTime"
        private const val ColumnSeverity = "severity"
        private const val ColumnTag = "tag"
        private const val ColumnDescription = "description"
    }
}