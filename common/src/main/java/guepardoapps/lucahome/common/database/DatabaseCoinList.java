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
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Coin;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabaseCoinList {
    private static final String TAG = DatabaseCoinList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_USER = "_user";
    private static final String KEY_TYPE = "_type";
    private static final String KEY_AMOUNT = "_amount";
    private static final String KEY_CURRENT_CONVERSION = "_currentConversion";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabaseCoinListDb";
    private static final String DATABASE_TABLE = "DatabaseCoinListTable";
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
                    + KEY_USER + " TEXT NOT NULL, "
                    + KEY_TYPE + " TEXT NOT NULL, "
                    + KEY_AMOUNT + " TEXT NOT NULL, "
                    + KEY_CURRENT_CONVERSION + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseCoinList(@NonNull Context context) {
        _context = context;
    }

    public DatabaseCoinList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull Coin newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_USER, newEntry.GetUser());
        contentValues.put(KEY_TYPE, newEntry.GetType());
        contentValues.put(KEY_AMOUNT, String.valueOf(newEntry.GetAmount()));
        contentValues.put(KEY_CURRENT_CONVERSION, String.valueOf(newEntry.GetCurrentConversion()));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull Coin updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_USER, updateEntry.GetUser());
        contentValues.put(KEY_TYPE, updateEntry.GetType());
        contentValues.put(KEY_AMOUNT, String.valueOf(updateEntry.GetAmount()));
        contentValues.put(KEY_CURRENT_CONVERSION, String.valueOf(updateEntry.GetCurrentConversion()));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<Coin> GetCoinList() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_USER, KEY_TYPE, KEY_AMOUNT, KEY_CURRENT_CONVERSION, KEY_IS_ON_SERVER, KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<Coin> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int userIndex = cursor.getColumnIndex(KEY_USER);
        int typeIndex = cursor.getColumnIndex(KEY_TYPE);
        int amountIndex = cursor.getColumnIndex(KEY_AMOUNT);
        int currentConversionIndex = cursor.getColumnIndex(KEY_CURRENT_CONVERSION);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String user = cursor.getString(userIndex);
            String type = cursor.getString(typeIndex);
            String amountString = cursor.getString(amountIndex);
            String currentConversionString = cursor.getString(currentConversionIndex);

            int id = -1;
            double amount = 0;
            double currentConversion = 0;

            try {
                id = Integer.parseInt(idString);
                amount = Double.parseDouble(amountString);
                currentConversion = Double.parseDouble(currentConversionString);
            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }


            int icon;
            switch (type) {
                case "BCH":
                    icon = R.drawable.bch;
                    break;
                case "BTC":
                    icon = R.drawable.btc;
                    break;
                case "DASH":
                    icon = R.drawable.dash;
                    break;
                case "ETC":
                    icon = R.drawable.etc;
                    break;
                case "ETH":
                    icon = R.drawable.eth;
                    break;
                case "IOTA":
                    icon = R.drawable.iota;
                    break;
                case "LTC":
                    icon = R.drawable.ltc;
                    break;
                case "XMR":
                    icon = R.drawable.xmr;
                    break;
                case "XRP":
                    icon = R.drawable.xrp;
                    break;
                case "ZEC":
                    icon = R.drawable.zec;
                    break;
                default:
                    icon = R.drawable.btc;
            }

            String isOnServerString = cursor.getString(isOnServerIndex);
            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            String serverActionString = cursor.getString(serverActionIndex);
            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            Coin entry = new Coin(id, user, type, amount, currentConversion, Coin.Trend.NULL, icon, isOnServer, serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull Coin deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
