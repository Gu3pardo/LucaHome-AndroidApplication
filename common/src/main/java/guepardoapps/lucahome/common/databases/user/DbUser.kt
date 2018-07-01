package guepardoapps.lucahome.common.databases.user

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import guepardoapps.lucahome.common.enums.UserRole
import guepardoapps.lucahome.common.models.user.User
import java.util.*

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

class DbUser(context: Context, factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DatabaseName, factory, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createTable = (
                "CREATE TABLE " + DatabaseTable
                        + "("
                        + ColumnId + " INTEGER PRIMARY KEY,"
                        + ColumnUuid + " TEXT,"
                        + ColumnName + " TEXT,"
                        + ColumnPassword + " TEXT,"
                        + ColumnRole + " INT"
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

    fun get(): User? {
        val database = this.readableDatabase

        val projection = arrayOf(
                ColumnId,
                ColumnUuid,
                ColumnName,
                ColumnPassword,
                ColumnRole)

        val sortOrder = "$ColumnId ASC"

        val cursor = database.query(
                DatabaseTable, projection, null, null,
                null, null, sortOrder)

        val list = mutableListOf<User>()
        with(cursor) {
            while (moveToNext()) {
                val user = User()

                user.uuid = UUID.fromString(getString(getColumnIndexOrThrow(ColumnUuid)))
                user.name = getString(getColumnIndexOrThrow(ColumnName))
                user.password = getString(getColumnIndexOrThrow(ColumnPassword))
                user.role = UserRole.values()[getInt(getColumnIndexOrThrow(ColumnRole))]

                list.add(user)
            }
        }

        return list.firstOrNull()
    }

    fun add(entity: User): Long {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnName, entity.name)
            put(ColumnPassword, entity.password)
            put(ColumnRole, entity.role.ordinal)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTable, null, values)
    }

    fun update(entity: User): Int {
        val values = ContentValues().apply {
            put(ColumnUuid, entity.uuid.toString())
            put(ColumnName, entity.name)
            put(ColumnPassword, entity.password)
            put(ColumnRole, entity.role.ordinal)
        }

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        val database = this.writableDatabase
        return database.update(DatabaseTable, values, selection, selectionArgs)
    }

    fun delete(entity: User): Int {
        val database = this.writableDatabase

        val selection = "$ColumnUuid LIKE ?"
        val selectionArgs = arrayOf(entity.uuid.toString())

        return database.delete(DatabaseTable, selection, selectionArgs)
    }

    companion object {
        private const val DatabaseVersion = 1
        private const val DatabaseName = "guepardoapps-lucahome.db"
        private const val DatabaseTable = "user"

        private const val ColumnId = "_id"
        private const val ColumnUuid = "uuid"
        private const val ColumnName = "name"
        private const val ColumnPassword = "password"
        private const val ColumnRole = "role"
    }
}