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

@SuppressWarnings({"WeakerAccess", "unused"})
public class DatabaseBixbyActionList {
    private static final String TAG = DatabaseBixbyActionList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_ACTION_ID = "_actionId";
    private static final String KEY_ACTION_TYPE = "_actionType";
    private static final String KEY_APPLICATION_ACTION = "_applicationAction";
    private static final String KEY_NETWORK_ACTION = "_networkAction";
    private static final String KEY_WIRELESSSOCKET_ACTION = "_wirelessSocketAction";

    private static final String DATABASE_NAME = "DatabaseBixbyActionListDb";
    private static final String DATABASE_TABLE = "DatabaseBixbyActionListTable";
    private static final int DATABASE_VERSION = 2;

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
                    + KEY_ACTION_TYPE + " TEXT NOT NULL, "
                    + KEY_APPLICATION_ACTION + " TEXT NOT NULL, "
                    + KEY_NETWORK_ACTION + " TEXT NOT NULL, "
                    + KEY_WIRELESSSOCKET_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseBixbyActionList(@NonNull Context context) {
        _context = context;
    }

    public DatabaseBixbyActionList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull BixbyAction newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_ACTION_ID, newEntry.GetActionId());
        contentValues.put(KEY_ACTION_TYPE, String.valueOf(newEntry.GetActionType().ordinal()));
        contentValues.put(KEY_APPLICATION_ACTION, newEntry.GetApplicationAction().GetDatabaseString());
        contentValues.put(KEY_NETWORK_ACTION, newEntry.GetNetworkAction().GetDatabaseString());
        contentValues.put(KEY_WIRELESSSOCKET_ACTION, newEntry.GetWirelessSocketAction().GetDatabaseString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull BixbyAction updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_ACTION_ID, updateEntry.GetActionId());
        contentValues.put(KEY_ACTION_TYPE, String.valueOf(updateEntry.GetActionType().ordinal()));
        contentValues.put(KEY_APPLICATION_ACTION, updateEntry.GetApplicationAction().GetDatabaseString());
        contentValues.put(KEY_NETWORK_ACTION, updateEntry.GetNetworkAction().GetDatabaseString());
        contentValues.put(KEY_WIRELESSSOCKET_ACTION, updateEntry.GetWirelessSocketAction().GetDatabaseString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<BixbyAction> GetBixbyActionList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_ACTION_ID,
                KEY_ACTION_TYPE,
                KEY_APPLICATION_ACTION,
                KEY_NETWORK_ACTION,
                KEY_WIRELESSSOCKET_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<BixbyAction> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int actionIdIndex = cursor.getColumnIndex(KEY_ACTION_ID);
        int actionTypeIndex = cursor.getColumnIndex(KEY_ACTION_TYPE);
        int applicationActionIndex = cursor.getColumnIndex(KEY_APPLICATION_ACTION);
        int networkActionIndex = cursor.getColumnIndex(KEY_NETWORK_ACTION);
        int wirelessSocketActionIndex = cursor.getColumnIndex(KEY_WIRELESSSOCKET_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String actionIdString = cursor.getString(actionIdIndex);
            String actionTypeString = cursor.getString(actionTypeIndex);
            String applicationActionString = cursor.getString(applicationActionIndex);
            String networkActionString = cursor.getString(networkActionIndex);
            String wirelessSocketActionString = cursor.getString(wirelessSocketActionIndex);

            int id = -1;
            int actionId = -1;

            BixbyAction.ActionType actionType = BixbyAction.ActionType.Null;

            ApplicationAction applicationAction = new ApplicationAction(applicationActionString);
            NetworkAction networkAction = new NetworkAction();
            WirelessSocketAction wirelessSocketAction = new WirelessSocketAction();

            try {
                id = Integer.parseInt(idString);
                actionId = Integer.parseInt(actionIdString);

                actionType = BixbyAction.ActionType.values()[Integer.parseInt(actionTypeString)];

                networkAction = new NetworkAction(networkActionString);
                wirelessSocketAction = new WirelessSocketAction(wirelessSocketActionString);
            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }

            BixbyAction entry = new BixbyAction(id, actionId, actionType, applicationAction, networkAction, wirelessSocketAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull BixbyAction deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
