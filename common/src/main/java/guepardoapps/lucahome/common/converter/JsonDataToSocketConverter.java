package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToSocketConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToSocketConverter.class.getSimpleName();
    private static Logger _logger;

    private static String _searchParameter = "{socket:";

    public JsonDataToSocketConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<WirelessSocket> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<WirelessSocket> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    public WirelessSocket Get(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
            if (value.contains(_searchParameter)) {
                value = value.replace(_searchParameter, "").replace("};};", "");

                String[] data = value.split("\\};");
                WirelessSocket newValue = parseStringToValue(-1, data);
                if (newValue != null) {
                    return newValue;
                }
            }
        }

        _logger.Error(value + " has an error!");
        return null;
    }

    private static SerializableList<WirelessSocket> parseStringToList(String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<WirelessSocket> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    WirelessSocket newValue = parseStringToValue(index, data);
                    if (newValue != null) {
                        list.addValue(newValue);
                    }
                }

                return list;
            }
        }

        _logger.Error(value + " has an error!");
        return new SerializableList<>();
    }

    private static WirelessSocket parseStringToValue(
            int id,
            @NonNull String[] data) {
        if (data.length == 4) {
            if (data[0].contains("{Name:")
                    && data[1].contains("{Area:")
                    && data[2].contains("{Code:")
                    && data[3].contains("{State:")) {

                String name = data[0].replace("{Name:", "").replace("};", "");
                String area = data[1].replace("{Area:", "").replace("};", "");
                String code = data[2].replace("{Code:", "").replace("};", "");

                String isActivatedString = data[3].replace("{State:", "").replace("};", "");
                boolean isActivated = isActivatedString.contains("1");

                WirelessSocket newValue = new WirelessSocket(id, name, area, code, isActivated);
                _logger.Debug(String.format(Locale.getDefault(), "New WirelessSocketDto %s", newValue));
                return newValue;
            }
        }

        _logger.Error("Data has an error!");
        return null;
    }
}