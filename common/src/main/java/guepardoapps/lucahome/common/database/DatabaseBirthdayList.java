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
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaBirthday;

public class DatabaseBirthdayList {

    private static final String TAG = DatabaseBirthdayList.class.getSimpleName();
    private Logger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_DATE = "_date";

    private static final String DATABASE_NAME = "DatabaseBirthdayListDb";
    private static final String DATABASE_TABLE = "DatabaseBirthdayListTable";
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
                    + KEY_NAME + " TEXT NOT NULL, "
                    + KEY_DATE + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseBirthdayList(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public DatabaseBirthdayList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull LucaBirthday newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_DATE, newEntry.GetDate().DDMMYYYY());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<LucaBirthday> GetBirthdayList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_NAME,
                KEY_DATE};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<LucaBirthday> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int dateIndex = cursor.getColumnIndex(KEY_DATE);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            int id = -1;
            String name = cursor.getString(nameIndex);
            String dateString = cursor.getString(dateIndex);
            SerializableDate date = new SerializableDate();

            try {
                id = Integer.parseInt(idString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            String[] dateStringArray = dateString.split("\\.");
            if (dateStringArray.length == 3) {
                String dayString = dateStringArray[0].replace(".", "");
                String monthString = dateStringArray[1].replace(".", "");
                String yearString = dateStringArray[2].replace(".", "");

                try {
                    int day = Integer.parseInt(dayString);
                    int month = Integer.parseInt(monthString);
                    int year = Integer.parseInt(yearString);

                    date = new SerializableDate(year, month, day);
                } catch (Exception ex) {
                    _logger.Error(ex.toString());
                }
            } else {
                _logger.Error("Size of birthdayStringArray has invalid size " + String.valueOf(dateStringArray.length));
            }

            LucaBirthday entry = new LucaBirthday(
                    id,
                    name,
                    date);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull LucaBirthday deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
