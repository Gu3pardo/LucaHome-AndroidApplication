package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.ListedMenu;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToListedMenuConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToListedMenuConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{listedmenu:";

    public JsonDataToListedMenuConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<ListedMenu> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            _logger.Debug("StringsAreEqual");
            return parseStringToList(stringArray[0]);
        } else {
            _logger.Debug("SelectString");
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            _logger.Debug("usedEntry: " + usedEntry);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<ListedMenu> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    public ListedMenu Get(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
            if (value.contains(_searchParameter)) {
                value = value.replace(_searchParameter, "").replace("};};", "");

                String[] data = value.split("\\};");
                ListedMenu newValue = parseStringToValue(data);
                if (newValue != null) {
                    return newValue;
                } else {
                    _logger.Error("NewValue is null!");
                }
            } else {
                _logger.Error(String.format("String %s doesnot contain %s!", value, _searchParameter));
            }
        } else {
            _logger.Error(String.format(Locale.getDefault(), "String %s contains %d x %s!", value,
                    StringHelper.GetStringCount(value, _searchParameter), _searchParameter));
        }

        _logger.Error(value + " has an error!");
        return null;
    }

    private SerializableList<ListedMenu> parseStringToList(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<ListedMenu> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (String entry : entries) {
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    ListedMenu newValue = parseStringToValue(data);
                    if (newValue != null) {
                        list.addValue(newValue);
                    } else {
                        _logger.Error("NewValue is null!");
                    }
                }
                return list;
            } else {
                _logger.Error(String.format("String %s doesnot contain %s!", value, _searchParameter));
            }
        } else {
            _logger.Error(String.format(Locale.getDefault(), "String %s contains %d x %s!", value,
                    StringHelper.GetStringCount(value, _searchParameter), _searchParameter));
        }

        _logger.Error(value + " has an error!");
        return new SerializableList<>();
    }

    private ListedMenu parseStringToValue(@NonNull String[] data) {
        if (data.length == 4) {
            if (data[0].contains("{id:") && data[1].contains("{description:") && data[2].contains("{rating:")
                    && data[3].contains("{lastSuggestion:")) {

                String idString = data[0].replace("{id:", "").replace("};", "");
                int id = Integer.parseInt(idString);

                String description = data[1].replace("{description:", "").replace("};", "");
                if (description.length() == 0) {
                    description = " ";
                }

                String ratingString = data[2].replace("{rating:", "").replace("};", "");
                int rating = Integer.parseInt(ratingString);

                String lastSuggestionString = data[3].replace("{lastSuggestion:", "").replace("};", "");
                boolean lastSuggestion = lastSuggestionString.contains("1");

                ListedMenu newValue = new ListedMenu(id, description, rating, lastSuggestion);
                _logger.Debug(String.format(Locale.getDefault(), "New ListedMenuDto %s", newValue));

                return newValue;
            } else {
                _logger.Error("Data has invalid entry!");
            }
        } else {
            _logger.Error(String.format(Locale.getDefault(), "Data has wrong length %d!", data.length));
        }

        _logger.Error("Data has an error!");
        return null;
    }
}