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
    private Logger _logger;

    private static String _searchParameter = "{\"Temperature\":";

    public JsonDataToTemperatureConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<Temperature> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Temperature> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<Temperature> parseStringToList(@NonNull String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<Temperature> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONObject jsonObjectData = jsonObject.getJSONObject("Temperature");

                double temperatureValue = jsonObjectData.getDouble("Value");

                String area = jsonObjectData.getString("Area");

                String sensorPath = jsonObjectData.getString("SensorPath");
                String graphPath = jsonObjectData.getString("GraphPath");

                Temperature temperature = new Temperature(temperatureValue, area, new SerializableDate(), new SerializableTime(), sensorPath, Temperature.TemperatureType.RASPBERRY, graphPath);
                list.addValue(temperature);

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}