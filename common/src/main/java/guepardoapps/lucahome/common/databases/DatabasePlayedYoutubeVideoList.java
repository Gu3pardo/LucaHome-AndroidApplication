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

import guepardoapps.lucahome.common.classes.mediaserver.PlayedYoutubeVideo;

@SuppressWarnings({"WeakerAccess"})
public class DatabasePlayedYoutubeVideoList implements IDatabaseLucaClassList<PlayedYoutubeVideo> {
    public static final String KeyYoutubeId = "_youtubeId";
    public static final String KeyTitle = "_title";
    public static final String KeyPlayCount = "_playCount";

    private static final String DatabaseTable = "DatabasePlayedYoutubeVideoListTable";

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
                    + KeyYoutubeId + " TEXT NOT NULL, "
                    + KeyTitle + " TEXT NOT NULL, "
                    + KeyPlayCount + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabasePlayedYoutubeVideoList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabasePlayedYoutubeVideoList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull PlayedYoutubeVideo entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyYoutubeId, entry.GetYoutubeId());
        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyPlayCount, entry.GetPlayCount());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull PlayedYoutubeVideo entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyYoutubeId, entry.GetYoutubeId());
        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyPlayCount, entry.GetPlayCount());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull PlayedYoutubeVideo entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<PlayedYoutubeVideo> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyYoutubeId,
                KeyTitle,
                KeyPlayCount};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<PlayedYoutubeVideo> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int youtubeIdIndex = cursor.getColumnIndex(KeyYoutubeId);
        int titleIndex = cursor.getColumnIndex(KeyTitle);
        int playCountIndex = cursor.getColumnIndex(KeyPlayCount);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String youtubeId = cursor.getString(youtubeIdIndex);
            String title = cursor.getString(titleIndex);
            int playCount = cursor.getInt(playCountIndex);

            UUID uuid = UUID.fromString(uuidString);

            PlayedYoutubeVideo entry = new PlayedYoutubeVideo(uuid, youtubeId, title, playCount);
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
