package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;

import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseBirthdayList {

    private static final String TAG = DatabaseBirthdayList.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_BIRTHDAY = "_birthday";
    private static final String KEY_DRAWABLE = "_drawable";

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
                    + KEY_BIRTHDAY + " TEXT NOT NULL, "
                    + KEY_DRAWABLE + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseBirthdayList(Context context) {
        _logger = new LucaHomeLogger(TAG);
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

    public long CreateEntry(BirthdayDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_BIRTHDAY, newEntry.GetBirthdayString());
        contentValues.put(KEY_DRAWABLE, String.valueOf(newEntry.GetDrawable()));

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<BirthdayDto> GetBirthdayList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_NAME,
                KEY_BIRTHDAY,
                KEY_DRAWABLE};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<BirthdayDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int birthdayIndex = cursor.getColumnIndex(KEY_BIRTHDAY);
        int drawableIndex = cursor.getColumnIndex(KEY_DRAWABLE);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String birthdayString = cursor.getString(birthdayIndex);
            String drawableString = cursor.getString(drawableIndex);

            int id = -1;
            int drawable = -1;
            try {
                id = Integer.parseInt(idString);
                drawable = Integer.parseInt(drawableString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            Calendar birthday = Calendar.getInstance();

            String[] birthdayStringArray = birthdayString.split("\\.");
            if (birthdayStringArray.length == 3) {
                String dayString = birthdayStringArray[0].replace(".", "");
                String monthString = birthdayStringArray[1].replace(".", "");
                String yearString = birthdayStringArray[2].replace(".", "");

                try {
                    int day = Integer.parseInt(dayString);
                    int month = Integer.parseInt(monthString);
                    int year = Integer.parseInt(yearString);

                    birthday.set(Calendar.DAY_OF_MONTH, day);
                    birthday.set(Calendar.MONTH, month - 1);
                    birthday.set(Calendar.YEAR, year);
                } catch (Exception ex) {
                    _logger.Error(ex.toString());
                }
            } else {
                _logger.Error("Size of birthdayStringArray has invalid size " + String.valueOf(birthdayStringArray.length));
            }

            BirthdayDto entry = new BirthdayDto(
                    id,
                    name,
                    birthday,
                    drawable);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(BirthdayDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
