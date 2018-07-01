package guepardoapps.lucahome.bixby.databases

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import guepardoapps.lucahome.bixby.enums.ActionType
import guepardoapps.lucahome.bixby.enums.RequirementType
import guepardoapps.lucahome.bixby.models.actions.ApplicationAction
import guepardoapps.lucahome.bixby.models.actions.BixbyAction
import guepardoapps.lucahome.bixby.models.actions.WirelessSwitchAction
import guepardoapps.lucahome.bixby.models.requirements.BixbyRequirement
import guepardoapps.lucahome.bixby.models.requirements.LightRequirement
import guepardoapps.lucahome.bixby.models.requirements.PositionRequirement
import guepardoapps.lucahome.bixby.models.shared.NetworkEntity
import guepardoapps.lucahome.bixby.models.shared.WirelessSocketEntity

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

class DbHandler(context: Context, factory: SQLiteDatabase.CursorFactory?)
    : SQLiteOpenHelper(context, DatabaseName, factory, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createTableActions = (
                "CREATE TABLE " + DatabaseTableActions
                        + "("
                        + DbActionColumnId + " INTEGER PRIMARY KEY,"
                        + DbActionColumnActionId + " INTEGER,"
                        + DbActionColumnActionType + " INTEGER,"
                        + DbActionColumnApplicationAction + " TEXT,"
                        + DbActionColumnNetworkAction + " TEXT,"
                        + DbActionColumnWirelessSocketAction + " TEXT,"
                        + DbActionColumnWirelessSwitchAction + " TEXT"
                        + ")")
        database.execSQL(createTableActions)

        val createTableRequirements = (
                "CREATE TABLE " + DatabaseTableRequirements
                        + "("
                        + DbRequirementsColumnId + " INTEGER PRIMARY KEY,"
                        + DbRequirementsColumnActionId + " INTEGER,"
                        + DbRequirementsColumnRequirementType + " INTEGER,"
                        + DbRequirementsColumnPuckJsPosition + " TEXT,"
                        + DbRequirementsColumnLightRequirement + " TEXT,"
                        + DbRequirementsColumnNetworkRequirement + " TEXT,"
                        + DbRequirementsColumnWirelessSocketRequirement + " TEXT"
                        + ")")
        database.execSQL(createTableRequirements)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTableActions")
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTableRequirements")
        onCreate(database)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    fun add(action: BixbyAction): Long {
        val values = ContentValues().apply {
            put(DbActionColumnActionId, action.actionId)
            put(DbActionColumnActionType, action.actionType.ordinal)
            put(DbActionColumnApplicationAction, action.applicationAction.getDatabaseString())
            put(DbActionColumnNetworkAction, action.networkAction.getDatabaseString())
            put(DbActionColumnWirelessSocketAction, action.wirelessSocketAction.getDatabaseString())
            put(DbActionColumnWirelessSwitchAction, action.wirelessSwitchAction.getDatabaseString())
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTableActions, null, values)
    }

    fun add(requirement: BixbyRequirement): Long {
        val values = ContentValues().apply {
            put(DbRequirementsColumnActionId, requirement.actionId)
            put(DbRequirementsColumnRequirementType, requirement.requirementType.ordinal)
            put(DbRequirementsColumnPuckJsPosition, requirement.positionRequirement.getDatabaseString())
            put(DbRequirementsColumnLightRequirement, requirement.lightRequirement.getDatabaseString())
            put(DbRequirementsColumnNetworkRequirement, requirement.networkRequirement.getDatabaseString())
            put(DbRequirementsColumnWirelessSocketRequirement, requirement.wirelessSocketRequirement.getDatabaseString())
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTableRequirements, null, values)
    }

    fun update(action: BixbyAction): Int {
        val values = ContentValues().apply {
            put(DbActionColumnActionId, action.actionId)
            put(DbActionColumnActionType, action.actionType.ordinal)
            put(DbActionColumnApplicationAction, action.applicationAction.getDatabaseString())
            put(DbActionColumnNetworkAction, action.networkAction.getDatabaseString())
            put(DbActionColumnWirelessSocketAction, action.wirelessSocketAction.getDatabaseString())
            put(DbActionColumnWirelessSwitchAction, action.wirelessSwitchAction.getDatabaseString())
        }

        val selection = "$DbActionColumnId LIKE ?"
        val selectionArgs = arrayOf(action.id.toString())

        val database = this.writableDatabase
        return database.update(DatabaseTableActions, values, selection, selectionArgs)
    }

    fun update(requirement: BixbyRequirement): Int {
        val values = ContentValues().apply {
            put(DbRequirementsColumnActionId, requirement.actionId)
            put(DbRequirementsColumnRequirementType, requirement.requirementType.ordinal)
            put(DbRequirementsColumnPuckJsPosition, requirement.positionRequirement.getDatabaseString())
            put(DbRequirementsColumnLightRequirement, requirement.lightRequirement.getDatabaseString())
            put(DbRequirementsColumnNetworkRequirement, requirement.networkRequirement.getDatabaseString())
            put(DbRequirementsColumnWirelessSocketRequirement, requirement.wirelessSocketRequirement.getDatabaseString())
        }

        val selection = "$DbRequirementsColumnId LIKE ?"
        val selectionArgs = arrayOf(requirement.id.toString())

        val database = this.writableDatabase
        return database.update(DatabaseTableRequirements, values, selection, selectionArgs)
    }

    fun delete(action: BixbyAction): Int {
        val database = this.writableDatabase

        val selection = "$DbActionColumnId LIKE ?"
        val selectionArgs = arrayOf(action.id.toString())

        return database.delete(DatabaseTableActions, selection, selectionArgs)
    }

    fun delete(requirement: BixbyRequirement): Int {
        val database = this.writableDatabase

        val selection = "$DbRequirementsColumnId LIKE ?"
        val selectionArgs = arrayOf(requirement.id.toString())

        return database.delete(DatabaseTableRequirements, selection, selectionArgs)
    }

    fun loadActionList(): MutableList<BixbyAction> {
        val database = this.readableDatabase

        val projection = arrayOf(
                DbActionColumnId,
                DbActionColumnActionId,
                DbActionColumnActionType,
                DbActionColumnApplicationAction,
                DbActionColumnNetworkAction,
                DbActionColumnWirelessSocketAction,
                DbActionColumnWirelessSwitchAction
        )

        val sortOrder = "$DbActionColumnId ASC"

        val cursor = database.query(
                DatabaseTableActions, projection, null, null,
                null, null, sortOrder)

        val bixbyActionList = mutableListOf<BixbyAction>()
        with(cursor) {
            while (moveToNext()) {
                val id: Int = getInt(getColumnIndexOrThrow(DbActionColumnId))
                val actionId: Int = getInt(getColumnIndexOrThrow(DbActionColumnActionId))
                val actionType: ActionType = ActionType.values()[getInt(getColumnIndexOrThrow(DbActionColumnActionType))]

                val applicationAction = ApplicationAction()
                applicationAction.parseFromDb(getString(getColumnIndexOrThrow(DbActionColumnApplicationAction)))

                val networkAction = NetworkEntity()
                networkAction.parseFromDb(getString(getColumnIndexOrThrow(DbActionColumnNetworkAction)))

                val wirelessSocketAction = WirelessSocketEntity()
                wirelessSocketAction.parseFromDb(getString(getColumnIndexOrThrow(DbActionColumnWirelessSocketAction)))

                val wirelessSwitchAction = WirelessSwitchAction()
                wirelessSwitchAction.parseFromDb(getString(getColumnIndexOrThrow(DbActionColumnWirelessSwitchAction)))

                val bixbyAction = BixbyAction()
                bixbyAction.id = id
                bixbyAction.actionId = actionId
                bixbyAction.actionType = actionType
                bixbyAction.applicationAction = applicationAction
                bixbyAction.networkAction = networkAction
                bixbyAction.wirelessSocketAction = wirelessSocketAction
                bixbyAction.wirelessSwitchAction = wirelessSwitchAction

                bixbyActionList.add(bixbyAction)
            }
        }

        return bixbyActionList
    }

    fun loadRequirementList(): MutableList<BixbyRequirement> {
        val database = this.readableDatabase

        val projection = arrayOf(
                DbRequirementsColumnId,
                DbRequirementsColumnActionId,
                DbRequirementsColumnRequirementType,
                DbRequirementsColumnPuckJsPosition,
                DbRequirementsColumnLightRequirement,
                DbRequirementsColumnNetworkRequirement,
                DbRequirementsColumnWirelessSocketRequirement
        )

        val sortOrder = "$DbRequirementsColumnId ASC"

        val cursor = database.query(
                DatabaseTableRequirements, projection, null, null,
                null, null, sortOrder)

        val bixbyRequirementList = mutableListOf<BixbyRequirement>()
        with(cursor) {
            while (moveToNext()) {
                val id: Int = getInt(getColumnIndexOrThrow(DbRequirementsColumnId))
                val actionId: Int = getInt(getColumnIndexOrThrow(DbRequirementsColumnActionId))
                val requirementType: RequirementType = RequirementType.values()[getInt(getColumnIndexOrThrow(DbRequirementsColumnRequirementType))]

                val positionRequirement = PositionRequirement()
                positionRequirement.parseFromDb(getString(getColumnIndexOrThrow(DbRequirementsColumnPuckJsPosition)))

                val lightRequirement = LightRequirement()
                lightRequirement.parseFromDb(getString(getColumnIndexOrThrow(DbRequirementsColumnLightRequirement)))

                val networkRequirement = NetworkEntity()
                networkRequirement.parseFromDb(getString(getColumnIndexOrThrow(DbRequirementsColumnNetworkRequirement)))

                val wirelessSocketRequirement = WirelessSocketEntity()
                wirelessSocketRequirement.parseFromDb(getString(getColumnIndexOrThrow(DbRequirementsColumnWirelessSocketRequirement)))

                val bixbyRequirement = BixbyRequirement()
                bixbyRequirement.id = id
                bixbyRequirement.actionId = actionId
                bixbyRequirement.requirementType = requirementType
                bixbyRequirement.positionRequirement = positionRequirement
                bixbyRequirement.lightRequirement = lightRequirement
                bixbyRequirement.networkRequirement = networkRequirement
                bixbyRequirement.wirelessSocketRequirement = wirelessSocketRequirement

                bixbyRequirementList.add(bixbyRequirement)
            }
        }

        return bixbyRequirementList
    }

    companion object {
        private const val DatabaseVersion = 1
        private const val DatabaseName = "guepardoapps-bixby.db"

        private const val DatabaseTableActions = "actions"

        private const val DbActionColumnId = "_id"
        private const val DbActionColumnActionId = "actionId"
        private const val DbActionColumnActionType = "actionType"
        private const val DbActionColumnApplicationAction = "applicationAction"
        private const val DbActionColumnNetworkAction = "networkAction"
        private const val DbActionColumnWirelessSocketAction = "wirelessSocketAction"
        private const val DbActionColumnWirelessSwitchAction = "wirelessSwitchAction"

        private const val DatabaseTableRequirements = "requirements"

        private const val DbRequirementsColumnId = "_id"
        private const val DbRequirementsColumnActionId = "actionId"
        private const val DbRequirementsColumnRequirementType = "requirementType"
        private const val DbRequirementsColumnPuckJsPosition = "positionRequirement"
        private const val DbRequirementsColumnLightRequirement = "lightRequirement"
        private const val DbRequirementsColumnNetworkRequirement = "networkRequirement"
        private const val DbRequirementsColumnWirelessSocketRequirement = "wirelessSocketRequirement"
    }
}