package guepardoapps.lucahome.bixby.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.bixby.classes.requirements.BixbyRequirement;
import guepardoapps.lucahome.bixby.classes.requirements.LightRequirement;
import guepardoapps.lucahome.bixby.classes.requirements.NetworkRequirement;
import guepardoapps.lucahome.bixby.classes.requirements.WirelessSocketRequirement;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseBixbyRequirementList implements IDatabaseBixbyClassList<BixbyRequirement> {
    private static final String Tag = DatabaseBixbyRequirementList.class.getSimpleName();

    private static final String KeyRowId = "_id";
    private static final String KeyActionId = "_actionId";
    private static final String KeyRequirementType = "_requirementType";
    private static final String KeyPuckJsPosition = "_puckJsPosition";
    private static final String KeyLightRequirement = "_lightRequirement";
    private static final String KeyNetworkRequirement = "_networkRequirement";
    private static final String KeyWirelessSocketRequirement = "_wirelessSocketRequirement";

    private static final String DatabaseTable = "DatabaseBixbyRequirementListTable";

    private DatabaseHelper _databaseHelper;
    private final Context _context;
    private SQLiteDatabase _database;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private DatabaseHelper(Context context) {
            super(context, DatabaseName, null, DatabaseVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(" CREATE TABLE " + DatabaseTable + " ( "
                    + KeyRowId + " INTEGER PRIMARY KEY, "
                    + KeyActionId + " INTEGER, "
                    + KeyRequirementType + " INTEGER, "
                    + KeyPuckJsPosition + " TEXT NOT NULL, "
                    + KeyLightRequirement + " TEXT NOT NULL, "
                    + KeyNetworkRequirement + " TEXT NOT NULL, "
                    + KeyWirelessSocketRequirement + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseBixbyRequirementList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseBixbyRequirementList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull BixbyRequirement entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyActionId, entry.GetActionId());
        contentValues.put(KeyRequirementType, entry.GetRequirementType().ordinal());
        contentValues.put(KeyPuckJsPosition, entry.GetPuckJsPosition());
        contentValues.put(KeyLightRequirement, entry.GetLightRequirement().GetDatabaseString());
        contentValues.put(KeyNetworkRequirement, entry.GetNetworkRequirement().GetDatabaseString());
        contentValues.put(KeyWirelessSocketRequirement, entry.GetWirelessSocketRequirement().GetDatabaseString());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull BixbyRequirement entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyActionId, entry.GetActionId());
        contentValues.put(KeyRequirementType, entry.GetRequirementType().ordinal());
        contentValues.put(KeyPuckJsPosition, entry.GetPuckJsPosition());
        contentValues.put(KeyLightRequirement, entry.GetLightRequirement().GetDatabaseString());
        contentValues.put(KeyNetworkRequirement, entry.GetNetworkRequirement().GetDatabaseString());
        contentValues.put(KeyWirelessSocketRequirement, entry.GetWirelessSocketRequirement().GetDatabaseString());

        return _database.update(DatabaseTable, contentValues, KeyRowId + "=" + entry.GetId(), null);
    }

    @Override
    public long DeleteEntry(@NonNull BixbyRequirement deleteEntry) throws SQLException {
        return _database.delete(DatabaseTable, KeyRowId + "=" + deleteEntry.GetId(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<BixbyRequirement> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyRowId,
                KeyActionId,
                KeyRequirementType,
                KeyPuckJsPosition,
                KeyLightRequirement,
                KeyNetworkRequirement,
                KeyWirelessSocketRequirement};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<BixbyRequirement> result = new ArrayList<>();

        int idIndex = cursor.getColumnIndex(KeyRowId);
        int actionIdIndex = cursor.getColumnIndex(KeyActionId);
        int requirementTypeIndex = cursor.getColumnIndex(KeyRequirementType);
        int puckJsPositionIndex = cursor.getColumnIndex(KeyPuckJsPosition);
        int lightRequirementIndex = cursor.getColumnIndex(KeyLightRequirement);
        int networkRequirementIndex = cursor.getColumnIndex(KeyNetworkRequirement);
        int wirelessSocketRequirementIndex = cursor.getColumnIndex(KeyWirelessSocketRequirement);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(idIndex);
            int actionId = cursor.getInt(actionIdIndex);
            int requirementTypeInteger = cursor.getInt(requirementTypeIndex);
            String puckJsPosition = cursor.getString(puckJsPositionIndex);
            String lightRequirementString = cursor.getString(lightRequirementIndex);
            String networkRequirementString = cursor.getString(networkRequirementIndex);
            String wirelessSocketRequirementString = cursor.getString(wirelessSocketRequirementIndex);

            BixbyRequirement.RequirementType requirementType = BixbyRequirement.RequirementType.values()[requirementTypeInteger];

            LightRequirement lightRequirement = new LightRequirement();
            NetworkRequirement networkRequirement = new NetworkRequirement();
            WirelessSocketRequirement wirelessSocketRequirement = new WirelessSocketRequirement();

            try {
                lightRequirement = new LightRequirement(lightRequirementString);
                networkRequirement = new NetworkRequirement(networkRequirementString);
                wirelessSocketRequirement = new WirelessSocketRequirement(wirelessSocketRequirementString);
            } catch (Exception ex) {
                Logger.getInstance().Error(Tag, ex.getMessage());
            }

            result.add(new BixbyRequirement(id, actionId, requirementType, puckJsPosition, lightRequirement, networkRequirement, wirelessSocketRequirement));
        }

        cursor.close();

        return result;
    }
}
