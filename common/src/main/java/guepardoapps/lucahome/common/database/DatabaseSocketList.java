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
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabaseSocketList {
    private static final String TAG = DatabaseSocketList.class.getSimpleName();
    private Logger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_AREA = "_area";
    private static final String KEY_CODE = "_code";
    private static final String KEY_IS_ACTIVATED = "_isActivated";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabaseSocketListDb";
    private static final String DATABASE_TABLE = "DatabaseSocketListTable";
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
                    + KEY_NAME + " TEXT NOT NULL, "
                    + KEY_AREA + " TEXT NOT NULL, "
                    + KEY_CODE + " TEXT NOT NULL, "
                    + KEY_IS_ACTIVATED + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
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
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull WirelessSocket updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_NAME, updateEntry.GetName());
        contentValues.put(KEY_AREA, updateEntry.GetArea());
        contentValues.put(KEY_CODE, updateEntry.GetCode());
        contentValues.put(KEY_IS_ACTIVATED, (updateEntry.IsActivated() ? "1" : "0"));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<WirelessSocket> GetSocketList() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_NAME, KEY_AREA, KEY_CODE, KEY_IS_ACTIVATED, KEY_IS_ON_SERVER, KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<WirelessSocket> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int areaIndex = cursor.getColumnIndex(KEY_AREA);
        int codeIndex = cursor.getColumnIndex(KEY_CODE);
        int isActivatedIndex = cursor.getColumnIndex(KEY_IS_ACTIVATED);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

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

            String isOnServerString = cursor.getString(isOnServerIndex);
            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            String serverActionString = cursor.getString(serverActionIndex);
            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            WirelessSocket entry = new WirelessSocket(id, name, area, code, isActivated, isOnServer, serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull WirelessSocket deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
