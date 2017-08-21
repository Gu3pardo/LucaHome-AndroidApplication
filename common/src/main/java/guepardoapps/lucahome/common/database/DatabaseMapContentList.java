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
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.classes.WirelessSocket;

public class DatabaseMapContentList {
    private static final String TAG = DatabaseMapContentList.class.getSimpleName();
    private Logger _logger;

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_POSITION = "_position";
    private static final String KEY_DRAWING_TYPE = "_drawingType";
    private static final String KEY_SOCKET = "_socket";
    private static final String KEY_TEMPERATURE = "_temperature";
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
                    + KEY_SOCKET + " TEXT NOT NULL, "
                    + KEY_TEMPERATURE + " TEXT NOT NULL, "
                    + KEY_VISIBILITY + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseMapContentList(@NonNull Context context) {
        _logger = new Logger(TAG);
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

    public long CreateEntry(@NonNull MapContent newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_POSITION, newEntry.GetPositionString());
        contentValues.put(KEY_DRAWING_TYPE, String.valueOf(newEntry.GetDrawingType()));
        contentValues.put(KEY_SOCKET, (newEntry.GetSocket() != null ? newEntry.GetSocket().GetName() : ""));
        contentValues.put(KEY_TEMPERATURE, (newEntry.GetTemperature() != null ? newEntry.GetTemperature().GetArea() : ""));
        contentValues.put(KEY_VISIBILITY, (newEntry.IsVisible() ? "1" : "0"));

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public SerializableList<MapContent> GetMapContent(SerializableList<WirelessSocket> socketList, SerializableList<Temperature> temperatureList) {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_POSITION,
                KEY_DRAWING_TYPE,
                KEY_SOCKET,
                KEY_TEMPERATURE,
                KEY_VISIBILITY};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<MapContent> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int positionIndex = cursor.getColumnIndex(KEY_POSITION);
        int drawingTypeIndex = cursor.getColumnIndex(KEY_DRAWING_TYPE);
        int socketIndex = cursor.getColumnIndex(KEY_SOCKET);
        int temperatureAreaIndex = cursor.getColumnIndex(KEY_TEMPERATURE);
        int visibilityIndex = cursor.getColumnIndex(KEY_VISIBILITY);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String positionString = cursor.getString(positionIndex);
            String drawingTypeString = cursor.getString(drawingTypeIndex);
            String socketString = cursor.getString(socketIndex);
            String temperatureArea = cursor.getString(temperatureAreaIndex);
            String visibilityString = cursor.getString(visibilityIndex);

            int id = -1;
            int[] position = new int[]{-1, -1};
            MapContent.DrawingType drawingType = MapContent.DrawingType.Null;

            try {
                id = Integer.parseInt(idString);

                String[] positionStringArray = positionString.split("\\|");
                position[0] = Integer.parseInt(positionStringArray[0].replace("|", ""));
                position[1] = Integer.parseInt(positionStringArray[1].replace("|", ""));

                for (MapContent.DrawingType drawingTypeEntry : MapContent.DrawingType.values()) {
                    if (drawingTypeEntry.toString().contentEquals(drawingTypeString)) {
                        drawingType = drawingTypeEntry;
                        break;
                    }
                }
            } catch (Exception ex) {
                _logger.Error(ex.toString());
            }

            WirelessSocket wirelessSocket = null;
            if (socketList != null) {
                for (int index = 0; index < socketList.getSize(); index++) {
                    WirelessSocket socket = socketList.getValue(index);
                    if (socket.GetName().contentEquals(socketString)) {
                        wirelessSocket = socket;
                        break;
                    }
                }
            }

            Temperature temperature = null;
            if (temperatureList != null) {
                for (int index = 0; index < temperatureList.getSize(); index++) {
                    Temperature temperatureEntry = temperatureList.getValue(index);
                    if (temperatureEntry.GetArea().contentEquals(temperatureArea)) {
                        temperature = temperatureEntry;
                        break;
                    }
                }
            }

            boolean visibility = visibilityString.contains("1");

            MapContent entry = new MapContent(
                    id,
                    position,
                    drawingType,
                    temperatureArea,
                    wirelessSocket,
                    null,
                    temperature,
                    visibility);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull MapContent deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
