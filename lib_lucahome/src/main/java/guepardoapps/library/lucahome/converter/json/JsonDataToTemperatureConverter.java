package guepardoapps.library.lucahome.converter.json;

import java.util.Calendar;
import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.enums.TemperatureType;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;

public final class JsonDataToTemperatureConverter {

    private static final String TAG = JsonDataToTemperatureConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{temperature:";

    public static SerializableList<TemperatureDto> GetList(String[] stringArray) {
        SerializableList<TemperatureDto> temperatureList = new SerializableList<>();
        for (String entry : stringArray) {
            if (entry == null || entry.length() == 0) {
                continue;
            }

            if (entry.contains(_searchParameter)) {
                entry = entry.replace(_searchParameter, "").replace("};};", "");

                String[] data = entry.split("\\};");
                TemperatureDto newValue = ParseStringArrayToValue(data);
                if (newValue != null) {
                    temperatureList.addValue(newValue);
                }
            }
        }

        return temperatureList;
    }

    public static TemperatureDto Get(String value) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (value.contains(_searchParameter)) {
            value = value.replace(_searchParameter, "").replace("};};", "");

            String[] data = value.split("\\};");
            TemperatureDto newValue = ParseStringArrayToValue(data);
            if (newValue != null) {
                return newValue;
            }
        }

        _logger.Error(value + " has an error!");
        return null;
    }

    private static TemperatureDto ParseStringArrayToValue(String[] data) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (data.length == 4) {
            if (data[0].contains("{value:") && data[1].contains("{area:") && data[2].contains("{sensorPath:")
                    && data[3].contains("{graphPath:")) {

                String tempString = data[0].replace("{value:", "").replace("};", "");
                Double temperature = Double.parseDouble(tempString);

                String area = data[1].replace("{area:", "").replace("};", "");
                String sensorPath = data[2].replace("{sensorPath:", "").replace("};", "");

                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                SerializableTime time = new SerializableTime(hour, minute, second, 0);

                String graphPath = data[3].replace("{graphPath:", "").replace("};", "");

                TemperatureDto newValue = new TemperatureDto(temperature, area, time, sensorPath,
                        TemperatureType.RASPBERRY, graphPath);
                _logger.Debug(String.format(Locale.getDefault(), "New TemperatureDto %s", newValue));

                return newValue;
            }
        }

        _logger.Error("Data has an error!");
        return null;
    }
}