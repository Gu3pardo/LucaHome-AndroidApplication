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

import guepardoapps.lucahome.common.classes.Temperature;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseTemperatureList implements IDatabaseLucaClassList<Temperature> {
    public static final String KeyRoomUuid = "_roomUuid";
    public static final String KeyTemperature = "_temperature";
    public static final String KeyDateTime = "_dateTime";
    public static final String KeySensorPath = "_sensorPath";
    public static final String KeyTemperatureType = "_temperatureType";
    public static final String KeyGraphPath = "_graphPath";

    private static final String DatabaseTable = "DatabaseTemperatureListTable";

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
                    + KeyTemperature + " REAL, "
                    + KeyDateTime + " INTEGER, "
                    + KeySensorPath + " TEXT NOT NULL, "
                    + KeyTemperatureType + " INTEGER, "
                    + KeyGraphPath + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseTemperatureList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseTemperatureList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull Temperature entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyTemperature, entry.GetTemperature());
        contentValues.put(KeyDateTime, entry.GetDateTime().getTimeInMillis());
        contentValues.put(KeySensorPath, entry.GetSensorPath());
        contentValues.put(KeyTemperatureType, entry.GetTemperatureType().ordinal());
        contentValues.put(KeyGraphPath, entry.GetGraphPath());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull Temperature entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyRoomUuid, entry.GetRoomUuid().toString());
        contentValues.put(KeyTemperature, entry.GetTemperature());
        contentValues.put(KeyDateTime, entry.GetDateTime().getTimeInMillis());
        contentValues.put(KeySensorPath, entry.GetSensorPath());
        contentValues.put(KeyTemperatureType, entry.GetTemperatureType().ordinal());
        contentValues.put(KeyGraphPath, entry.GetGraphPath());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull Temperature entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<Temperature> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyRoomUuid,
                KeyTemperature,
                KeyDateTime,
                KeySensorPath,
                KeyTemperatureType,
                KeyGraphPath};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<Temperature> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int roomUuidIndex = cursor.getColumnIndex(KeyRoomUuid);
        int temperatureIndex = cursor.getColumnIndex(KeyTemperature);
        int dateTimeIndex = cursor.getColumnIndex(KeyDateTime);
        int sensorPathIndex = cursor.getColumnIndex(KeySensorPath);
        int temperatureTypeIndex = cursor.getColumnIndex(KeyTemperatureType);
        int graphPathIndex = cursor.getColumnIndex(KeyGraphPath);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String roomUuidString = cursor.getString(roomUuidIndex);
            float temperature = cursor.getFloat(temperatureIndex);
            int dateTimeInteger = cursor.getInt(dateTimeIndex);
            String sensorPath = cursor.getString(sensorPathIndex);
            int temperatureTypeInteger = cursor.getInt(temperatureTypeIndex);
            String graphPath = cursor.getString(graphPathIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID roomUuid = UUID.fromString(roomUuidString);

            Calendar dateTime = Calendar.getInstance();
            dateTime.setTimeInMillis(dateTimeInteger);

            Temperature.TemperatureType temperatureType = Temperature.TemperatureType.values()[temperatureTypeInteger];

            Temperature entry = new Temperature(uuid, roomUuid, temperature, dateTime, sensorPath, temperatureType, graphPath);
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
