package guepardoapps.lucahome.common.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.WirelessSwitch;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseWirelessSwitchList implements IDatabaseLucaClassList<WirelessSwitch> {
    public static final String KeyRoomUuid = "_roomUuid";
    public static final String KeyName = "_name";
    public static final String KeyRemoteId = "_remoteId";
    public static final String KeyCode = "_keyCode";
    public static final String KeyAction = "_action";
    public static final String KeyLastTriggerDateTime = "_lastTriggerDateTime";
    public static final String KeyLastTriggerUser = "_lastTriggerUser";

    private static final String DatabaseTable = "DatabaseWirelessSwitchListTable";

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
                    + KeyUuid + " TEXT NOT NULL, "
                    + KeyRoomUuid + " TEXT NOT NULL, "
                    + KeyName + " TEXT NOT NULL, "
                    + KeyRemoteId + " INTEGER, "
                    + KeyCode + " TEXT NOT NULL, "
                    + KeyAction + " INTEGER, "
                    + KeyLastTriggerDateTime + " INTEGER, "
                    + KeyLastTriggerUser + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseWirelessSwitchList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseWirelessSwitchList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull WirelessSwitch entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyRemoteId, entry.GetRemoteId());
        contentValues.put(KeyCode, String.valueOf(entry.GetKeyCode()));
        contentValues.put(KeyAction, entry.GetAction() ? 1 : 0);
        contentValues.put(KeyLastTriggerDateTime, entry.GetLastTriggerDateTime().getTimeInMillis());
        contentValues.put(KeyLastTriggerUser, entry.GetLastTriggerUser());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull WirelessSwitch entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyRemoteId, entry.GetRemoteId());
        contentValues.put(KeyCode, String.valueOf(entry.GetKeyCode()));
        contentValues.put(KeyAction, entry.GetAction() ? 1 : 0);
        contentValues.put(KeyLastTriggerDateTime, entry.GetLastTriggerDateTime().getTimeInMillis());
        contentValues.put(KeyLastTriggerUser, entry.GetLastTriggerUser());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull WirelessSwitch entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<WirelessSwitch> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyRoomUuid,
                KeyName,
                KeyRemoteId,
                KeyCode,
                KeyAction,
                KeyLastTriggerDateTime,
                KeyLastTriggerUser,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<WirelessSwitch> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int roomUuidIndex = cursor.getColumnIndex(KeyRoomUuid);
        int nameIndex = cursor.getColumnIndex(KeyName);
        int remoteIdIndex = cursor.getColumnIndex(KeyRemoteId);
        int keyCodeIndex = cursor.getColumnIndex(KeyCode);
        int actionIndex = cursor.getColumnIndex(KeyAction);
        int lastTriggerDateTimeIndex = cursor.getColumnIndex(KeyLastTriggerDateTime);
        int lastTriggerUserIndex = cursor.getColumnIndex(KeyLastTriggerUser);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String roomUuidString = cursor.getString(roomUuidIndex);
            String name = cursor.getString(nameIndex);
            int remoteId = cursor.getInt(remoteIdIndex);
            String keyCodeString = cursor.getString(keyCodeIndex);
            int actionInteger = cursor.getInt(actionIndex);
            int lastTriggerDateTimeInteger = cursor.getInt(lastTriggerDateTimeIndex);
            String lastTriggerUser = cursor.getString(lastTriggerUserIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID roomUuid = UUID.fromString(roomUuidString);

            char keyCode = keyCodeString.charAt(0);
            boolean action = actionInteger == 1;

            Calendar lastTriggerDateTime = Calendar.getInstance();
            lastTriggerDateTime.setTimeInMillis(lastTriggerDateTimeInteger);

            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            boolean isOnServer = isOnServerInteger == 1;
            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            WirelessSwitch entry = new WirelessSwitch(uuid, roomUuid, name, remoteId, keyCode, action, lastTriggerDateTime, lastTriggerUser, isOnServer, serverDbAction);
            result.add(entry);
        }

        cursor.close();

        return result;
    }

    @Override
    public ArrayList<String> GetStringQueryList(boolean distinct, @NonNull String column, String selection, String groupBy, String orderBy) throws SQLException {
        Cursor cursor = _database.query(distinct, DatabaseTable, new String[]{column}, selection, null, groupBy, null, orderBy, null);
        ArrayList<String> result = new ArrayList<>();

        int index = cursor.getColumnIndex(column);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String value = cursor.getString(index);
            result.add(value);
        }
        cursor.close();

        return result;
    }

    @Override
    public ArrayList<Integer> GetIntegerQueryList(boolean distinct, @NonNull String column, String selection, String groupBy, String orderBy) throws SQLException {
        Cursor cursor = _database.query(distinct, DatabaseTable, new String[]{column}, selection, null, groupBy, null, orderBy, null);
        ArrayList<Integer> result = new ArrayList<>();

        int index = cursor.getColumnIndex(column);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int value = cursor.getInt(index);
            result.add(value);
        }
        cursor.close();

        return result;
    }
}
