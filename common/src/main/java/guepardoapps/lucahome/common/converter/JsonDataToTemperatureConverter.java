package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;
import guepardoapps.lucahome.common.service.TemperatureService;

public final class JsonDataToTemperatureConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToTemperatureConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Temperature\":";

    private static final JsonDataToTemperatureConverter SINGLETON = new JsonDataToTemperatureConverter();

    public static JsonDataToTemperatureConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToTemperatureConverter() {
    }

    @Override
    public SerializableList<Temperature> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Temperature> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<Temperature> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            SerializableList<Temperature> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONObject jsonObjectData = jsonObject.getJSONObject("Temperature");

                double temperatureValue = jsonObjectData.getDouble("Value");

                String area = jsonObjectData.getString("Area");

                String sensorPath = jsonObjectData.getString("SensorPath");
                String graphPath = jsonObjectData.getString("GraphPath");

                Temperature temperature = new Temperature(TemperatureService.getInstance().GetHighestId() + 1, temperatureValue, area, new SerializableDate(), new SerializableTime(), sensorPath, Temperature.TemperatureType.RASPBERRY, graphPath);
                list.addValue(temperature);

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(TAG, jsonString + " has an error!");
        return new SerializableList<>();
    }
}