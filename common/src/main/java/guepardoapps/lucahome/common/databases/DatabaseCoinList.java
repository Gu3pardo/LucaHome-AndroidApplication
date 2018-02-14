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

import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.classes.ILucaClass;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseCoinList implements IDatabaseLucaClassList<Coin> {
    public static final String KeyUser = "_user";
    public static final String KeyType = "_type";
    public static final String KeyAmount = "_amount";
    public static final String KeyCurrentConversion = "_currentConversion";
    public static final String KeyCurrentTrend = "_currentTrend";

    private static final String DatabaseTable = "DatabaseCoinListTable";

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
                    + KeyUser + " TEXT NOT NULL, "
                    + KeyType + " TEXT NOT NULL, "
                    + KeyAmount + " REAL, "
                    + KeyCurrentConversion + " REAL, "
                    + KeyCurrentTrend + " INTEGER, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseCoinList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseCoinList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull Coin entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyUser, entry.GetUser());
        contentValues.put(KeyType, entry.GetType());
        contentValues.put(KeyAmount, entry.GetAmount());
        contentValues.put(KeyCurrentConversion, entry.GetCurrentConversion());
        contentValues.put(KeyCurrentTrend, entry.GetCurrentTrend().ordinal());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull Coin entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUser, entry.GetUser());
        contentValues.put(KeyType, entry.GetType());
        contentValues.put(KeyAmount, entry.GetAmount());
        contentValues.put(KeyCurrentConversion, entry.GetCurrentConversion());
        contentValues.put(KeyCurrentTrend, entry.GetCurrentTrend().ordinal());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull Coin entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<Coin> GetList(String selection, String groupBy, String orderBy) {
        String[] columns = new String[]{
                KeyUuid,
                KeyUser,
                KeyType,
                KeyAmount,
                KeyCurrentConversion,
                KeyCurrentTrend,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<Coin> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int userIndex = cursor.getColumnIndex(KeyUser);
        int typeIndex = cursor.getColumnIndex(KeyType);
        int amountIndex = cursor.getColumnIndex(KeyAmount);
        int currentConversionIndex = cursor.getColumnIndex(KeyCurrentConversion);
        int currentTrendIndex = cursor.getColumnIndex(KeyCurrentTrend);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String user = cursor.getString(userIndex);
            String type = cursor.getString(typeIndex);
            float amount = cursor.getFloat(amountIndex);
            float currentConversion = cursor.getFloat(currentConversionIndex);

            int currentTrendInteger = cursor.getInt(currentTrendIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);

            Coin.Trend currentTrend = Coin.Trend.values()[currentTrendInteger];

            boolean isOnServer = isOnServerInteger == 1;

            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            Coin entry = new Coin(uuid, user, type, amount, currentConversion, currentTrend, isOnServer, serverDbAction);
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
