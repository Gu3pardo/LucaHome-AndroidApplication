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
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.MeterData;

public class DatabaseMeterDataList {
    private static final String TAG = DatabaseMeterDataList.class.getSimpleName();

    private static final String KEY_ROW_ID = "_id";
    private static final String KEY_TYPE = "_type";
    private static final String KEY_TYPE_ID = "_typeId";
    private static final String KEY_SAVE_DATE = "_saveDate";
    private static final String KEY_SAVE_TIME = "_saveTime";
    private static final String KEY_METER_ID = "_meterId";
    private static final String KEY_AREA = "_area";
    private static final String KEY_VALUE = "_value";
    private static final String KEY_IMAGE_NAME = "_imageName";

    private static final String DATABASE_NAME = "DatabaseMeterDataListDb";
    private static final String DATABASE_TABLE = "DatabaseMeterDataListTable";
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
                    + KEY_TYPE + " TEXT NOT NULL, "
                    + KEY_TYPE_ID + " TEXT NOT NULL, "
                    + KEY_SAVE_DATE + " TEXT NOT NULL, "
                    + KEY_SAVE_TIME + " TEXT NOT NULL, "
                    + KEY_METER_ID + " TEXT NOT NULL, "
                    + KEY_AREA + " TEXT NOT NULL, "
                    + KEY_VALUE + " TEXT NOT NULL, "
                    + KEY_IMAGE_NAME + " TEXT NOT NULL); ");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(database);
        }
    }

    public DatabaseMeterDataList(@NonNull Context context) {
        _context = context;
    }

    public DatabaseMeterDataList Open() throws SQLException {
        _databaseHelper = new DatabaseHelper(_context);
        _database = _databaseHelper.getWritableDatabase();
        return this;
    }

    public void Close() {
        _databaseHelper.close();
    }

    public long CreateEntry(@NonNull MeterData newEntry) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, newEntry.GetId());
        contentValues.put(KEY_TYPE, newEntry.GetType());
        contentValues.put(KEY_TYPE_ID, String.valueOf(newEntry.GetTypeId()));
        contentValues.put(KEY_SAVE_DATE, newEntry.GetSaveDate().toString());
        contentValues.put(KEY_SAVE_TIME, newEntry.GetSaveTime().toString());
        contentValues.put(KEY_METER_ID, newEntry.GetMeterId());
        contentValues.put(KEY_AREA, newEntry.GetArea());
        contentValues.put(KEY_VALUE, String.valueOf(newEntry.GetValue()));
        contentValues.put(KEY_IMAGE_NAME, newEntry.GetImageName());

        return _database.insert(DATABASE_TABLE, null, contentValues);
    }

    public boolean Update(@NonNull MeterData updateEntry) throws SQLException {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_ROW_ID, updateEntry.GetId());
        contentValues.put(KEY_TYPE, updateEntry.GetType());
        contentValues.put(KEY_TYPE_ID, String.valueOf(updateEntry.GetTypeId()));
        contentValues.put(KEY_SAVE_DATE, updateEntry.GetSaveDate().toString());
        contentValues.put(KEY_SAVE_TIME, updateEntry.GetSaveTime().toString());
        contentValues.put(KEY_METER_ID, updateEntry.GetMeterId());
        contentValues.put(KEY_AREA, updateEntry.GetArea());
        contentValues.put(KEY_VALUE, String.valueOf(updateEntry.GetValue()));
        contentValues.put(KEY_IMAGE_NAME, updateEntry.GetImageName());

        _database.update(DATABASE_TABLE, contentValues, KEY_ROW_ID + "=" + updateEntry.GetId(), null);

        return true;
    }

    public SerializableList<MeterData> GetMeterDataList() {
        String[] columns = new String[]{KEY_ROW_ID, KEY_TYPE, KEY_TYPE_ID, KEY_SAVE_DATE, KEY_SAVE_TIME, KEY_METER_ID, KEY_AREA, KEY_VALUE, KEY_IMAGE_NAME};

        Cursor cursor = _database.query(DATABASE_TABLE, columns, null, null, null, null, null);
        SerializableList<MeterData> result = new SerializableList<>();

        int idIndex = cursor.getColumnIndex(KEY_ROW_ID);
        int typeIndex = cursor.getColumnIndex(KEY_TYPE);
        int typeIdIndex = cursor.getColumnIndex(KEY_TYPE_ID);
        int saveDateIndex = cursor.getColumnIndex(KEY_SAVE_DATE);
        int saveTimeIndex = cursor.getColumnIndex(KEY_SAVE_TIME);
        int meterIdIndex = cursor.getColumnIndex(KEY_METER_ID);
        int areaIndex = cursor.getColumnIndex(KEY_AREA);
        int valueIndex = cursor.getColumnIndex(KEY_VALUE);
        int imageNameIndex = cursor.getColumnIndex(KEY_IMAGE_NAME);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String idString = cursor.getString(idIndex);
            String type = cursor.getString(typeIndex);
            String typeIdString = cursor.getString(typeIdIndex);
            String saveDateString = cursor.getString(saveDateIndex);
            String saveTimeString = cursor.getString(saveTimeIndex);
            String meterId = cursor.getString(meterIdIndex);
            String area = cursor.getString(areaIndex);
            String valueString = cursor.getString(valueIndex);
            String imageName = cursor.getString(imageNameIndex);

            int id = -1;
            int typeId = -1;
            SerializableDate saveDate = new SerializableDate();
            SerializableTime saveTime = new SerializableTime();
            double value = 0;
            try {
                id = Integer.parseInt(idString);
                typeId = Integer.parseInt(typeIdString);
                saveDate = new SerializableDate(saveDateString);
                saveTime = new SerializableTime(saveTimeString);
                value = Double.parseDouble(valueString);
            } catch (Exception ex) {
                Logger.getInstance().Error(TAG, ex.getMessage());
            }

            MeterData entry = new MeterData(id, type, typeId, saveDate, saveTime, meterId, area, value, imageName);
            result.addValue(entry);
        }

        cursor.close();

        return result;
    }

    public void Delete(@NonNull MeterData deleteEntry) throws SQLException {
        _database.delete(DATABASE_TABLE, KEY_ROW_ID + "=" + deleteEntry.GetId(), null);
    }
}
