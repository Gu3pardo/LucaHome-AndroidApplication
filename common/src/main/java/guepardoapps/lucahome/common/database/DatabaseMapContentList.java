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
import guepardoapps.lucahome.common.classes.MediaServerData;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class DatabaseMapContentList {
    private static final String TAG = DatabaseMapContentList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_DRAWING_TYPE = "_drawingType";
    private static final String KEY_DRAWING_TYPE_ID = "_drawingTypeId";
    private static final String KEY_POSITION = "_position";
    private static final String KEY_NAME = "_name";
    private static final String KEY_SHORT_NAME = "_shortName";
    private static final String KEY_AREA = "_area";
    private static final String KEY_VISIBILITY = "_visibility";
    private static final String KEY_MEDIA_SERVER_ID = "_mediaServerId";
    private static final String KEY_TEMPERATURE_AREA = "_temperatureArea";
    private static final String KEY_WIRELESS_SOCKET_NAME = "_wirelessSocketName";
    private static final String KEY_WIRELESS_SWITCH_NAME = "_wirelessSwitchName";
    private static final String KEY_IS_ON_SERVER = "_isOnServer";
    private static final String KEY_SERVER_ACTION = "_serverAction";

    private static final String DATABASE_NAME = "DatabaseMapContentDb";
    private static final String DATABASE_TABLE = "DatabaseMapContentTable";
    private static final int DATABASE_VERSION = 3;

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
                    + KEY_DRAWING_TYPE + " TEXT NOT NULL, "
                    + KEY_DRAWING_TYPE_ID + " TEXT NOT NULL, "
                    + KEY_POSITION + " TEXT NOT NULL, "
                    + KEY_NAME + " TEXT NOT NULL, "
                    + KEY_SHORT_NAME + " TEXT NOT NULL, "
                    + KEY_AREA + " TEXT NOT NULL, "
                    + KEY_VISIBILITY + " TEXT NOT NULL, "
                    + KEY_MEDIA_SERVER_ID + " TEXT NOT NULL, "
                    + KEY_TEMPERATURE_AREA + " TEXT NOT NULL, "
                    + KEY_WIRELESS_SOCKET_NAME + " TEXT NOT NULL, "
                    + KEY_WIRELESS_SWITCH_NAME + " TEXT NOT NULL, "
                    + KEY_IS_ON_SERVER + " TEXT NOT NULL, "
                    + KEY_SERVER_ACTION + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseMapContentList(@NonNull Context context) {
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
        contentValues.put(KEY_DRAWING_TYPE, String.valueOf(newEntry.GetDrawingType()));
        contentValues.put(KEY_DRAWING_TYPE_ID, String.valueOf(newEntry.GetDrawingTypeId()));
        contentValues.put(KEY_POSITION, newEntry.GetPositionString());
        contentValues.put(KEY_NAME, newEntry.GetName());
        contentValues.put(KEY_SHORT_NAME, newEntry.GetShortName());
        contentValues.put(KEY_AREA, newEntry.GetArea());
        contentValues.put(KEY_VISIBILITY, (newEntry.IsVisible() ? "1" : "0"));
        contentValues.put(KEY_MEDIA_SERVER_ID, String.valueOf(newEntry.GetMediaServer() != null ? newEntry.GetMediaServer().GetMediaServerSelection().GetId() : -1));
        contentValues.put(KEY_TEMPERATURE_AREA, (newEntry.GetTemperature() != null ? newEntry.GetTemperature().GetArea() : ""));
        contentValues.put(KEY_WIRELESS_SOCKET_NAME, (newEntry.GetWirelessSocket() != null ? newEntry.GetWirelessSocket().GetName() : ""));
        contentValues.put(KEY_WIRELESS_SWITCH_NAME, (newEntry.GetWirelessSwitch() != null ? newEntry.GetWirelessSwitch().GetName() : ""));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(newEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, newEntry.GetServerDbAction().toString());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull MapContent updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_DRAWING_TYPE, String.valueOf(updateEntry.GetDrawingType()));
        contentValues.put(KEY_DRAWING_TYPE_ID, String.valueOf(updateEntry.GetDrawingTypeId()));
        contentValues.put(KEY_POSITION, updateEntry.GetPositionString());
        contentValues.put(KEY_NAME, updateEntry.GetName());
        contentValues.put(KEY_SHORT_NAME, updateEntry.GetShortName());
        contentValues.put(KEY_AREA, updateEntry.GetArea());
        contentValues.put(KEY_VISIBILITY, (updateEntry.IsVisible() ? "1" : "0"));
        contentValues.put(KEY_MEDIA_SERVER_ID, String.valueOf(updateEntry.GetMediaServer() != null ? updateEntry.GetMediaServer().GetMediaServerSelection().GetId() : -1));
        contentValues.put(KEY_TEMPERATURE_AREA, (updateEntry.GetTemperature() != null ? updateEntry.GetTemperature().GetArea() : ""));
        contentValues.put(KEY_WIRELESS_SOCKET_NAME, (updateEntry.GetWirelessSocket() != null ? updateEntry.GetWirelessSocket().GetName() : ""));
        contentValues.put(KEY_WIRELESS_SWITCH_NAME, (updateEntry.GetWirelessSwitch() != null ? updateEntry.GetWirelessSwitch().GetName() : ""));
        contentValues.put(KEY_IS_ON_SERVER, String.valueOf(updateEntry.GetIsOnServer()));
        contentValues.put(KEY_SERVER_ACTION, updateEntry.GetServerDbAction().toString());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<MapContent> GetMapContent(
            @NonNull SerializableList<MediaServerData> mediaServerDataList,
            @NonNull SerializableList<Temperature> temperatureList,
            @NonNull SerializableList<WirelessSocket> wirelessSocketList,
            @NonNull SerializableList<WirelessSwitch> wirelessSwitchList) {
        String[] columns = new String[]{
                KEY_ROW_ID,
                KEY_DRAWING_TYPE,
                KEY_DRAWING_TYPE_ID,
                KEY_POSITION,
                KEY_NAME,
                KEY_SHORT_NAME,
                KEY_AREA,
                KEY_VISIBILITY,
                KEY_MEDIA_SERVER_ID,
                KEY_TEMPERATURE_AREA,
                KEY_WIRELESS_SOCKET_NAME,
                KEY_WIRELESS_SWITCH_NAME,
                KEY_IS_ON_SERVER,
                KEY_SERVER_ACTION};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<MapContent> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int drawingTypeIndex = cursor.getColumnIndex(KEY_DRAWING_TYPE);
        int drawingTypeIdIndex = cursor.getColumnIndex(KEY_DRAWING_TYPE_ID);
        int positionIndex = cursor.getColumnIndex(KEY_POSITION);
        int nameIndex = cursor.getColumnIndex(KEY_NAME);
        int shortNameIndex = cursor.getColumnIndex(KEY_SHORT_NAME);
        int areaIndex = cursor.getColumnIndex(KEY_AREA);
        int visibilityIndex = cursor.getColumnIndex(KEY_VISIBILITY);
        int mediaServerIdIndex = cursor.getColumnIndex(KEY_MEDIA_SERVER_ID);
        int temperatureAreaIndex = cursor.getColumnIndex(KEY_TEMPERATURE_AREA);
        int wirelessSocketNameIndex = cursor.getColumnIndex(KEY_WIRELESS_SOCKET_NAME);
        int wirelessSwitchNameIndex = cursor.getColumnIndex(KEY_WIRELESS_SWITCH_NAME);
        int isOnServerIndex = cursor.getColumnIndex(KEY_IS_ON_SERVER);
        int serverActionIndex = cursor.getColumnIndex(KEY_SERVER_ACTION);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String drawingTypeString = cursor.getString(drawingTypeIndex);
            String drawingTypeIdString = cursor.getString(drawingTypeIdIndex);
            String positionString = cursor.getString(positionIndex);
            String name = cursor.getString(nameIndex);
            String shortName = cursor.getString(shortNameIndex);
            String area = cursor.getString(areaIndex);
            String visibilityString = cursor.getString(visibilityIndex);
            String mediaServerIdString = cursor.getString(mediaServerIdIndex);
            String temperatureArea = cursor.getString(temperatureAreaIndex);
            String wirelessSocketString = cursor.getString(wirelessSocketNameIndex);
            String wirelessSwitchString = cursor.getString(wirelessSwitchNameIndex);
            String isOnServerString = cursor.getString(isOnServerIndex);
            String serverActionString = cursor.getString(serverActionIndex);

            int id = -1;
            MapContent.DrawingType drawingType = MapContent.DrawingType.Null;
            int drawingTypeId = -1;
            int[] position = new int[]{-1, -1};

            int mediaServerId = -1;

            try {
                id = Integer.parseInt(idString);

                for (MapContent.DrawingType drawingTypeEntry : MapContent.DrawingType.values()) {
                    if (drawingTypeEntry.toString().contentEquals(drawingTypeString)) {
                        drawingType = drawingTypeEntry;
                        break;
                    }
                }

                drawingTypeId = Integer.parseInt(drawingTypeIdString);

                String[] positionStringArray = positionString.split("\\|");
                position[0] = Integer.parseInt(positionStringArray[0].replace("|", ""));
                position[1] = Integer.parseInt(positionStringArray[1].replace("|", ""));

                mediaServerId = Integer.parseInt(mediaServerIdString);

            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }

            boolean visibility = visibilityString.contains("1");

            MediaServerData mediaServerData = null;
            Temperature temperature = null;
            WirelessSocket wirelessSocket = null;
            WirelessSwitch wirelessSwitch = null;

            for (int index = 0; index < mediaServerDataList.getSize(); index++) {
                MediaServerData currentMediaServerData = mediaServerDataList.getValue(index);
                if (currentMediaServerData.GetMediaServerSelection().GetId() == mediaServerId) {
                    mediaServerData = currentMediaServerData;
                    break;
                }
            }
            for (int index = 0; index < temperatureList.getSize(); index++) {
                Temperature temperatureEntry = temperatureList.getValue(index);
                if (temperatureEntry.GetArea().contentEquals(temperatureArea)) {
                    temperature = temperatureEntry;
                    break;
                }
            }
            for (int index = 0; index < wirelessSocketList.getSize(); index++) {
                WirelessSocket currentWirelessSocket = wirelessSocketList.getValue(index);
                if (currentWirelessSocket.GetName().contentEquals(wirelessSocketString)) {
                    wirelessSocket = currentWirelessSocket;
                    break;
                }
            }
            for (int index = 0; index < wirelessSwitchList.getSize(); index++) {
                WirelessSwitch currentWirelessSwitch = wirelessSwitchList.getValue(index);
                if (currentWirelessSwitch.GetName().contentEquals(wirelessSwitchString)) {
                    wirelessSwitch = currentWirelessSwitch;
                    break;
                }
            }

            boolean isOnServer = Boolean.getBoolean(isOnServerString);

            ILucaClass.LucaServerDbAction serverAction = ILucaClass.LucaServerDbAction.valueOf(serverActionString);

            MapContent entry = new MapContent(
                    id,
                    drawingType,
                    drawingTypeId,
                    position,
                    name,
                    shortName,
                    area,
                    visibility,
                    null,
                    null,
                    null,
                    mediaServerData,
                    null,
                    temperature,
                    wirelessSocket,
                    wirelessSwitch,
                    isOnServer,
                    serverAction);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull MapContent deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
