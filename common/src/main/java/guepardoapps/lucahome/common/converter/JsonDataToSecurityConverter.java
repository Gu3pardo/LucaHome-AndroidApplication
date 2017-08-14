package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.Security;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public class JsonDataToSecurityConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToSecurityConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{MotionData:";

    public JsonDataToSecurityConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<Security> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Security> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<Security> parseStringToList(@NonNull String value) {
        _logger.Debug("SerializableList<Security> parseStringToList");
        _logger.Debug(value);

        if (!value.contains("Error")) {
            if (StringHelper.GetStringCount(value, _searchParameter) > 0) {
                if (value.contains(_searchParameter)) {
                    SerializableList<Security> list = new SerializableList<>();

                    String[] entries = value.split("\\" + _searchParameter);
                    for (String entry : entries) {
                        entry = entry.replace(_searchParameter, "").replace("};};", "");

                        String[] data = entry.split("\\};");
                        Security newValue = parseStringToValue(data);
                        if (newValue != null) {
                            list.addValue(newValue);
                        } else {
                            _logger.Error("NewValue is null!");
                        }
                    }

                    return list;
                } else {
                    _logger.Error("Value does not contain " + _searchParameter);
                }
            } else {
                _logger.Error("Value does not contain more then 0x " + _searchParameter);
            }
        } else {
            _logger.Error("value contains error!");
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private Security parseStringToValue(@NonNull String[] data) {
        _logger.Debug("Security parseStringToValue");
        _logger.Debug("Data has size " + String.valueOf(data.length));

        if (data.length == 4) {
            if (data[0].contains("{State:")
                    && data[1].contains("{URL:")
                    && data[2].contains("{MotionEvents:")
                    && data[3].contains("{Control:")) {

                String isCameraActiveString = data[0].replace("{State:", "").replace("};", "");
                boolean isCameraActive = isCameraActiveString.contains("1");

                String isMotionControlActiveString = data[3].replace("{Control:", "").replace("};", "");
                boolean isMotionControlActive = isMotionControlActiveString.contains("1");

                String url = data[1].replace("{URL:", "").replace("};", "");

                String registeredEventsString = data[2].replace("{MotionEvents:", "").replace("};", "");
                String[] registeredEventsData = registeredEventsString.split("\\},");

                SerializableList<String> registeredEvents = new SerializableList<>();
                for (String registeredEvent : registeredEventsData) {
                    registeredEvent = registeredEvent.replace("{Event:", "").replace("},", "");
                    registeredEvents.addValue(registeredEvent);
                }

                Security newValue = new Security(isCameraActive, isMotionControlActive, url, registeredEvents);
                _logger.Debug(String.format(Locale.getDefault(), "New Security %s", newValue));

                return newValue;
            } else {
                _logger.Error("Data contains invalid parameter");
            }
        } else {
            _logger.Error("Data has invalid length " + String.valueOf(data.length));
        }

        _logger.Error("Data has an error!");

        return null;
    }
}
