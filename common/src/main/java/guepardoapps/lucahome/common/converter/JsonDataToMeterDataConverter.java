package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.MeterData;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToMeterDataConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToMeterDataConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToMeterDataConverter SINGLETON = new JsonDataToMeterDataConverter();

    public static JsonDataToMeterDataConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToMeterDataConverter() {
    }

    @Override
    public SerializableList<MeterData> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<MeterData> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<MeterData> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            SerializableList<MeterData> list = new SerializableList<>();

            try {

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("MeterData");

                    int id = child.getInt("Id");

                    String type = child.getString("Type");
                    int typeId = child.getInt("TypeId");

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    JSONObject jsonTime = child.getJSONObject("Time");

                    int hour = jsonTime.getInt("Hour");
                    int minute = jsonTime.getInt("Minute");

                    String meterId = child.getString("MeterId");
                    String area = child.getString("Area");

                    double value = child.getDouble("Value");
                    String imageName = child.getString("ImageName");

                    MeterData newMeterData = new MeterData(id, type, typeId, new SerializableDate(year, month, day), new SerializableTime(hour, minute, 0, 0), meterId, area, value, imageName);
                    list.addValue(newMeterData);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(TAG, jsonString + " has an error!");
        return new SerializableList<>();
    }
}