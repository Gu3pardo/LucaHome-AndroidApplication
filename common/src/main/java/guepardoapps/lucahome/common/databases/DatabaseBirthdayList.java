package guepardoapps.lucahome.common.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Birthday;
import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.utils.BitmapHelper;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class DatabaseBirthdayList implements IDatabaseLucaClassList<Birthday> {
    private static final String Tag = DatabaseBirthdayList.class.getSimpleName();

    public static final String KeyName = "_name";
    public static final String KeyBirthday = "_birthday";
    public static final String KeyGroup = "_group";
    public static final String KeyRemindMe = "_remindMe";
    public static final String KeySentMail = "_sentMail";

    private static final String DatabaseTable = "DatabaseBirthdayListTable";

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
                    + KeyBirthday + " INTEGER, "
                    + KeyGroup + " TEXT NOT NULL, "
                    + KeyRemindMe + " INTEGER, "
                    + KeySentMail + " INTEGER, "
                    + KeyIsOnServer + " INTEGER, "
                    + KeyServerAction + " INTEGER); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DatabaseTable);
            onCreate(database);
        }
    }

    public DatabaseBirthdayList(@NonNull Context context) {
        _context = context;
    }

    @Override
    public DatabaseBirthdayList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    @Override
    public void Close() {
        _databaseHelper.close();
    }

    @Override
    public long AddEntry(@NonNull Birthday entry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyUuid, entry.GetUuid().toString());
        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyBirthday, entry.GetBirthday().getTimeInMillis());
        contentValues.put(KeyGroup, entry.GetGroup());
        contentValues.put(KeyRemindMe, (entry.GetRemindMe() ? 1 : 0));
        contentValues.put(KeySentMail, (entry.GetSentMail() ? 1 : 0));
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.insert(DatabaseTable, null, contentValues);
    }

    @Override
    public long UpdateEntry(@NonNull Birthday entry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KeyName, entry.GetName());
        contentValues.put(KeyBirthday, entry.GetBirthday().getTimeInMillis());
        contentValues.put(KeyGroup, entry.GetGroup());
        contentValues.put(KeyRemindMe, (entry.GetRemindMe() ? 1 : 0));
        contentValues.put(KeySentMail, (entry.GetSentMail() ? 1 : 0));
        contentValues.put(KeyIsOnServer, (entry.GetIsOnServer() ? 1 : 0));
        contentValues.put(KeyServerAction, entry.GetServerDbAction().ordinal());

        return _database.update(DatabaseTable, contentValues, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public long DeleteEntry(@NonNull Birthday entry) throws SQLException {
        return _database.delete(DatabaseTable, KeyUuid + "=" + entry.GetUuid().toString(), null);
    }

    @Override
    public void ClearDatabase() {
        _database.delete(DatabaseTable, null, null);
    }

    @Override
    public ArrayList<Birthday> GetList(String selection, String groupBy, String orderBy) throws SQLException {
        String[] columns = new String[]{
                KeyUuid,
                KeyName,
                KeyBirthday,
                KeyGroup,
                KeyRemindMe,
                KeySentMail,
                KeyIsOnServer,
                KeyServerAction};

        Cursor cursor = _database.query(DatabaseTable, columns, selection, null, groupBy, null, orderBy);
        ArrayList<Birthday> result = new ArrayList<>();

        int uuidIndex = cursor.getColumnIndex(KeyUuid);
        int nameIndex = cursor.getColumnIndex(KeyName);
        int birthdayIndex = cursor.getColumnIndex(KeyBirthday);
        int groupIndex = cursor.getColumnIndex(KeyGroup);
        int remindMeIndex = cursor.getColumnIndex(KeyRemindMe);
        int sentMailIndex = cursor.getColumnIndex(KeySentMail);
        int isOnServerIndex = cursor.getColumnIndex(KeyIsOnServer);
        int serverActionIndex = cursor.getColumnIndex(KeyServerAction);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uuidString = cursor.getString(uuidIndex);
            String name = cursor.getString(nameIndex);
            String group = cursor.getString(groupIndex);

            int birthdayInteger = cursor.getInt(birthdayIndex);
            int remindMeInteger = cursor.getInt(remindMeIndex);
            int sentMailInteger = cursor.getInt(sentMailIndex);
            int isOnServerInteger = cursor.getInt(isOnServerIndex);
            int serverActionInteger = cursor.getInt(serverActionIndex);

            UUID uuid = UUID.fromString(uuidString);

            Calendar birthday = Calendar.getInstance();
            birthday.setTimeInMillis(birthdayInteger);

            boolean remindMe = remindMeInteger == 1;
            boolean sentMail = sentMailInteger == 1;
            boolean isOnServer = isOnServerInteger == 1;

            ILucaClass.LucaServerDbAction serverDbAction = ILucaClass.LucaServerDbAction.values()[serverActionInteger];

            Bitmap photo = BitmapFactory.decodeResource(_context.getResources(), R.mipmap.ic_face_white_48dp);
            try {
                photo = BitmapHelper.RetrieveContactPhoto(_context, name, 250, 250, true);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.getMessage());
            }

            Birthday entry = new Birthday(uuid, name, birthday, group, remindMe, sentMail, photo, isOnServer, serverDbAction);
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
