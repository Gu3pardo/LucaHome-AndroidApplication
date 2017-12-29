package guepardoapps.lucahome.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.MoneyMeterData;

public class DatabaseMoneyMeterDataList {
    private static final String TAG = DatabaseMoneyMeterDataList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_TYPE_ID = "_typeId";
    private static final String KEY_BANK = "_bank";
    private static final String KEY_PLAN = "_plan";
    private static final String KEY_AMOUNT = "_amount";
    private static final String KEY_UNIT = "_unit";
    private static final String KEY_SAVE_DATE = "_saveDate";
    private static final String KEY_USER = "_ser";

    private static final String DATABASE_NAME = "DatabaseMoneyMeterDataListDb";
    private static final String DATABASE_TABLE = "DatabaseMoneyMeterDataListTable";
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
                    + KEY_TYPE_ID + " TEXT NOT NULL, "
                    + KEY_BANK + " TEXT NOT NULL, "
                    + KEY_PLAN + " TEXT NOT NULL, "
                    + KEY_AMOUNT + " TEXT NOT NULL, "
                    + KEY_UNIT + " TEXT NOT NULL, "
                    + KEY_SAVE_DATE + " TEXT NOT NULL, "
                    + KEY_USER + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseMoneyMeterDataList(@NonNull Context context) {
        _context = context;
    }

    public DatabaseMoneyMeterDataList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull MoneyMeterData newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_TYPE_ID, String.valueOf(newEntry.GetTypeId()));
        contentValues.put(KEY_BANK, newEntry.GetBank());
        contentValues.put(KEY_PLAN, newEntry.GetPlan());
        contentValues.put(KEY_AMOUNT, String.valueOf(newEntry.GetAmount()));
        contentValues.put(KEY_UNIT, newEntry.GetUnit());
        contentValues.put(KEY_SAVE_DATE, newEntry.GetSaveDate().toString());
        contentValues.put(KEY_USER, newEntry.GetUser());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull MoneyMeterData updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_TYPE_ID, String.valueOf(updateEntry.GetTypeId()));
        contentValues.put(KEY_BANK, updateEntry.GetBank());
        contentValues.put(KEY_PLAN, updateEntry.GetPlan());
        contentValues.put(KEY_AMOUNT, String.valueOf(updateEntry.GetAmount()));
        contentValues.put(KEY_UNIT, updateEntry.GetUnit());
        contentValues.put(KEY_SAVE_DATE, updateEntry.GetSaveDate().toString());
        contentValues.put(KEY_USER, updateEntry.GetUser());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<MoneyMeterData> GetMoneyMeterDataList() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_TYPE_ID, KEY_BANK, KEY_PLAN, KEY_AMOUNT, KEY_UNIT, KEY_SAVE_DATE, KEY_USER};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<MoneyMeterData> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int typeIdIndex = cursor.getColumnIndex(KEY_TYPE_ID);
        int bankIndex = cursor.getColumnIndex(KEY_BANK);
        int planIndex = cursor.getColumnIndex(KEY_PLAN);
        int amountIndex = cursor.getColumnIndex(KEY_AMOUNT);
        int unitIndex = cursor.getColumnIndex(KEY_UNIT);
        int saveDateIndex = cursor.getColumnIndex(KEY_SAVE_DATE);
        int userIndex = cursor.getColumnIndex(KEY_USER);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String typeIdString = cursor.getString(typeIdIndex);
            String bank = cursor.getString(bankIndex);
            String plan = cursor.getString(planIndex);
            String amountString = cursor.getString(amountIndex);
            String unit = cursor.getString(unitIndex);
            String saveDateString = cursor.getString(saveDateIndex);
            String user = cursor.getString(userIndex);

            int id = -1;
            int typeId = -1;
            SerializableDate saveDate = new SerializableDate();
            double amount = 0;
            try {
                id = Integer.parseInt(idString);
                typeId = Integer.parseInt(typeIdString);
                saveDate = new SerializableDate(saveDateString);
                amount = Double.parseDouble(amountString);
            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }

            MoneyMeterData entry = new MoneyMeterData(id, typeId, bank, plan, amount, unit, saveDate, user);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull MoneyMeterData deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
