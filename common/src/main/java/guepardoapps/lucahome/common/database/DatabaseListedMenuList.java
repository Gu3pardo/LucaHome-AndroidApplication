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
import guepardoapps.lucahome.common.classes.ListedMenu;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabaseListedMenuList {
    private static final String TAG = DatabaseListedMenuList.class.getSimpleName();
    private Logger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_DESCRIPTION = "_description";
    private static final String KEY_RATING = "_rating";
    private static final String KEY_LAST_SUGGESTION = "_lastSuggestion";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabaseListedMenuListDb";
    private static final String DATABASE_TABLE = "DatabaseListedMenuListTable";
    private static final int DATABASE_VERSION = 2;

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
                    + KEY_DESCRIPTION + " TEXT NOT NULL, "
                    + KEY_RATING + " TEXT NOT NULL, "
                    + KEY_LAST_SUGGESTION + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseListedMenuList(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public DatabaseListedMenuList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull ListedMenu newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_DESCRIPTION, newEntry.GetDescription());
        contentValues.put(KEY_RATING, String.valueOf(newEntry.GetRating()));
        contentValues.put(KEY_LAST_SUGGESTION, newEntry.GetLastSuggestion() ? "1" : "0");
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull ListedMenu updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_DESCRIPTION, updateEntry.GetDescription());
        contentValues.put(KEY_RATING, String.valueOf(updateEntry.GetRating()));
        contentValues.put(KEY_LAST_SUGGESTION, updateEntry.GetLastSuggestion() ? "1" : "0");
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<ListedMenu> GetListedMenuList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_DESCRIPTION,
                KEY_RATING,
                KEY_LAST_SUGGESTION,
                KEY_IS_ON_SERVER,
                KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<ListedMenu> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int descriptionIndex = cursor.getColumnIndex(KEY_DESCRIPTION);
        int ratingIndex = cursor.getColumnIndex(KEY_RATING);
        int lastSuggestionIndex = cursor.getColumnIndex(KEY_LAST_SUGGESTION);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String description = cursor.getString(descriptionIndex);
            String ratingString = cursor.getString(ratingIndex);
            String lastSuggestionString = cursor.getString(lastSuggestionIndex);

            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            int rating = -1;
            try {
                rating = Integer.parseInt(ratingString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            boolean lastSuggestion = lastSuggestionString.contains("1");

            String isOnServerString = cursor.getString(isOnServerIndex);
            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            String serverActionString = cursor.getString(serverActionIndex);
            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            ListedMenu entry = new ListedMenu(id, description, rating, lastSuggestion, isOnServer, serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull ListedMenu deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
