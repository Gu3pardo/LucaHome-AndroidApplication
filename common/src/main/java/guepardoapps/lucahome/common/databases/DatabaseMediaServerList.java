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
import guepardoapps.lucahome.common.classes.MediaServer;
import guepardoapps.lucahome.common.classes.mediaserver.BundledData;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseMediaServerList implements IDatabaseLucaClassList<MediaServer> {
    private static final String Tag = DatabaseMediaServerList.class.getSimpleName();

    public static final String KeyRoomUuid = "_roomUuid";
    public static final String KeyIp = "_ip";
    public static final String KeyIsSleepingServer = "_isSleepingServer";
    public static final String KeyWirelessSocketUuid = "_wirelessSocketUuid";
    public static final String KeyIsActive = "_isActive";
    public static final String KeyBundledData = "_bundledData";

    private static final String DatabaseTable = "DatabaseMediaServerListTable";

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
                    + KeyIp + " TEXT NOT NULL, "
                    + KeyIsSleepingServer + " INTEGER, "
                    + KeyWirelessSocketUuid + " TEXT NOT NULL, "
                    + KeyIsActive + " INTEGER, "
                    + KeyBundledData + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseMediaServerList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseMediaServerList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull MediaServer entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyIp, entry.GetIp());
        contentValues.put(KeyIsSleepingServer, entry.IsSleepingServer() ? 1 : 0);
        contentValues.put(KeyWirelessSocketUuid, entry.GetWirelessSocketUuid().toString());
        contentValues.put(KeyIsActive, entry.IsActive() ? 1 : 0);
        contentValues.put(KeyBundledData, entry.GetBundledData().GetCommunicationString());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull MediaServer entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyIp, entry.GetIp());
        contentValues.put(KeyIsSleepingServer, entry.IsSleepingServer() ? 1 : 0);
        contentValues.put(KeyWirelessSocketUuid, entry.GetWirelessSocketUuid().toString());
        contentValues.put(KeyIsActive, entry.IsActive() ? 1 : 0);
        contentValues.put(KeyBundledData, entry.GetBundledData().GetCommunicationString());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull MediaServer entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<MediaServer> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyRoomUuid,
                KeyIp,
                KeyIsSleepingServer,
                KeyWirelessSocketUuid,
                KeyIsActive,
                KeyBundledData,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<MediaServer> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int roomUuidIndex = cursor.getColumnIndex(KeyRoomUuid);
        int wirelessSocketUuidIndex = cursor.getColumnIndex(KeyWirelessSocketUuid);
        int ipIndex = cursor.getColumnIndex(KeyIp);
        int isSleepingServerIndex = cursor.getColumnIndex(KeyIsSleepingServer);
        int isActiveIndex = cursor.getColumnIndex(KeyIsActive);
        int bundleDataIndex = cursor.getColumnIndex(KeyBundledData);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String roomUuidString = cursor.getString(roomUuidIndex);
            String wirelessSocketUuidString = cursor.getString(wirelessSocketUuidIndex);
            String ip = cursor.getString(ipIndex);
            int isSleepingServerInteger = cursor.getInt(isSleepingServerIndex);
            int isActiveInteger = cursor.getInt(isActiveIndex);
            String bundleDataString = cursor.getString(bundleDataIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID roomUuid = UUID.fromString(roomUuidString);
            UUID wirelessSocketUuid = UUID.fromString(wirelessSocketUuidString);

            boolean isSleepingServer = isSleepingServerInteger == 1;
            boolean isActive = isActiveInteger == 1;

            BundledData bundledData = new BundledData();
            try {
                bundledData.ParseCommunicationString(bundleDataString);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
            }

            boolean isOnServer = isOnServerInteger == 1;

            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            MediaServer entry = new MediaServer(uuid, roomUuid, ip, isSleepingServer, wirelessSocketUuid, isActive, bundledData, isOnServer, serverDbAction);
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
