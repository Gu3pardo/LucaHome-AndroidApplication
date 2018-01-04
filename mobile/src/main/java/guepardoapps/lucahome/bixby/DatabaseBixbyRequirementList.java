package guepardoapps.lucahome.bixby;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;

public class DatabaseBixbyRequirementList {
    private static final String TAG = DatabaseBixbyRequirementList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_ACTION_ID = "_actionId";
    private static final String KEY_REQUIREMENT_TYPE = "_requirementType";
    private static final String KEY_PUCKJS_POSITION = "_puckJsPosition";
    private static final String KEY_LIGHT_REQUIREMENT = "_lightRequirement";
    private static final String KEY_NETWORK_REQUIREMENT = "_networkRequirement";
    private static final String KEY_WIRELESSSOCKET_REQUIREMENT = "_wirelessSocketRequirement";

    private static final String DATABASE_NAME = "DatabaseBixbyRequirementListDb";
    private static final String DATABASE_TABLE = "DatabaseBixbyRequirementListTable";
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper _databaseHelper;
    private final Context _context;
    private SQLiteDatabase _database;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(" CREATE TABLE " + DATABASE_TABLE + " ( "
                    + KEY_ROW_ID + " TEXT NOT NULL, "
                    + KEY_ACTION_ID + " TEXT NOT NULL, "
                    + KEY_REQUIREMENT_TYPE + " TEXT NOT NULL, "
                    + KEY_PUCKJS_POSITION + " TEXT NOT NULL, "
                    + KEY_LIGHT_REQUIREMENT + " TEXT NOT NULL, "
                    + KEY_NETWORK_REQUIREMENT + " TEXT NOT NULL, "
                    + KEY_WIRELESSSOCKET_REQUIREMENT + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseBixbyRequirementList(@NonNull Context context) {
        _context = context;
    }

    public DatabaseBixbyRequirementList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull BixbyRequirement newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_ACTION_ID, newEntry.GetActionId());
        contentValues.put(KEY_REQUIREMENT_TYPE, String.valueOf(newEntry.GetRequirementType().ordinal()));
        contentValues.put(KEY_PUCKJS_POSITION, newEntry.GetPuckJsPosition());
        contentValues.put(KEY_LIGHT_REQUIREMENT, newEntry.GetLightRequirement().GetDatabaseString());
        contentValues.put(KEY_NETWORK_REQUIREMENT, newEntry.GetNetworkRequirement().GetDatabaseString());
        contentValues.put(KEY_WIRELESSSOCKET_REQUIREMENT, newEntry.GetWirelessSocketRequirement().GetDatabaseString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull BixbyRequirement updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_ACTION_ID, updateEntry.GetActionId());
        contentValues.put(KEY_REQUIREMENT_TYPE, String.valueOf(updateEntry.GetRequirementType().ordinal()));
        contentValues.put(KEY_PUCKJS_POSITION, updateEntry.GetPuckJsPosition());
        contentValues.put(KEY_LIGHT_REQUIREMENT, updateEntry.GetLightRequirement().GetDatabaseString());
        contentValues.put(KEY_NETWORK_REQUIREMENT, updateEntry.GetNetworkRequirement().GetDatabaseString());
        contentValues.put(KEY_WIRELESSSOCKET_REQUIREMENT, updateEntry.GetWirelessSocketRequirement().GetDatabaseString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<BixbyRequirement> GetBixbyRequirementList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_ACTION_ID,
                KEY_REQUIREMENT_TYPE,
                KEY_PUCKJS_POSITION,
                KEY_LIGHT_REQUIREMENT,
                KEY_NETWORK_REQUIREMENT,
                KEY_WIRELESSSOCKET_REQUIREMENT};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<BixbyRequirement> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int actionIdIndex = cursor.getColumnIndex(KEY_ACTION_ID);
        int requirementTypeIndex = cursor.getColumnIndex(KEY_REQUIREMENT_TYPE);
        int puckJsPositionIndex = cursor.getColumnIndex(KEY_PUCKJS_POSITION);
        int lightRequirementIndex = cursor.getColumnIndex(KEY_LIGHT_REQUIREMENT);
        int networkRequirementIndex = cursor.getColumnIndex(KEY_NETWORK_REQUIREMENT);
        int wirelessSocketRequirementIndex = cursor.getColumnIndex(KEY_WIRELESSSOCKET_REQUIREMENT);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String actionIdString = cursor.getString(actionIdIndex);
            String requirementTypeString = cursor.getString(requirementTypeIndex);
            String puckJsPosition = cursor.getString(puckJsPositionIndex);
            String lightRequirementString = cursor.getString(lightRequirementIndex);
            String networkRequirementString = cursor.getString(networkRequirementIndex);
            String wirelessSocketRequirementString = cursor.getString(wirelessSocketRequirementIndex);

            int id = -1;
            int actionId = -1;

            BixbyRequirement.RequirementType requirementType = BixbyRequirement.RequirementType.Null;

            LightRequirement lightRequirement = new LightRequirement();
            NetworkRequirement networkRequirement = new NetworkRequirement();
            WirelessSocketRequirement wirelessSocketRequirement = new WirelessSocketRequirement();

            try {
                id = Integer.parseInt(idString);
                actionId = Integer.parseInt(actionIdString);

                requirementType = BixbyRequirement.RequirementType.values()[Integer.parseInt(requirementTypeString)];

                lightRequirement = new LightRequirement(lightRequirementString);
                networkRequirement = new NetworkRequirement(networkRequirementString);
                wirelessSocketRequirement = new WirelessSocketRequirement(wirelessSocketRequirementString);
            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }

            BixbyRequirement entry = new BixbyRequirement(id, actionId, requirementType, puckJsPosition, lightRequirement, networkRequirement, wirelessSocketRequirement);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull BixbyRequirement deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
