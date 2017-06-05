package guepardoapps.library.lucahome.converter.json;

import java.sql.Date;
import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.ChangeDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToChangeConverter {

    private static final String TAG = JsonDataToChangeConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{change:";

    public static SerializableList<ChangeDto> GetList(String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return ParseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return ParseStringToList(usedEntry);
        }
    }

    private static SerializableList<ChangeDto> ParseStringToList(String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<ChangeDto> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    ChangeDto newValue = ParseStringToValue(index, data);
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

    private static ChangeDto ParseStringToValue(int id, String[] data) {
        if (data.length == 7) {
            if (data[0].contains("{Type:")
                    && data[1].contains("{Hour:")
                    && data[2].contains("{Minute:")
                    && data[3].contains("{Day:")
                    && data[4].contains("{Month:")
                    && data[5].contains("{Year:")
                    && data[6].contains("{User:")) {

                String type = data[0].replace("{Type:", "").replace("};", "");

                String dayString = data[3].replace("{Day:", "").replace("};", "");
                int day = Integer.parseInt(dayString);
                String monthString = data[4].replace("{Month:", "").replace("};", "");
                int month = Integer.parseInt(monthString) - 1;
                String yearString = data[5].replace("{Year:", "").replace("};", "");
                int year = Integer.parseInt(yearString);
                @SuppressWarnings("deprecation")
                Date date = new Date(year, month, day);

                String hourString = data[1].replace("{Hour:", "").replace("};", "");
                int hour = Integer.parseInt(hourString);
                String minuteString = data[2].replace("{Minute:", "").replace("};", "");
                int minute = Integer.parseInt(minuteString);
                SerializableTime time = new SerializableTime(hour, minute, 0, 0);

                String user = data[6].replace("{User:", "").replace("};", "");

                ChangeDto newValue = new ChangeDto(id, type, date, time, user);
                _logger.Debug(String.format(Locale.getDefault(), "New ChangeDto %s", newValue));

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