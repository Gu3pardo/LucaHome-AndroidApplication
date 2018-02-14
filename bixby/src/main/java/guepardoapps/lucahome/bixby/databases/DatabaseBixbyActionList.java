package guepardoapps.lucahome.bixby.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.bixby.classes.actions.ApplicationAction;
import guepardoapps.lucahome.bixby.classes.actions.BixbyAction;
import guepardoapps.lucahome.bixby.classes.actions.NetworkAction;
import guepardoapps.lucahome.bixby.classes.actions.WirelessSocketAction;
import guepardoapps.lucahome.bixby.classes.actions.WirelessSwitchAction;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseBixbyActionList implements IDatabaseBixbyClassList<BixbyAction> {
    private static final String Tag = DatabaseBixbyActionList.class.getSimpleName();

    private static final String KeyRowId = "_id";
    private static final String KeyActionId = "_actionId";
    private static final String KeyActionType = "_actionType";
    private static final String KeyApplicationAction = "_applicationAction";
    private static final String KeyNetworkAction = "_networkAction";
    private static final String KeyWirelessSocketAction = "_wirelessSocketAction";
    private static final String KeyWirelessSwitchAction = "_wirelessSwitchAction";

    private static final String DatabaseTable = "DatabaseBixbyActionListTable";

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
                    + KeyActionType + " INTEGER, "
                    + KeyApplicationAction + " TEXT NOT NULL, "
                    + KeyNetworkAction + " TEXT NOT NULL, "
                    + KeyWirelessSocketAction + " TEXT NOT NULL, "
                    + KeyWirelessSwitchAction + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseBixbyActionList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseBixbyActionList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull BixbyAction entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyActionId, entry.GetActionId());
        contentValues.put(KeyActionType, entry.GetActionType().ordinal());
        contentValues.put(KeyApplicationAction, entry.GetApplicationAction().GetDatabaseString());
        contentValues.put(KeyNetworkAction, entry.GetNetworkAction().GetDatabaseString());
        contentValues.put(KeyWirelessSocketAction, entry.GetWirelessSocketAction().GetDatabaseString());
        contentValues.put(KeyWirelessSwitchAction, entry.GetWirelessSwitchAction().GetDatabaseString());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull BixbyAction entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyActionId, entry.GetActionId());
        contentValues.put(KeyActionType, entry.GetActionType().ordinal());
        contentValues.put(KeyApplicationAction, entry.GetApplicationAction().GetDatabaseString());
        contentValues.put(KeyNetworkAction, entry.GetNetworkAction().GetDatabaseString());
        contentValues.put(KeyWirelessSocketAction, entry.GetWirelessSocketAction().GetDatabaseString());
        contentValues.put(KeyWirelessSwitchAction, entry.GetWirelessSwitchAction().GetDatabaseString());

        return _database.update(DatabaseTable, contentValues, KeyRowId + "=" + entry.GetId(), null);
    }

    @Override
    public long DeleteEntry(@NonNull BixbyAction entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyRowId + "=" + entry.GetId(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<BixbyAction> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyRowId,
                KeyActionId,
                KeyActionType,
                KeyApplicationAction,
                KeyNetworkAction,
                KeyWirelessSocketAction,
                KeyWirelessSwitchAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<BixbyAction> result = new ArrayList<>();

        int idIndex = cursor.getColumnIndex(KeyRowId);
        int actionIdIndex = cursor.getColumnIndex(KeyActionId);
        int actionTypeIndex = cursor.getColumnIndex(KeyActionType);
        int applicationActionIndex = cursor.getColumnIndex(KeyApplicationAction);
        int networkActionIndex = cursor.getColumnIndex(KeyNetworkAction);
        int wirelessSocketActionIndex = cursor.getColumnIndex(KeyWirelessSocketAction);
        int wirelessSwitchActionIndex = cursor.getColumnIndex(KeyWirelessSwitchAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(idIndex);
            int actionId = cursor.getInt(actionIdIndex);
            int actionTypeInteger = cursor.getInt(actionTypeIndex);
            String applicationActionString = cursor.getString(applicationActionIndex);
            String networkActionString = cursor.getString(networkActionIndex);
            String wirelessSocketActionString = cursor.getString(wirelessSocketActionIndex);
            String wirelessSwitchActionString = cursor.getString(wirelessSwitchActionIndex);

            BixbyAction.ActionType actionType = BixbyAction.ActionType.values()[actionTypeInteger];

            ApplicationAction applicationAction = new ApplicationAction();
            NetworkAction networkAction = new NetworkAction();
            WirelessSocketAction wirelessSocketAction = new WirelessSocketAction();
            WirelessSwitchAction wirelessSwitchAction = new WirelessSwitchAction();

            try {
                applicationAction = new ApplicationAction(applicationActionString);
                networkAction = new NetworkAction(networkActionString);
                wirelessSocketAction = new WirelessSocketAction(wirelessSocketActionString);
                wirelessSwitchAction = new WirelessSwitchAction(wirelessSwitchActionString);
            } catch (Exception ex) {
                Logger.getInstance().Error(Tag, ex.getMessage());
            }

            result.add(new BixbyAction(id, actionId, actionType, applicationAction, networkAction, wirelessSocketAction, wirelessSwitchAction));
        }

        cursor.close();

        return result;
    }
}
