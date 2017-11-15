package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToMenuConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToMenuConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{menu:";

    public JsonDataToMenuConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<LucaMenu> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<LucaMenu> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    public LucaMenu Get(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
            if (value.contains(_searchParameter)) {
                value = value.replace(_searchParameter, "").replace("};};", "");

                String[] data = value.split("\\};");
                LucaMenu newValue = parseStringToValue(-1, data);
                if (newValue != null) {
                    return newValue;
                }
            }
        }

        _logger.Error(value + " has an error!");
        return null;
    }

    private SerializableList<LucaMenu> parseStringToList(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 0) {
            if (value.contains(_searchParameter)) {
                SerializableList<LucaMenu> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    LucaMenu newValue = parseStringToValue(index, data);
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

    private LucaMenu parseStringToValue(
            int id,
            @NonNull String[] data) {
        if (data.length == 6) {
            if (data[0].contains("{weekday:")
                    && data[1].contains("{day:")
                    && data[2].contains("{month:")
                    && data[3].contains("{year:")
                    && data[4].contains("{title:")
                    && data[5].contains("{description:")) {

                String weekdayString = data[0].replace("{weekday:", "").replace("};", "");
                Weekday weekday = Weekday.GetByEnglishString(weekdayString);

                String dayString = data[1].replace("{day:", "").replace("};", "");
                String monthString = data[2].replace("{month:", "").replace("};", "");
                String yearString = data[3].replace("{year:", "").replace("};", "");
                int day = Integer.parseInt(dayString);
                int month = Integer.parseInt(monthString);
                int year = Integer.parseInt(yearString);
                SerializableDate date = new SerializableDate(year, month, day);

                String title = data[4].replace("{title:", "").replace("};", "");

                String description = data[5].replace("{description:", "").replace("};", "");
                if (description.length() == 0) {
                    description = " ";
                }

                LucaMenu newValue = new LucaMenu(id, title, description, weekday, date, true, ILucaClass.LucaServerDbAction.Null);
                _logger.Debug(String.format(Locale.getDefault(), "New LucaMenu %s", newValue));

                return newValue;
            }
        }

        _logger.Error("Data has an error!");
        return null;
    }
}