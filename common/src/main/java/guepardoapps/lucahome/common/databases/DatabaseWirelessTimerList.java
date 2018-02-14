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
import guepardoapps.lucahome.common.classes.WirelessTimer;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseWirelessTimerList implements IDatabaseLucaClassList<WirelessTimer> {
    public static final String KeyName = "_name";
    public static final String KeyDateTime = "_dateTime";
    public static final String KeyIsActive = "_isActive";
    public static final String KeyWirelessSocketUuid = "_wirelessSocketUuid";
    public static final String KeyWirelessSocketAction = "_wirelessSocketAction";
    public static final String KeyWirelessSwitchUuid = "_wirelessSwitchUuid";

    private static final String DatabaseTable = "DatabaseWirelessTimerListTable";

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
                    + KeyName + " TEXT NOT NULL, "
                    + KeyDateTime + " INTEGER, "
                    + KeyIsActive + " INTEGER, "
                    + KeyWirelessSocketUuid + " TEXT NOT NULL, "
                    + KeyWirelessSocketAction + " INTEGER, "
                    + KeyWirelessSwitchUuid + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseWirelessTimerList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseWirelessTimerList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull WirelessTimer entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyDateTime, entry.GetDateTime().getTimeInMillis());
        contentValues.put(KeyIsActive, entry.IsActive() ? 1 : 0);
        contentValues.put(KeyWirelessSocketUuid, entry.GetWirelessSocketUuid().toString());
        contentValues.put(KeyWirelessSocketAction, entry.GetWirelessSocketAction() ? 1 : 0);
        contentValues.put(KeyWirelessSwitchUuid, entry.GetWirelessSwitchUuid().toString());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull WirelessTimer entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyDateTime, entry.GetDateTime().getTimeInMillis());
        contentValues.put(KeyIsActive, entry.IsActive() ? 1 : 0);
        contentValues.put(KeyWirelessSocketUuid, entry.GetWirelessSocketUuid().toString());
        contentValues.put(KeyWirelessSocketAction, entry.GetWirelessSocketAction() ? 1 : 0);
        contentValues.put(KeyWirelessSwitchUuid, entry.GetWirelessSwitchUuid().toString());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull WirelessTimer entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<WirelessTimer> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyName,
                KeyDateTime,
                KeyIsActive,
                KeyWirelessSocketUuid,
                KeyWirelessSocketAction,
                KeyWirelessSwitchUuid,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<WirelessTimer> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int nameIndex = cursor.getColumnIndex(KeyName);
        int dateTimeIndex = cursor.getColumnIndex(KeyDateTime);
        int isActiveIndex = cursor.getColumnIndex(KeyIsActive);
        int wirelessSocketUuidIndex = cursor.getColumnIndex(KeyWirelessSocketUuid);
        int wirelessSocketActionIndex = cursor.getColumnIndex(KeyWirelessSocketAction);
        int wirelessSwitchUuidIndex = cursor.getColumnIndex(KeyWirelessSwitchUuid);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String name = cursor.getString(nameIndex);
            int dateTimeInteger = cursor.getInt(dateTimeIndex);
            int isActiveInteger = cursor.getInt(isActiveIndex);
            String wirelessSocketUuidString = cursor.getString(wirelessSocketUuidIndex);
            int wirelessSocketActionInteger = cursor.getInt(wirelessSocketActionIndex);
            String wirelessSwitchUuidString = cursor.getString(wirelessSwitchUuidIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID wirelessSocketUuid = UUID.fromString(wirelessSocketUuidString);
            UUID wirelessSwitchUuid = UUID.fromString(wirelessSwitchUuidString);

            Calendar dateTime = Calendar.getInstance();
            dateTime.setTimeInMillis(dateTimeInteger);

            boolean isActive = isActiveInteger == 1;
            boolean wirelessSocketAction = wirelessSocketActionInteger == 1;

            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            boolean isOnServer = isOnServerInteger == 1;
            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            WirelessTimer entry = new WirelessTimer(uuid, name, dateTime, isActive, wirelessSocketUuid, wirelessSocketAction, wirelessSwitchUuid, isOnServer, serverDbAction);
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
