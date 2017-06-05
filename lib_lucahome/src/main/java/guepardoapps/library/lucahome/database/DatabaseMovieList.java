package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseMovieList {

    private static final String TAG = DatabaseMovieList.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_TITLE = "_title";
    private static final String KEY_GENRE = "_genre";
    private static final String KEY_DESCRIPTION = "_description";
    private static final String KEY_RATING = "_rating";
    private static final String KEY_WATCHED = "_watched";
    private static final String KEY_SOCKETS = "_sockets";

    private static final String DATABASE_NAME = "DatabaseMovieListDb";
    private static final String DATABASE_TABLE = "DatabaseMovieListTable";
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
                    + KEY_TITLE + " TEXT NOT NULL, "
                    + KEY_GENRE + " TEXT NOT NULL, "
                    + KEY_DESCRIPTION + " TEXT NOT NULL, "
                    + KEY_RATING + " TEXT NOT NULL, "
                    + KEY_WATCHED + " TEXT NOT NULL, "
                    + KEY_SOCKETS + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseMovieList(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public DatabaseMovieList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(MovieDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_TITLE, newEntry.GetTitle());
        contentValues.put(KEY_GENRE, newEntry.GetGenre());
        contentValues.put(KEY_DESCRIPTION, newEntry.GetDescription());
        contentValues.put(KEY_RATING, String.valueOf(newEntry.GetRating()));
        contentValues.put(KEY_WATCHED, String.valueOf(newEntry.GetWatched()));
        contentValues.put(KEY_SOCKETS, newEntry.GetSocketsString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<MovieDto> GetMovieList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_TITLE,
                KEY_GENRE,
                KEY_DESCRIPTION,
                KEY_RATING,
                KEY_WATCHED,
                KEY_SOCKETS};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<MovieDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int titleIndex = cursor.getColumnIndex(KEY_TITLE);
        int genreIndex = cursor.getColumnIndex(KEY_GENRE);
        int descriptionIndex = cursor.getColumnIndex(KEY_DESCRIPTION);
        int ratingIndex = cursor.getColumnIndex(KEY_RATING);
        int watchedIndex = cursor.getColumnIndex(KEY_WATCHED);
        int socketsIndex = cursor.getColumnIndex(KEY_SOCKETS);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String title = cursor.getString(titleIndex);
            String genre = cursor.getString(genreIndex);
            String description = cursor.getString(descriptionIndex);
            String ratingString = cursor.getString(ratingIndex);
            String watchedString = cursor.getString(watchedIndex);
            String socketsString = cursor.getString(socketsIndex);

            int id = -1;
            int rating = -1;
            int watched = -1;
            try {
                id = Integer.parseInt(idString);
                rating = Integer.parseInt(ratingString);
                watched = Integer.parseInt(watchedString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            String[] sockets = socketsString.split("\\|");
            for (int index = 0; index < sockets.length; index++) {
                String socket = sockets[index];
                socket = socket.replace("|", "");
                sockets[index] = socket;
            }

            MovieDto entry = new MovieDto(id, title, genre, description, rating, watched, sockets);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(MovieDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
