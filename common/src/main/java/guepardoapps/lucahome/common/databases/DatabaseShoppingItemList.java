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
import guepardoapps.lucahome.common.classes.ShoppingItem;
import guepardoapps.lucahome.common.enums.ShoppingItemType;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseShoppingItemList implements IDatabaseLucaClassList<ShoppingItem> {
    public static final String KeyName = "_name";
    public static final String KeyType = "_type";
    public static final String KeyQuantity = "_quantity";
    public static final String KeyUnit = "_unit";
    public static final String KeyBought = "_bought";

    private static final String DatabaseTable = "DatabaseShoppingItemListTable";

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
                    + KeyName + " TEXT NOT NULL, "
                    + KeyType + " INTEGER, "
                    + KeyQuantity + " INTEGER, "
                    + KeyUnit + " TEXT NOT NULL, "
                    + KeyBought + " INTEGER, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseShoppingItemList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseShoppingItemList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull ShoppingItem entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyType, entry.GetType().ordinal());
        contentValues.put(KeyQuantity, entry.GetQuantity());
        contentValues.put(KeyUnit, entry.GetUnit());
        contentValues.put(KeyBought, entry.GetBought() ? 1 : 0);
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull ShoppingItem entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyType, entry.GetType().ordinal());
        contentValues.put(KeyQuantity, entry.GetQuantity());
        contentValues.put(KeyUnit, entry.GetUnit());
        contentValues.put(KeyBought, entry.GetBought() ? 1 : 0);
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull ShoppingItem entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<ShoppingItem> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyName,
                KeyType,
                KeyQuantity,
                KeyUnit,
                KeyBought,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<ShoppingItem> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int nameIndex = cursor.getColumnIndex(KeyName);
        int typeIndex = cursor.getColumnIndex(KeyType);
        int quantityIndex = cursor.getColumnIndex(KeyQuantity);
        int unitIndex = cursor.getColumnIndex(KeyUnit);
        int boughtIndex = cursor.getColumnIndex(KeyBought);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String name = cursor.getString(nameIndex);
            int typeInteger = cursor.getInt(typeIndex);
            int quantity = cursor.getInt(quantityIndex);
            String unit = cursor.getString(unitIndex);
            int boughtInteger = cursor.getInt(boughtIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);

            ShoppingItemType type = ShoppingItemType.GetById(typeInteger);
            boolean bought = boughtInteger == 1;

            boolean isOnServer = isOnServerInteger == 1;
            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            ShoppingItem entry = new ShoppingItem(uuid, name, type, quantity, unit, bought, isOnServer, serverDbAction);
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
