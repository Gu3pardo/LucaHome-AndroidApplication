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
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabasePuckJsList {
    private static final String TAG = DatabasePuckJsList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_AREA = "_area";
    private static final String KEY_MAC = "_mac";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabasePuckJsListDb";
    private static final String DATABASE_TABLE = "DatabasePuckJsListTable";
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
                    + KEY_MAC + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabasePuckJsList(@NonNull Context context) {
        _context = context;
    }

    public DatabasePuckJsList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull PuckJs newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_AREA, newEntry.GetArea());
        contentValues.put(KEY_MAC, newEntry.GetMac());
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull PuckJs updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_NAME, updateEntry.GetName());
        contentValues.put(KEY_AREA, updateEntry.GetArea());
        contentValues.put(KEY_MAC, updateEntry.GetMac());
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<PuckJs> GetPuckJsList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_NAME,
                KEY_AREA,
                KEY_MAC,
                KEY_IS_ON_SERVER,
                KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<PuckJs> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int areaIndex = cursor.getColumnIndex(KEY_AREA);
        int macIndex = cursor.getColumnIndex(KEY_MAC);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String area = cursor.getString(areaIndex);
            String mac = cursor.getString(macIndex);

            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }

            String isOnServerString = cursor.getString(isOnServerIndex);
            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            String serverActionString = cursor.getString(serverActionIndex);
            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            PuckJs entry = new PuckJs(id, name, area, mac, isOnServer, serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull PuckJs deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
