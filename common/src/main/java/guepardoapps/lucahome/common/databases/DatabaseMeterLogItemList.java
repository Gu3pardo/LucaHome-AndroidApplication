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
import guepardoapps.lucahome.common.classes.MeterLogItem;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseMeterLogItemList implements IDatabaseLucaClassList<MeterLogItem> {
    public static final String KeyRoomUuid = "_roomUuid";
    public static final String KeyTypeUuid = "_typeUuid";
    public static final String KeyType = "_type";
    public static final String KeySaveDateTime = "_saveDateTime";
    public static final String KeyMeterId = "_meterId";
    public static final String KeyValue = "_value";
    public static final String KeyImageName = "_imageName";

    private static final String DatabaseTable = "DatabaseMeterLogItemListTable";

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
                    + KeyTypeUuid + " TEXT NOT NULL, "
                    + KeyType + " TEXT NOT NULL, "
                    + KeySaveDateTime + " INTEGER, "
                    + KeyMeterId + " TEXT NOT NULL, "
                    + KeyValue + " REAL, "
                    + KeyImageName + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseMeterLogItemList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseMeterLogItemList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull MeterLogItem entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyTypeUuid, entry.GetTypeUuid().toString());
        contentValues.put(KeyType, entry.GetType());
        contentValues.put(KeySaveDateTime, entry.GetSaveDateTime().getTimeInMillis());
        contentValues.put(KeyMeterId, entry.GetMeterId());
        contentValues.put(KeyValue, entry.GetValue());
        contentValues.put(KeyImageName, entry.GetImageName());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull MeterLogItem entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyTypeUuid, entry.GetTypeUuid().toString());
        contentValues.put(KeyType, entry.GetType());
        contentValues.put(KeySaveDateTime, entry.GetSaveDateTime().getTimeInMillis());
        contentValues.put(KeyMeterId, entry.GetMeterId());
        contentValues.put(KeyValue, entry.GetValue());
        contentValues.put(KeyImageName, entry.GetImageName());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull MeterLogItem entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<MeterLogItem> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyRoomUuid,
                KeyTypeUuid,
                KeyType,
                KeySaveDateTime,
                KeyMeterId,
                KeyValue,
                KeyImageName,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<MeterLogItem> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int roomUuidIndex = cursor.getColumnIndex(KeyRoomUuid);
        int typeUuidIndex = cursor.getColumnIndex(KeyTypeUuid);
        int typeIndex = cursor.getColumnIndex(KeyType);
        int saveDateTimeIndex = cursor.getColumnIndex(KeySaveDateTime);
        int meterIdIndex = cursor.getColumnIndex(KeyMeterId);
        int valueIndex = cursor.getColumnIndex(KeyValue);
        int imageNameIndex = cursor.getColumnIndex(KeyImageName);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String roomUuidString = cursor.getString(roomUuidIndex);
            String typeUuidString = cursor.getString(typeUuidIndex);
            String type = cursor.getString(typeIndex);
            int saveDateTimeInteger = cursor.getInt(saveDateTimeIndex);
            String meterId = cursor.getString(meterIdIndex);
            double value = cursor.getFloat(valueIndex);
            String imageName = cursor.getString(imageNameIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID roomUuid = UUID.fromString(roomUuidString);
            UUID typeUuid = UUID.fromString(typeUuidString);
            Calendar saveDateTime = Calendar.getInstance();
            saveDateTime.setTimeInMillis(saveDateTimeInteger);

            boolean isOnServer = isOnServerInteger == 1;

            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            MeterLogItem entry = new MeterLogItem(uuid, roomUuid, typeUuid, type, saveDateTime, meterId, value, imageName, isOnServer, serverDbAction);
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
