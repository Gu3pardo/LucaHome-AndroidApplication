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
    public SerializableList<Temperature> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Temperature> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<Temperature> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            SerializableList<Temperature> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONObject jsonObjectData = jsonObject.getJSONObject("Temperature");

                double temperatureValue = jsonObjectData.getDouble("Value");

                String area = jsonObjectData.getString("Area");

                String sensorPath = jsonObjectData.getString("SensorPath");
                String graphPath = jsonObjectData.getString("GraphPath");

                Temperature temperature = new Temperature(temperatureValue, area, new SerializableDate(), new SerializableTime(), sensorPath, Temperature.TemperatureType.RASPBERRY, graphPath);
                list.addValue(temperature);

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }
}