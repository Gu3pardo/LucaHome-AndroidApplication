package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.sql.Date;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.Change;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToChangeConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToChangeConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{change:";

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
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<Change> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    Change newValue = parseStringToValue(index, data);
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

    private Change parseStringToValue(int id, @NonNull String[] data) {
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

                Change newValue = new Change(id, type, date, time, user);
                _logger.Debug(String.format(Locale.getDefault(), "New ChangeDto %s", newValue));

                return newValue;
            }
        }

        _logger.Error("Data has an error!");

        return null;
    }
}