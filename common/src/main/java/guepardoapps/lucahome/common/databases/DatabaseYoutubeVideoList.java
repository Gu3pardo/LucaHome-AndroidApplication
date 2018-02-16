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
import guepardoapps.lucahome.common.classes.YoutubeVideo;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseYoutubeVideoList implements IDatabaseLucaClassList<YoutubeVideo> {
    public static final String KeyYoutubeId = "_youtubeId";
    public static final String KeyTitle = "_title";
    public static final String KeyPlayCount = "_playCount";
    public static final String KeyDescription = "_description";
    public static final String KeyMediumImageUrl = "_mediumImageUrl";

    private static final String DatabaseTable = "DatabaseYoutubeVideoListTable";

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
                    + KeyPlayCount + " INTEGER, "
                    + KeyDescription + " TEXT NOT NULL, "
                    + KeyMediumImageUrl + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseYoutubeVideoList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseYoutubeVideoList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull YoutubeVideo entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyYoutubeId, entry.GetYoutubeId());
        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyPlayCount, entry.GetPlayCount());
        contentValues.put(KeyDescription, entry.GetDescription());
        contentValues.put(KeyMediumImageUrl, entry.GetMediumImageUrl());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull YoutubeVideo entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyYoutubeId, entry.GetYoutubeId());
        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyPlayCount, entry.GetPlayCount());
        contentValues.put(KeyDescription, entry.GetDescription());
        contentValues.put(KeyMediumImageUrl, entry.GetMediumImageUrl());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull YoutubeVideo entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<YoutubeVideo> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyYoutubeId,
                KeyTitle,
                KeyPlayCount,
                KeyDescription,
                KeyMediumImageUrl,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<YoutubeVideo> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int youtubeIdIndex = cursor.getColumnIndex(KeyYoutubeId);
        int titleIndex = cursor.getColumnIndex(KeyTitle);
        int playCountIndex = cursor.getColumnIndex(KeyPlayCount);
        int descriptionIndex = cursor.getColumnIndex(KeyDescription);
        int mediumImageUrlIndex = cursor.getColumnIndex(KeyMediumImageUrl);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String youtubeId = cursor.getString(youtubeIdIndex);
            String title = cursor.getString(titleIndex);
            int playCount = cursor.getInt(playCountIndex);
            String description = cursor.getString(descriptionIndex);
            String mediumImageUrl = cursor.getString(mediumImageUrlIndex);

            UUID uuid = UUID.fromString(uuidString);

            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            boolean isOnServer = isOnServerInteger == 1;
            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            YoutubeVideo entry = new YoutubeVideo(uuid, youtubeId, title, playCount, description, mediumImageUrl, isOnServer, serverDbAction);
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
