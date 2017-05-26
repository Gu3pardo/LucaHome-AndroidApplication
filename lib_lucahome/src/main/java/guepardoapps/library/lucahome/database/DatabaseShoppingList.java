package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseShoppingList {

    private static final String TAG = DatabaseShoppingList.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_GROUP = "_group";
    private static final String KEY_QUANTITY = "_quantity";
    private static final String KEY_BOUGHT = "_bought";

    private static final String DATABASE_NAME = "DatabaseShoppingListDb";
    private static final String DATABASE_TABLE = "DatabaseShoppingListTable";
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
            database.execSQL(" CREATE TABLE " + DATABASE_TABLE + " ( " + KEY_ROW_ID + " TEXT NOT NULL, " + KEY_NAME
                    + " TEXT NOT NULL, " + KEY_GROUP + " TEXT NOT NULL, " + KEY_QUANTITY + " TEXT NOT NULL, "
                    + KEY_BOUGHT + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseShoppingList(Context context) {
        _logger = new LucaHomeLogger(TAG);
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

    public long CreateEntry(ShoppingEntryDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_GROUP, newEntry.GetGroup().toString());
        contentValues.put(KEY_QUANTITY, newEntry.GetQuantity());
        contentValues.put(KEY_BOUGHT, (newEntry.IsBought() ? "1" : "0"));

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<ShoppingEntryDto> GetShoppingList() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_NAME, KEY_GROUP, KEY_QUANTITY, KEY_BOUGHT};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<ShoppingEntryDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int groupIndex = cursor.getColumnIndex(KEY_GROUP);
        int quantityIndex = cursor.getColumnIndex(KEY_QUANTITY);
        int boughtIndex = cursor.getColumnIndex(KEY_BOUGHT);

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

            ShoppingEntryDto entry = new ShoppingEntryDto(id, name, group, quantity, bought);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(ShoppingEntryDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
