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

import guepardoapps.lucahome.common.classes.Change;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseChangeList implements IDatabaseLucaClassList<Change> {
    public static final String KeyType = "_type";
    public static final String KeyDateTime = "_dateTime";
    public static final String KeyUser = "_user";

    private static final String DatabaseTable = "DatabaseChangeListTable";

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
                    + KeyType + " TEXT NOT NULL, "
                    + KeyDateTime + " INTEGER, "
                    + KeyUser + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseChangeList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseChangeList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull Change entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyType, entry.GetType());
        contentValues.put(KeyDateTime, entry.GetDateTime().getTimeInMillis());
        contentValues.put(KeyUser, entry.GetUser());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull Change entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyType, entry.GetType());
        contentValues.put(KeyDateTime, entry.GetDateTime().getTimeInMillis());
        contentValues.put(KeyUser, entry.GetUser());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull Change entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<Change> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyType,
                KeyDateTime,
                KeyUser};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<Change> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int typeIndex = cursor.getColumnIndex(KeyType);
        int dateTimeIndex = cursor.getColumnIndex(KeyDateTime);
        int userIndex = cursor.getColumnIndex(KeyUser);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String type = cursor.getString(typeIndex);
            int dateTimeInteger = cursor.getInt(dateTimeIndex);
            String user = cursor.getString(userIndex);

            UUID uuid = UUID.fromString(uuidString);

            Calendar dateTime = Calendar.getInstance();
            dateTime.setTimeInMillis(dateTimeInteger);

            Change entry = new Change(uuid, type, dateTime, user);
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
