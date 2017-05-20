package guepardoapps.library.lucahome.converter.json;

import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToSocketConverter {

    private static final String TAG = JsonDataToSocketConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{socket:";

    public static SerializableList<WirelessSocketDto> GetList(String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return ParseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return ParseStringToList(usedEntry);
        }
    }

    public static WirelessSocketDto Get(String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
            if (value.contains(_searchParameter)) {
                value = value.replace(_searchParameter, "").replace("};};", "");

                String[] data = value.split("\\};");
                WirelessSocketDto newValue = ParseStringToValue(data);
                if (newValue != null) {
                    return newValue;
                }
            }
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error(value + " has an error!");

        return null;
    }

    private static SerializableList<WirelessSocketDto> ParseStringToList(String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<WirelessSocketDto> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (String entry : entries) {
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    WirelessSocketDto newValue = ParseStringToValue(data);
                    if (newValue != null) {
                        list.addValue(newValue);
                    }
                }

                return list;
            }
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private static WirelessSocketDto ParseStringToValue(String[] data) {
        if (data.length == 4) {
            if (data[0].contains("{Name:") && data[1].contains("{Area:") && data[2].contains("{Code:")
                    && data[3].contains("{State:")) {

                String name = data[0].replace("{Name:", "").replace("};", "");
                String area = data[1].replace("{Area:", "").replace("};", "");
                String code = data[2].replace("{Code:", "").replace("};", "");

                String isActivatedString = data[3].replace("{State:", "").replace("};", "");
                boolean isActivated = isActivatedString.contains("1");

                WirelessSocketDto newValue = new WirelessSocketDto(name, area, code, isActivated);
                _logger.Debug(String.format(Locale.GERMAN, "New WirelessSocketDto %s", newValue));

                return newValue;
            }
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error("Data has an error!");

        return null;
    }
}