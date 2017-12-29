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
import guepardoapps.lucahome.common.classes.Change;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToChangeConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToChangeConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToChangeConverter SINGLETON = new JsonDataToChangeConverter();

    public static JsonDataToChangeConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToChangeConverter() {
    }

    @Override
    public SerializableList<Change> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Change> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<Change> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            SerializableList<Change> list = new SerializableList<>();

            try {

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Change");

                    String type = child.getString("Type");
                    String user = child.getString("UserName");

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    JSONObject jsonTime = child.getJSONObject("Time");

                    int hour = jsonTime.getInt("Hour");
                    int minute = jsonTime.getInt("Minute");

                    Change newChange = new Change(dataIndex, type, new SerializableDate(year, month, day), new SerializableTime(hour, minute, 0, 0), user);
                    list.addValue(newChange);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }
}