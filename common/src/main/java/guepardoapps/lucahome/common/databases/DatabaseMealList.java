package guepardoapps.lucahome.common.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.Meal;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseMealList implements IDatabaseLucaClassList<Meal> {
    private static final String Tag = DatabaseMealList.class.getSimpleName();

    public static final String KeyTitle = "_title";
    public static final String KeyDescription = "_description";
    public static final String KeyDate = "_date";
    public static final String KeyShoppingUuidList = "_shoppingUuidList";

    private static final String KeyDatabaseUuidListJson = "KeyDatabaseUuidListJson";

    private static final String DatabaseTable = "DatabaseMealListTable";

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
                    + KeyTitle + " TEXT NOT NULL, "
                    + KeyDescription + " TEXT NOT NULL, "
                    + KeyDate + " INTEGER, "
                    + KeyShoppingUuidList + " TEXT NOT NULL, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseMealList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseMealList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull Meal entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyDescription, entry.GetDescription());
        contentValues.put(KeyDate, entry.GetDate().getTimeInMillis());
        contentValues.put(KeyShoppingUuidList, getShoppingUuidListJson(entry.GetShoppingItemUuidList()));
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull Meal entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyTitle, entry.GetTitle());
        contentValues.put(KeyDescription, entry.GetDescription());
        contentValues.put(KeyDate, entry.GetDate().getTimeInMillis());
        contentValues.put(KeyShoppingUuidList, getShoppingUuidListJson(entry.GetShoppingItemUuidList()));
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull Meal entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<Meal> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyTitle,
                KeyDescription,
                KeyDate,
                KeyShoppingUuidList,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<Meal> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int titleIndex = cursor.getColumnIndex(KeyTitle);
        int descriptionIndex = cursor.getColumnIndex(KeyDescription);
        int dateIndex = cursor.getColumnIndex(KeyDate);
        int shoppingUuidListIndex = cursor.getColumnIndex(KeyShoppingUuidList);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String title = cursor.getString(titleIndex);
            String description = cursor.getString(descriptionIndex);
            int dateInteger = cursor.getInt(dateIndex);
            String shoppingUuidListString = cursor.getString(shoppingUuidListIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);
            ArrayList<UUID> shoppingUuidList = parseShoppingUuidListString(shoppingUuidListString);

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(dateInteger);

            boolean isOnServer = isOnServerInteger == 1;

            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            Meal entry = new Meal(uuid, title, description, date, shoppingUuidList, isOnServer, serverDbAction);
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

    private String getShoppingUuidListJson(ArrayList<UUID> uuidList) {
        JSONObject json = new JSONObject();
        try {
            json.put(KeyDatabaseUuidListJson, new JSONArray(uuidList));
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }
        return json.toString();
    }

    private ArrayList<UUID> parseShoppingUuidListString(String shoppingUuidListString) {
        ArrayList<UUID> shoppingUuidList = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(shoppingUuidListString);
            JSONArray jsonArray = json.optJSONArray(KeyDatabaseUuidListJson);
            for (int index = 0; index < jsonArray.length(); index++) {
                String uuidString = jsonArray.optString(index);
                UUID uuid = UUID.fromString(uuidString);
                shoppingUuidList.add(uuid);
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }

        return shoppingUuidList;
    }
}
