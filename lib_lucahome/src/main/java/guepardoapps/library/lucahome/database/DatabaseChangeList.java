package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Date;

import guepardoapps.library.lucahome.common.dto.ChangeDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;

public class DatabaseChangeList {

    private static final String TAG = DatabaseChangeList.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_TYPE = "_type";
    private static final String KEY_DATE = "_date";
    private static final String KEY_TIME = "_time";
    private static final String KEY_USER = "_user";

    private static final String DATABASE_NAME = "DatabaseChangeListDb";
    private static final String DATABASE_TABLE = "DatabaseChangeListTable";
    private static final int DATABASE_VERSION = 1;

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
                    + KEY_TYPE + " TEXT NOT NULL, "
                    + KEY_DATE + " TEXT NOT NULL, "
                    + KEY_TIME + " TEXT NOT NULL, "
                    + KEY_USER + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseChangeList(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public DatabaseChangeList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(ChangeDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_TYPE, newEntry.GetType());
        contentValues.put(KEY_DATE, newEntry.GetDateString());
        contentValues.put(KEY_TIME, newEntry.GetTime().HHMMSSmm());
        contentValues.put(KEY_USER, newEntry.GetUser());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<ChangeDto> GetChangeList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_TYPE,
                KEY_DATE,
                KEY_TIME,
                KEY_USER};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<ChangeDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int typeIndex = cursor.getColumnIndex(KEY_TYPE);
        int dateIndex = cursor.getColumnIndex(KEY_DATE);
        int timeIndex = cursor.getColumnIndex(KEY_TIME);
        int userIndex = cursor.getColumnIndex(KEY_USER);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String type = cursor.getString(typeIndex);
            String dateString = cursor.getString(dateIndex);
            String timeString = cursor.getString(timeIndex);
            String user = cursor.getString(userIndex);

            int id = -1;
            Date date = new Date(-1, -1, -1);
            SerializableTime time = new SerializableTime();
            try {
                id = Integer.parseInt(idString);

                String[] dateStringArray = dateString.split("\\|");
                if (dateStringArray.length == 3) {
                    int day = Integer.parseInt(dateStringArray[0].replace("|", ""));
                    int month = Integer.parseInt(dateStringArray[1].replace("|", ""));
                    int year = Integer.parseInt(dateStringArray[2].replace("|", ""));
                    date = new Date(day, month, year);
                }

                String[] timeStringArray = timeString.split("\\:");
                if (timeStringArray.length == 4) {
                    int hour = Integer.parseInt(timeStringArray[0].replace(":", ""));
                    int minute = Integer.parseInt(timeStringArray[1].replace(":", ""));
                    int second = Integer.parseInt(timeStringArray[2].replace(":", ""));
                    int milliSecond = Integer.parseInt(timeStringArray[3].replace(":", ""));
                    time = new SerializableTime(hour, minute, second, milliSecond);
                }
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            ChangeDto entry = new ChangeDto(
                    id,
                    type,
                    date,
                    time,
                    user);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(ChangeDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
