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
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabaseShoppingList {
    private static final String TAG = DatabaseShoppingList.class.getSimpleName();
    private Logger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_GROUP = "_group";
    private static final String KEY_QUANTITY = "_quantity";
    private static final String KEY_BOUGHT = "_bought";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabaseShoppingListDb";
    private static final String DATABASE_TABLE = "DatabaseShoppingListTable";
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
                    + KEY_GROUP + " TEXT NOT NULL, "
                    + KEY_QUANTITY + " TEXT NOT NULL, "
                    + KEY_BOUGHT + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseShoppingList(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public DatabaseShoppingList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull ShoppingEntry newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_GROUP, newEntry.GetGroup().toString());
        contentValues.put(KEY_QUANTITY, newEntry.GetQuantity());
        contentValues.put(KEY_BOUGHT, (newEntry.GetBought() ? "1" : "0"));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull ShoppingEntry updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_NAME, updateEntry.GetName());
        contentValues.put(KEY_GROUP, updateEntry.GetGroup().toString());
        contentValues.put(KEY_QUANTITY, updateEntry.GetQuantity());
        contentValues.put(KEY_BOUGHT, (updateEntry.GetBought() ? "1" : "0"));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<ShoppingEntry> GetShoppingList() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_NAME, KEY_GROUP, KEY_QUANTITY, KEY_BOUGHT, KEY_IS_ON_SERVER, KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<ShoppingEntry> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int groupIndex = cursor.getColumnIndex(KEY_GROUP);
        int quantityIndex = cursor.getColumnIndex(KEY_QUANTITY);
        int boughtIndex = cursor.getColumnIndex(KEY_BOUGHT);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String groupString = cursor.getString(groupIndex);
            String quantityString = cursor.getString(quantityIndex);
            String boughtString = cursor.getString(boughtIndex);

            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            ShoppingEntryGroup group = ShoppingEntryGroup.OTHER;
            try {
                group = ShoppingEntryGroup.GetByString(groupString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            int quantity = -1;
            try {
                quantity = Integer.parseInt(quantityString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            boolean bought = boughtString.contains("1");

            String isOnServerString = cursor.getString(isOnServerIndex);
            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            String serverActionString = cursor.getString(serverActionIndex);
            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            ShoppingEntry entry = new ShoppingEntry(id, name, group, quantity, bought, isOnServer, serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull ShoppingEntry deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
