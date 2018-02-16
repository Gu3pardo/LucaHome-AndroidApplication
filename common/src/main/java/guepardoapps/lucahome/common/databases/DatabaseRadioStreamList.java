package guepardoapps.lucahome.common.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.RadioStream;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseRadioStreamList implements IDatabaseLucaClassList<RadioStream> {
    public static final String KeyTitle = "_title";
    public static final String KeyUrl = "_url";

    private static final String DatabaseTable = "DatabaseRadioStreamListTable";

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
                    + KeyTitle + " TEXT NOT NULL, "
                    + KeyUrl + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseRadioStreamList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseRadioStreamList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull RadioStream entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyUrl, entry.GetUrl());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull RadioStream entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyUrl, entry.GetUrl());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull RadioStream entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<RadioStream> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyTitle,
                KeyUrl,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<RadioStream> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int titleIndex = cursor.getColumnIndex(KeyTitle);
        int urlIndex = cursor.getColumnIndex(KeyUrl);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String title = cursor.getString(titleIndex);
            String url = cursor.getString(urlIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);

            boolean isOnServer = isOnServerInteger == 1;
            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            RadioStream entry = new RadioStream(uuid, title, url, isOnServer, serverDbAction);
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
