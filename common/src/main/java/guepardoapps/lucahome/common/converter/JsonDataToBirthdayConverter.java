package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public class JsonDataToBirthdayConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToBirthdayConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{birthday:";

    public JsonDataToBirthdayConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<LucaBirthday> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<LucaBirthday> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<LucaBirthday> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
                if (value.contains(_searchParameter)) {
                    SerializableList<LucaBirthday> list = new SerializableList<>();

                    String[] entries = value.split("\\" + _searchParameter);
                    for (String entry : entries) {
                        entry = entry.replace(_searchParameter, "").replace("};};", "");

                        String[] data = entry.split("\\};");
                        LucaBirthday newValue = parseStringToValue(data);
                        if (newValue != null) {
                            list.addValue(newValue);
                        }
                    }

                    return list;
                }
            }
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private LucaBirthday parseStringToValue(@NonNull String[] data) {
        if (data.length == 5) {
            if (data[0].contains("{id:")
                    && data[1].contains("{name:")
                    && data[2].contains("{day:")
                    && data[3].contains("{month:")
                    && data[4].contains("{year:")) {

                String idString = data[0].replace("{id:", "").replace("};", "");
                int id = Integer.parseInt(idString);

                String name = data[1].replace("{name:", "").replace("};", "");

                String dayString = data[2].replace("{day:", "").replace("};", "");
                int day = Integer.parseInt(dayString);
                String monthString = data[3].replace("{month:", "").replace("};", "");
                int month = Integer.parseInt(monthString);
                String yearString = data[4].replace("{year:", "").replace("};", "");
                int year = Integer.parseInt(yearString);

                SerializableDate birthday = new SerializableDate(year, month, day);

                LucaBirthday newValue = new LucaBirthday(id, name, birthday);
                _logger.Debug(String.format(Locale.getDefault(), "New BirthdayDto %s", newValue));

                return newValue;
            }
        }

        _logger.Error("Data has an error!");

        return null;
    }
}