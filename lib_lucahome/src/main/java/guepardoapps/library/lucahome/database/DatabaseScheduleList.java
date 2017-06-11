package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.common.enums.Weekday;

public class DatabaseScheduleList {

    private static final String TAG = DatabaseScheduleList.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String KEY_ROW_ID = "_id";

    private static final String KEY_NAME = "_name";
    private static final String KEY_INFORMATION = "_information";
    private static final String KEY_SOCKET = "_socket";
    private static final String KEY_WEEKDAY = "_weekday";
    private static final String KEY_TIME = "_time";
    private static final String KEY_ACTION = "_action";
    private static final String KEY_PLAY_SOUND = "_playSound";
    private static final String KEY_IS_ACTIVE = "_isActive";

    private static final String KEY_DRAWABLE = "_drawable";

    private static final String DATABASE_NAME = "DatabaseScheduleListDb";
    private static final String DATABASE_TABLE = "DatabaseScheduleListTable";
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
                    + KEY_NAME + " TEXT NOT NULL, "
                    + KEY_INFORMATION + " TEXT NOT NULL, "
                    + KEY_SOCKET + " TEXT NOT NULL, "
                    + KEY_WEEKDAY + " TEXT NOT NULL, "
                    + KEY_TIME + " TEXT NOT NULL, "
                    + KEY_ACTION + " TEXT NOT NULL, "
                    + KEY_PLAY_SOUND + " TEXT NOT NULL, "
                    + KEY_IS_ACTIVE + " TEXT NOT NULL, "
                    + KEY_DRAWABLE + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseScheduleList(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public DatabaseScheduleList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull ScheduleDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_INFORMATION, newEntry.GetInformation());
        contentValues.put(KEY_SOCKET, newEntry.GetSocket().GetName());
        contentValues.put(KEY_WEEKDAY, String.valueOf(newEntry.GetWeekday().GetInt()));
        contentValues.put(KEY_TIME, newEntry.GetTime().HHMMSS());
        contentValues.put(KEY_ACTION, (newEntry.GetAction() ? "1" : "0"));
        contentValues.put(KEY_PLAY_SOUND, (newEntry.GetPlaySound() ? "1" : "0"));
        contentValues.put(KEY_IS_ACTIVE, (newEntry.IsActive() ? "1" : "0"));
        contentValues.put(KEY_DRAWABLE, String.valueOf(newEntry.GetDrawable()));

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<ScheduleDto> GetScheduleList(@NonNull SerializableList<WirelessSocketDto> socketList) {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_NAME,
                KEY_INFORMATION,
                KEY_SOCKET,
                KEY_WEEKDAY,
                KEY_TIME,
                KEY_ACTION,
                KEY_PLAY_SOUND,
                KEY_IS_ACTIVE,
                KEY_DRAWABLE};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<ScheduleDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int informationIndex = cursor.getColumnIndex(KEY_INFORMATION);
        int socketIndex = cursor.getColumnIndex(KEY_SOCKET);
        int weekdayIndex = cursor.getColumnIndex(KEY_WEEKDAY);
        int timeIndex = cursor.getColumnIndex(KEY_TIME);
        int actionIndex = cursor.getColumnIndex(KEY_ACTION);
        int playSoundIndex = cursor.getColumnIndex(KEY_PLAY_SOUND);
        int isActiveIndex = cursor.getColumnIndex(KEY_IS_ACTIVE);
        int drawableIndex = cursor.getColumnIndex(KEY_DRAWABLE);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            String information = cursor.getString(informationIndex);
            String socketString = cursor.getString(socketIndex);
            String weekdayString = cursor.getString(weekdayIndex);
            String timeString = cursor.getString(timeIndex);
            String actionString = cursor.getString(actionIndex);
            String playSoundString = cursor.getString(playSoundIndex);
            String isActiveString = cursor.getString(isActiveIndex);
            String drawableString = cursor.getString(drawableIndex);

            int id = -1;
            int drawable = -1;
            int weekdayId = -1;
            try {
                id = Integer.parseInt(idString);
                drawable = Integer.parseInt(drawableString);
                weekdayId = Integer.parseInt(weekdayString);
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            WirelessSocketDto socket = new WirelessSocketDto();
            for (int index = 0; index < socketList.getSize(); index++) {
                WirelessSocketDto entry = socketList.getValue(index);
                if (entry.GetName().contains(socketString)) {
                    socket = entry;
                    break;
                }
            }

            Weekday weekday = Weekday.GetById(weekdayId);
            SerializableTime time = new SerializableTime(timeString);

            boolean action = actionString.contains("1");
            boolean playSound = playSoundString.contains("1");
            boolean isActive = isActiveString.contains("1");

            ScheduleDto entry = new ScheduleDto(
                    id,
                    name,
                    information,
                    socket,
                    weekday,
                    time,
                    action,
                    playSound,
                    isActive,
                    drawable);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(ScheduleDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
