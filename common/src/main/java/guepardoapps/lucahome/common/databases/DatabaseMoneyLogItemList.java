package guepardoapps.lucahome.common.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.MoneyLogItem;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseMoneyLogItemList implements IDatabaseLucaClassList<MoneyLogItem> {
    public static final String KeyTypeUuid = "_typeId";
    public static final String KeyBank = "_bank";
    public static final String KeyPlan = "_plan";
    public static final String KeyAmount = "_amount";
    public static final String KeyUnit = "_unit";
    public static final String KeySaveDate = "_saveDate";
    public static final String KeyUser = "_user";

    private static final String DatabaseTable = "DatabaseMoneyLogItemListTable";

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
                    + KeyTypeUuid + " TEXT NOT NULL, "
                    + KeyBank + " TEXT NOT NULL, "
                    + KeyPlan + " TEXT NOT NULL, "
                    + KeyAmount + " REAL, "
                    + KeyUnit + " TEXT NOT NULL, "
                    + KeySaveDate + " INTEGER, "
                    + KeyUser + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseMoneyLogItemList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseMoneyLogItemList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull MoneyLogItem entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyTypeUuid, entry.GetTypeUuid().toString());
        contentValues.put(KeyBank, entry.GetBank());
        contentValues.put(KeyPlan, entry.GetPlan());
        contentValues.put(KeyAmount, entry.GetAmount());
        contentValues.put(KeyUnit, entry.GetUnit());
        contentValues.put(KeySaveDate, entry.GetSaveDate().getTimeInMillis());
        contentValues.put(KeyUser, entry.GetUser());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull MoneyLogItem entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyTypeUuid, entry.GetTypeUuid().toString());
        contentValues.put(KeyBank, entry.GetBank());
        contentValues.put(KeyPlan, entry.GetPlan());
        contentValues.put(KeyAmount, entry.GetAmount());
        contentValues.put(KeyUnit, entry.GetUnit());
        contentValues.put(KeySaveDate, entry.GetSaveDate().getTimeInMillis());
        contentValues.put(KeyUser, entry.GetUser());
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull MoneyLogItem entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<MoneyLogItem> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyTypeUuid,
                KeyBank,
                KeyPlan,
                KeyAmount,
                KeyUnit,
                KeySaveDate,
                KeyUser,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<MoneyLogItem> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int typeUuidIndex = cursor.getColumnIndex(KeyTypeUuid);
        int bankIndex = cursor.getColumnIndex(KeyBank);
        int planIndex = cursor.getColumnIndex(KeyPlan);
        int amountIndex = cursor.getColumnIndex(KeyAmount);
        int unitIndex = cursor.getColumnIndex(KeyUnit);
        int saveDateIndex = cursor.getColumnIndex(KeySaveDate);
        int userIndex = cursor.getColumnIndex(KeyUser);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String typeUuidString = cursor.getString(typeUuidIndex);
            String bank = cursor.getString(bankIndex);
            String plan = cursor.getString(planIndex);
            double amount = cursor.getFloat(amountIndex);
            String unit = cursor.getString(unitIndex);
            int saveDateInteger = cursor.getInt(saveDateIndex);
            String user = cursor.getString(userIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID typeUuid = UUID.fromString(typeUuidString);

            Calendar saveDate = Calendar.getInstance();
            saveDate.setTimeInMillis(saveDateInteger);

            boolean isOnServer = isOnServerInteger == 1;

            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            MoneyLogItem entry = new MoneyLogItem(uuid, typeUuid, bank, plan, amount, unit, saveDate, user, isOnServer, serverDbAction);
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
