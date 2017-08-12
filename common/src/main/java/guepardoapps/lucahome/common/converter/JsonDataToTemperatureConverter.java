package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToTemperatureConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToTemperatureConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{temperature:";

    public JsonDataToTemperatureConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<Temperature> GetList(@NonNull String[] stringArray) {
        SerializableList<Temperature> temperatureList = new SerializableList<>();
        for (String entry : stringArray) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            if (entry.contains(_searchParameter)) {
                entry = entry.replace(_searchParameter, "").replace("};};", "");

                String[] data = entry.split("\\};");
                Temperature newValue = parseStringArrayToValue(data);
                if (newValue != null) {
                    temperatureList.addValue(newValue);
                }
            }
        }

        return temperatureList;
    }

    @Override
    public SerializableList<Temperature> GetList(@NonNull String jsonString) {
        SerializableList<Temperature> temperatureList = new SerializableList<>();

        if (jsonString.contains(_searchParameter)) {
            jsonString = jsonString.replace(_searchParameter, "").replace("};};", "");

            String[] data = jsonString.split("\\};");
            Temperature newValue = parseStringArrayToValue(data);
            if (newValue != null) {
                temperatureList.addValue(newValue);
            }
        }

        return temperatureList;
    }

    public Temperature Get(@NonNull String value) {
        if (value.contains(_searchParameter)) {
            value = value.replace(_searchParameter, "").replace("};};", "");

            String[] data = value.split("\\};");
            Temperature newValue = parseStringArrayToValue(data);
            if (newValue != null) {
                return newValue;
            }
        }

        _logger.Error(value + " has an error!");
        return null;
    }

    private Temperature parseStringArrayToValue(@NonNull String[] data) {
        if (data.length == 4) {
            if (data[0].contains("{value:")
                    && data[1].contains("{area:")
                    && data[2].contains("{sensorPath:")
                    && data[3].contains("{graphPath:")) {

                String tempString = data[0].replace("{value:", "").replace("};", "");
                Double temperature = Double.parseDouble(tempString);

                String area = data[1].replace("{area:", "").replace("};", "");
                String sensorPath = data[2].replace("{sensorPath:", "").replace("};", "");

                SerializableDate date = new SerializableDate();
                SerializableTime time = new SerializableTime();

                String graphPath = data[3].replace("{graphPath:", "").replace("};", "");

                Temperature newValue = new Temperature(
                        temperature,
                        area,
                        date,
                        time,
                        sensorPath,
                        Temperature.TemperatureType.RASPBERRY,
                        graphPath);
                _logger.Debug(String.format(Locale.getDefault(), "New TemperatureDto %s", newValue));
                return newValue;
            }
        }

        _logger.Error("Data has an error!");
        return null;
    }
}