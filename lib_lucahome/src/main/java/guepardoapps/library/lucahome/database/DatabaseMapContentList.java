package guepardoapps.library.lucahome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;

import guepardoapps.library.lucahome.common.dto.MapContentDto;
import guepardoapps.library.lucahome.common.enums.DrawingType;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseMapContentList {

    private static final String TAG = DatabaseMapContentList.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_POSITION = "_position";
    private static final String KEY_DRAWING_TYPE = "_drawingType";
    private static final String KEY_SCHEDULES = "_schedules";
    private static final String KEY_SOCKETS = "_sockets";
    private static final String KEY_TEMPERATURE_AREA = "_temperatureArea";
    private static final String KEY_MEDIA_SERVER_IP = "_mediaServerIp";
    private static final String KEY_VISIBILITY = "_visibility";

    private static final String DATABASE_NAME = "DatabaseMapContentDb";
    private static final String DATABASE_TABLE = "DatabaseMapContentTable";
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
                    + KEY_POSITION + " TEXT NOT NULL, "
                    + KEY_DRAWING_TYPE + " TEXT NOT NULL, "
                    + KEY_SCHEDULES + " TEXT NOT NULL, "
                    + KEY_SOCKETS + " TEXT NOT NULL, "
                    + KEY_TEMPERATURE_AREA + " TEXT NOT NULL, "
                    + KEY_MEDIA_SERVER_IP + " TEXT NOT NULL, "
                    + KEY_VISIBILITY + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseMapContentList(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
    }

    public DatabaseMapContentList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(MapContentDto newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_POSITION, newEntry.GetPositionString());
        contentValues.put(KEY_DRAWING_TYPE, String.valueOf(newEntry.GetDrawingType().GetId()));
        contentValues.put(KEY_SCHEDULES, newEntry.GetSchedulesString());
        contentValues.put(KEY_SOCKETS, newEntry.GetSocketsString());
        contentValues.put(KEY_TEMPERATURE_AREA, newEntry.GetTemperatureArea());
        contentValues.put(KEY_MEDIA_SERVER_IP, newEntry.GetMediaServerIp());
        contentValues.put(KEY_VISIBILITY, (newEntry.IsVisible() ? "1" : "0"));

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<MapContentDto> GetMapContent() {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_POSITION,
                KEY_DRAWING_TYPE,
                KEY_SCHEDULES,
                KEY_SOCKETS,
                KEY_TEMPERATURE_AREA,
                KEY_MEDIA_SERVER_IP,
                KEY_VISIBILITY};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<MapContentDto> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int positionIndex = cursor.getColumnIndex(KEY_POSITION);
        int drawingTypeIndex = cursor.getColumnIndex(KEY_DRAWING_TYPE);
        int schedulesIndex = cursor.getColumnIndex(KEY_SCHEDULES);
        int socketsIndex = cursor.getColumnIndex(KEY_SOCKETS);
        int temperatureAreaIndex = cursor.getColumnIndex(KEY_TEMPERATURE_AREA);
        int mediaServerIpIndex = cursor.getColumnIndex(KEY_MEDIA_SERVER_IP);
        int visibilityIndex = cursor.getColumnIndex(KEY_VISIBILITY);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String positionString = cursor.getString(positionIndex);
            String drawingTypeString = cursor.getString(drawingTypeIndex);
            String schedulesString = cursor.getString(schedulesIndex);
            String socketsString = cursor.getString(socketsIndex);
            String temperatureArea = cursor.getString(temperatureAreaIndex);
            String mediaServerIp = cursor.getString(mediaServerIpIndex);
            String visibilityString = cursor.getString(visibilityIndex);

            int id = -1;
            int[] position = new int[]{-1, -1};
            DrawingType drawingType = DrawingType.NULL;

            try {
                id = Integer.parseInt(idString);

                String[] positionStringArray = positionString.split("\\|");
                position[0] = Integer.parseInt(positionStringArray[0].replace("|", ""));
                position[1] = Integer.parseInt(positionStringArray[1].replace("|", ""));

                drawingType = DrawingType.GetById(Integer.parseInt(drawingTypeString));
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            String[] schedulesStringArray = schedulesString.split("\\|");
            String[] socketsStringArray = socketsString.split("\\|");

            boolean visibility = visibilityString.contains("1");

            MapContentDto entry = new MapContentDto(
                    id,
                    position,
                    drawingType,
                    new ArrayList<>(Arrays.asList(schedulesStringArray)),
                    new ArrayList<>(Arrays.asList(socketsStringArray)),
                    temperatureArea,
                    mediaServerIp,
                    visibility);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(MapContentDto deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
