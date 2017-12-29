package guepardoapps.lucahome.common.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabaseBirthdayList {
    private static final String TAG = DatabaseBirthdayList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_NAME = "_name";
    private static final String KEY_DATE = "_date";
    private static final String KEY_GROUP = "_group";
    private static final String KEY_REMIND_ME = "_remindMe";
    private static final String KEY_SENT_MAIL = "_sentMail";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabaseBirthdayListDb";
    private static final String DATABASE_TABLE = "DatabaseBirthdayListTable";
    private static final int DATABASE_VERSION = 4;

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
                    + KEY_DATE + " TEXT NOT NULL, "
                    + KEY_GROUP + " TEXT NOT NULL, "
                    + KEY_REMIND_ME + " TEXT NOT NULL, "
                    + KEY_SENT_MAIL + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseBirthdayList(@NonNull Context context) {
        _context = context;
    }

    public DatabaseBirthdayList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull LucaBirthday newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_DATE, newEntry.GetDate().DDMMYYYY());
        contentValues.put(KEY_GROUP, newEntry.GetGroup());
        contentValues.put(KEY_REMIND_ME, (newEntry.GetRemindMe() ? "1" : "0"));
        contentValues.put(KEY_SENT_MAIL, (newEntry.GetSentMail() ? "1" : "0"));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull LucaBirthday updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_NAME, updateEntry.GetName());
        contentValues.put(KEY_DATE, updateEntry.GetDate().DDMMYYYY());
        contentValues.put(KEY_GROUP, updateEntry.GetGroup());
        contentValues.put(KEY_REMIND_ME, (updateEntry.GetRemindMe() ? "1" : "0"));
        contentValues.put(KEY_SENT_MAIL, (updateEntry.GetSentMail() ? "1" : "0"));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<LucaBirthday> GetBirthdayList() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_NAME,
                KEY_DATE,
                KEY_GROUP,
                KEY_REMIND_ME,
                KEY_SENT_MAIL,
                KEY_IS_ON_SERVER,
                KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<LucaBirthday> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int dateIndex = cursor.getColumnIndex(KEY_DATE);
        int groupIndex = cursor.getColumnIndex(KEY_GROUP);
        int remindMeIndex = cursor.getColumnIndex(KEY_REMIND_ME);
        int sentMailIndex = cursor.getColumnIndex(KEY_SENT_MAIL);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String dateString = cursor.getString(dateIndex);
            String group = cursor.getString(groupIndex);
            String remindMeString = cursor.getString(remindMeIndex);
            String sentMailString = cursor.getString(sentMailIndex);
            SerializableDate date = new SerializableDate();

            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }

            String[] dateStringArray = dateString.split("\\.");
            if (dateStringArray.length == 3) {
                String dayString = dateStringArray[0].replace(".", "");
                String monthString = dateStringArray[1].replace(".", "");
                String yearString = dateStringArray[2].replace(".", "");

                try {
                    int day = Integer.parseInt(dayString);
                    int month = Integer.parseInt(monthString);
                    int year = Integer.parseInt(yearString);

                    date = new SerializableDate(year, month, day);
                } catch (Exception ex) {
                    Logger.getInstance().Error(TAG, ex.getMessage());
                }
            } else {
                Logger.getInstance().Error(TAG, "Size of birthdayStringArray has invalid size " + String.valueOf(dateStringArray.length));
            }

            boolean remindMe = remindMeString.contains("1");
            boolean sentMail = sentMailString.contains("1");

            Bitmap photo = BitmapFactory.decodeResource(_context.getResources(), guepardoapps.lucahome.basic.R.mipmap.ic_face_white_48dp);
            try {
                photo = Tools.RetrieveContactPhoto(_context, name, 250, 250, true);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());
            }

            String isOnServerString = cursor.getString(isOnServerIndex);
            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            String serverActionString = cursor.getString(serverActionIndex);
            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            LucaBirthday entry = new LucaBirthday(id, name, date, group, remindMe, sentMail, photo, isOnServer, serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull LucaBirthday deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
