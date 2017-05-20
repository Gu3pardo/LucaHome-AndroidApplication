package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import guepardoapps.library.lucahome.common.dto.ActionDto;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseActionStore {
    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_ACTION = "_action";
    private static final String KEY_BROADCAST = "_broadcast";

    private static final String DATABASE_NAME = "DatabaseActionStoreDb";
    private static final String DATABASE_TABLE = "DatabaseActionStoreTable";
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
            database.execSQL(" CREATE TABLE " + DATABASE_TABLE + " ( " + KEY_ROW_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME + " TEXT NOT NULL, " + KEY_ACTION
                    + " TEXT NOT NULL, " + KEY_BROADCAST + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseActionStore(Context context) {
        _context = context;
    }

    public DatabaseActionStore Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(ActionDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_ACTION, newEntry.GetAction());
        contentValues.put(KEY_BROADCAST, newEntry.GetBroadcast());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<ActionDto> GetStoredActions() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_NAME, KEY_ACTION, KEY_BROADCAST};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<ActionDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);

        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int actionIndex = cursor.getColumnIndex(KEY_ACTION);
        int broadcastIndex = cursor.getColumnIndex(KEY_BROADCAST);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int id = cursor.getInt(idIndex);

            String name = cursor.getString(nameIndex);
            String action = cursor.getString(actionIndex);
            String broadcast = cursor.getString(broadcastIndex);

            ActionDto entry = new ActionDto(id, name, action, broadcast);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(ActionDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
