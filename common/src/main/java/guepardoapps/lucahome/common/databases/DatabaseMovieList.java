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

import guepardoapps.lucahome.common.classes.Movie;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseMovieList implements IDatabaseLucaClassList<Movie> {
    public static final String KeyTitle = "_title";
    public static final String KeyGenre = "_genre";
    public static final String KeyDescription = "_description";
    public static final String KeyRating = "_rating";
    public static final String KeyWatchCount = "_watchCount";

    private static final String DatabaseTable = "DatabaseMovieListTable";

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
                    + KeyGenre + " TEXT NOT NULL, "
                    + KeyDescription + " TEXT NOT NULL, "
                    + KeyRating + " INTEGER, "
                    + KeyWatchCount + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseMovieList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseMovieList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull Movie entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyGenre, entry.GetGenre());
        contentValues.put(KeyDescription, entry.GetDescription());
        contentValues.put(KeyRating, entry.GetRating());
        contentValues.put(KeyWatchCount, entry.GetWatchCount());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull Movie entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyGenre, entry.GetGenre());
        contentValues.put(KeyDescription, entry.GetDescription());
        contentValues.put(KeyRating, entry.GetRating());
        contentValues.put(KeyWatchCount, entry.GetWatchCount());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull Movie entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<Movie> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyTitle,
                KeyGenre,
                KeyDescription,
                KeyRating,
                KeyWatchCount};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<Movie> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int titleIndex = cursor.getColumnIndex(KeyTitle);
        int genreIndex = cursor.getColumnIndex(KeyGenre);
        int descriptionIndex = cursor.getColumnIndex(KeyDescription);
        int ratingIndex = cursor.getColumnIndex(KeyRating);
        int watchCountIndex = cursor.getColumnIndex(KeyWatchCount);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String title = cursor.getString(titleIndex);
            String genre = cursor.getString(genreIndex);
            String description = cursor.getString(descriptionIndex);
            int rating = cursor.getInt(ratingIndex);
            int watchCount = cursor.getInt(watchCountIndex);

            UUID uuid = UUID.fromString(uuidString);

            Movie entry = new Movie(uuid, title, genre, description, rating, watchCount);
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
