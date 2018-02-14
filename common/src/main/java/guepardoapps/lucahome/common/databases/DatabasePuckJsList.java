package guepardoapps.lucahome.common.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.PuckJs;

@SuppressWarnings({"WeakerAccess"})
public class DatabasePuckJsList implements IDatabaseLucaClassList<PuckJs> {
    public static final String KeyRoomUuid = "_roomUuid";
    public static final String KeyName = "_name";
    public static final String KeyMac = "_mac";
    public static final String KeyBatteryLevel = "_batteryLevel";
    public static final String KeyLightValue = "_lightValue";

    private static final String DatabaseTable = "DatabasePuckJsListTable";

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
                    + KeyMac + " TEXT NOT NULL, "
                    + KeyBatteryLevel + " INTEGER, "
                    + KeyLightValue + " INTEGER, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabasePuckJsList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabasePuckJsList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull PuckJs entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyMac, entry.GetMac());
        contentValues.put(KeyBatteryLevel, entry.GetBatteryLevel());
        contentValues.put(KeyLightValue, entry.GetLightValue());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull PuckJs entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyMac, entry.GetMac());
        contentValues.put(KeyBatteryLevel, entry.GetBatteryLevel());
        contentValues.put(KeyLightValue, entry.GetLightValue());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull PuckJs entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<PuckJs> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyRoomUuid,
                KeyName,
                KeyMac,
                KeyBatteryLevel,
                KeyLightValue,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<PuckJs> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int roomUuidIndex = cursor.getColumnIndex(KeyRoomUuid);
        int nameIndex = cursor.getColumnIndex(KeyName);
        int macIndex = cursor.getColumnIndex(KeyMac);
        int batteryLevelIndex = cursor.getColumnIndex(KeyBatteryLevel);
        int lightValueIndex = cursor.getColumnIndex(KeyLightValue);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String roomUuidString = cursor.getString(roomUuidIndex);
            String name = cursor.getString(nameIndex);
            String mac = cursor.getString(macIndex);
            int batteryLevel = cursor.getInt(batteryLevelIndex);
            int lightValue = cursor.getInt(lightValueIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID roomUuid = UUID.fromString(roomUuidString);

            boolean isOnServer = isOnServerInteger == 1;
            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            PuckJs entry = new PuckJs(uuid, roomUuid, name, mac, batteryLevel, lightValue, isOnServer, serverDbAction);
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
