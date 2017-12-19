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
    private Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToChangeConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<Change> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Change> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<Change> parseStringToList(@NonNull String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<Change> list = new SerializableList<>();

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

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}