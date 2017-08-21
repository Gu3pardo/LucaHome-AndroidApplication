package guepardoapps.lucahome.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSocket;

public class DatabaseSocketList {
    private static final String TAG = DatabaseSocketList.class.getSimpleName();
    private Logger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_AREA = "_area";
    private static final String KEY_CODE = "_code";
    private static final String KEY_IS_ACTIVATED = "_isActivated";

    private static final String DATABASE_NAME = "DatabaseSocketListDb";
    private static final String DATABASE_TABLE = "DatabaseSocketListTable";
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
                    + KEY_AREA + " TEXT NOT NULL, "
                    + KEY_CODE + " TEXT NOT NULL, "
                    + KEY_IS_ACTIVATED + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseSocketList(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public DatabaseSocketList Open() throws SQLException {
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

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<WirelessSocket> GetSocketList() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_NAME, KEY_AREA, KEY_CODE, KEY_IS_ACTIVATED};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<WirelessSocket> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int areaIndex = cursor.getColumnIndex(KEY_AREA);
        int codeIndex = cursor.getColumnIndex(KEY_CODE);
        int isActivatedIndex = cursor.getColumnIndex(KEY_IS_ACTIVATED);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String area = cursor.getString(areaIndex);
            String code = cursor.getString(codeIndex);
            String isActivatedString = cursor.getString(isActivatedIndex);

            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            boolean isActivated = isActivatedString.contains("1");

            WirelessSocket entry = new WirelessSocket(id, name, area, code, isActivated);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull WirelessSocket deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
