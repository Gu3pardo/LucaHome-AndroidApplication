package guepardoapps.lucahome.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabaseWirelessSocketList {
    private static final String TAG = DatabaseWirelessSocketList.class.getSimpleName();
    private Logger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_AREA = "_area";
    private static final String KEY_CODE = "_code";
    private static final String KEY_IS_ACTIVATED = "_isActivated";
    private static final String KEY_LAST_TRIGGER_YEAR = "_lastTriggerYear";
    private static final String KEY_LAST_TRIGGER_MONTH = "_lastTriggerMonth";
    private static final String KEY_LAST_TRIGGER_DAY = "_lastTriggerDay";
    private static final String KEY_LAST_TRIGGER_HOUR = "_lastTriggerHour";
    private static final String KEY_LAST_TRIGGER_MINUTE = "_lastTriggerMinute";
    private static final String KEY_LAST_TRIGGER_USER = "_lastTriggerUser";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabaseSocketListDb";
    private static final String DATABASE_TABLE = "DatabaseSocketListTable";
    private static final int DATABASE_VERSION = 3;

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
                    + KEY_NAME + " TEXT NOT NULL, "
                    + KEY_AREA + " TEXT NOT NULL, "
                    + KEY_CODE + " TEXT NOT NULL, "
                    + KEY_IS_ACTIVATED + " TEXT NOT NULL, "
                    + KEY_LAST_TRIGGER_YEAR + " TEXT NOT NULL, "
                    + KEY_LAST_TRIGGER_MONTH + " TEXT NOT NULL, "
                    + KEY_LAST_TRIGGER_DAY + " TEXT NOT NULL, "
                    + KEY_LAST_TRIGGER_HOUR + " TEXT NOT NULL, "
                    + KEY_LAST_TRIGGER_MINUTE + " TEXT NOT NULL, "
                    + KEY_LAST_TRIGGER_USER + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseWirelessSocketList(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public DatabaseWirelessSocketList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull WirelessSocket newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_AREA, newEntry.GetArea());
        contentValues.put(KEY_CODE, newEntry.GetCode());
        contentValues.put(KEY_IS_ACTIVATED, (newEntry.IsActivated() ? "1" : "0"));
        contentValues.put(KEY_LAST_TRIGGER_YEAR, String.valueOf(newEntry.GetLastTriggerDate().Year()));
        contentValues.put(KEY_LAST_TRIGGER_MONTH, String.valueOf(newEntry.GetLastTriggerDate().Month()));
        contentValues.put(KEY_LAST_TRIGGER_DAY, String.valueOf(newEntry.GetLastTriggerDate().DayOfMonth()));
        contentValues.put(KEY_LAST_TRIGGER_HOUR, String.valueOf(newEntry.GetLastTriggerTime().Hour()));
        contentValues.put(KEY_LAST_TRIGGER_MINUTE, String.valueOf(newEntry.GetLastTriggerTime().Minute()));
        contentValues.put(KEY_LAST_TRIGGER_USER, newEntry.GetLastTriggerUser());
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull WirelessSocket updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_NAME, updateEntry.GetName());
        contentValues.put(KEY_AREA, updateEntry.GetArea());
        contentValues.put(KEY_CODE, updateEntry.GetCode());
        contentValues.put(KEY_IS_ACTIVATED, (updateEntry.IsActivated() ? "1" : "0"));
        contentValues.put(KEY_LAST_TRIGGER_YEAR, String.valueOf(updateEntry.GetLastTriggerDate().Year()));
        contentValues.put(KEY_LAST_TRIGGER_MONTH, String.valueOf(updateEntry.GetLastTriggerDate().Month()));
        contentValues.put(KEY_LAST_TRIGGER_DAY, String.valueOf(updateEntry.GetLastTriggerDate().DayOfMonth()));
        contentValues.put(KEY_LAST_TRIGGER_HOUR, String.valueOf(updateEntry.GetLastTriggerTime().Hour()));
        contentValues.put(KEY_LAST_TRIGGER_MINUTE, String.valueOf(updateEntry.GetLastTriggerTime().Minute()));
        contentValues.put(KEY_LAST_TRIGGER_USER, updateEntry.GetLastTriggerUser());
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<WirelessSocket> GetWirelessSocketList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_NAME,
                KEY_AREA,
                KEY_CODE,
                KEY_IS_ACTIVATED,
                KEY_LAST_TRIGGER_YEAR,
                KEY_LAST_TRIGGER_MONTH,
                KEY_LAST_TRIGGER_DAY,
                KEY_LAST_TRIGGER_HOUR,
                KEY_LAST_TRIGGER_MINUTE,
                KEY_LAST_TRIGGER_USER,
                KEY_IS_ON_SERVER,
                KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<WirelessSocket> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int areaIndex = cursor.getColumnIndex(KEY_AREA);
        int codeIndex = cursor.getColumnIndex(KEY_CODE);
        int isActivatedIndex = cursor.getColumnIndex(KEY_IS_ACTIVATED);
        int lastTriggerYearIndex = cursor.getColumnIndex(KEY_LAST_TRIGGER_YEAR);
        int lastTriggerMonthIndex = cursor.getColumnIndex(KEY_LAST_TRIGGER_MONTH);
        int lastTriggerDayIndex = cursor.getColumnIndex(KEY_LAST_TRIGGER_DAY);
        int lastTriggerHourIndex = cursor.getColumnIndex(KEY_LAST_TRIGGER_HOUR);
        int lastTriggerMinuteIndex = cursor.getColumnIndex(KEY_LAST_TRIGGER_MINUTE);
        int lastTriggerUserIndex = cursor.getColumnIndex(KEY_LAST_TRIGGER_USER);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String area = cursor.getString(areaIndex);
            String code = cursor.getString(codeIndex);
            String isActivatedString = cursor.getString(isActivatedIndex);
            String lastTriggerYearString = cursor.getString(lastTriggerYearIndex);
            String lastTriggerMonthString = cursor.getString(lastTriggerMonthIndex);
            String lastTriggerDayString = cursor.getString(lastTriggerDayIndex);
            String lastTriggerHourString = cursor.getString(lastTriggerHourIndex);
            String lastTriggerMinuteString = cursor.getString(lastTriggerMinuteIndex);
            String lastTriggerUser = cursor.getString(lastTriggerUserIndex);
            String isOnServerString = cursor.getString(isOnServerIndex);
            String serverActionString = cursor.getString(serverActionIndex);

            int id = -1;

            int year = 1970;
            int month = 1;
            int day = 1;
            int hour = 0;
            int minute = 0;

            try {
                id = Integer.parseInt(idString);

                year = Integer.parseInt(lastTriggerYearString);
                month = Integer.parseInt(lastTriggerMonthString);
                day = Integer.parseInt(lastTriggerDayString);
                hour = Integer.parseInt(lastTriggerHourString);
                minute = Integer.parseInt(lastTriggerMinuteString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            boolean isActivated = isActivatedString.contains("1");
            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            WirelessSocket entry = new WirelessSocket(id, name, area, code, isActivated, new SerializableDate(year, month, day), new SerializableTime(hour, minute, 0, 0), lastTriggerUser, isOnServer, serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull WirelessSocket deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
