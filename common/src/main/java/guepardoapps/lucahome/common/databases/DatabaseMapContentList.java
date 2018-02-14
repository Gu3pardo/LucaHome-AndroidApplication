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
import guepardoapps.lucahome.common.classes.MapContent;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseMapContentList implements IDatabaseLucaClassList<MapContent> {
    public static final String KeyDrawingTypeUuid = "_drawingTypeUuid";
    public static final String KeyDrawingType = "_drawingType";
    public static final String KeyPosition = "_position";
    public static final String KeyName = "_name";
    public static final String KeyShortName = "_shortName";
    public static final String KeyVisibility = "_visibility";

    private static final String DatabaseTable = "DatabaseMapContentListTable";

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
                    + KeyDrawingTypeUuid + " TEXT NOT NULL, "
                    + KeyDrawingType + " INTEGER, "
                    + KeyPosition + " TEXT NOT NULL, "
                    + KeyName + " TEXT NOT NULL, "
                    + KeyShortName + " TEXT NOT NULL, "
                    + KeyVisibility + " INTEGER, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseMapContentList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseMapContentList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull MapContent entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyDrawingTypeUuid, entry.GetDrawingTypeUuid().toString());
        contentValues.put(KeyDrawingType, entry.GetDrawingType().ordinal());
        contentValues.put(KeyPosition, entry.GetPositionString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyShortName, entry.GetShortName());
        contentValues.put(KeyVisibility, entry.IsVisible() ? 1 : 0);
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull MapContent entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyDrawingTypeUuid, entry.GetDrawingTypeUuid().toString());
        contentValues.put(KeyDrawingType, entry.GetDrawingType().ordinal());
        contentValues.put(KeyPosition, entry.GetPositionString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyShortName, entry.GetShortName());
        contentValues.put(KeyVisibility, entry.IsVisible() ? 1 : 0);
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull MapContent entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<MapContent> GetList(String selection, String groupBy, String orderBy) {
        String[] columns = new String[]{
                KeyUuid,
                KeyDrawingTypeUuid,
                KeyDrawingType,
                KeyPosition,
                KeyName,
                KeyShortName,
                KeyVisibility,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<MapContent> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int drawingTypeUuidIndex = cursor.getColumnIndex(KeyDrawingTypeUuid);
        int drawingTypeIndex = cursor.getColumnIndex(KeyDrawingType);
        int positionIndex = cursor.getColumnIndex(KeyPosition);
        int nameIndex = cursor.getColumnIndex(KeyName);
        int shortNameIndex = cursor.getColumnIndex(KeyShortName);
        int visibilityIndex = cursor.getColumnIndex(KeyVisibility);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String drawingTypeUuidString = cursor.getString(drawingTypeUuidIndex);
            int drawingTypeInteger = cursor.getInt(drawingTypeIndex);
            String positionString = cursor.getString(positionIndex);
            String name = cursor.getString(nameIndex);
            String shortName = cursor.getString(shortNameIndex);
            int visibilityInteger = cursor.getInt(visibilityIndex);

            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);
            UUID drawingTypeUuid = UUID.fromString(drawingTypeUuidString);

            MapContent.DrawingType drawingType = MapContent.DrawingType.values()[drawingTypeInteger];

            int[] position = new int[]{-1, -1};
            String[] positionStringArray = positionString.split("\\|");
            position[0] = Integer.parseInt(positionStringArray[0].replace("|", ""));
            position[1] = Integer.parseInt(positionStringArray[1].replace("|", ""));

            boolean visibility = visibilityInteger == 1;
            boolean isOnServer = isOnServerInteger == 1;

            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            MapContent entry = new MapContent(uuid, drawingTypeUuid, drawingType, position, name, shortName, visibility, isOnServer, serverDbAction);
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
