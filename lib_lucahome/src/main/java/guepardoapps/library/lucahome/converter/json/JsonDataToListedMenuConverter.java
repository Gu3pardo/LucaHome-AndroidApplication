package guepardoapps.library.lucahome.converter.json;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.ListedMenuDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToListedMenuConverter {

    private static final String TAG = JsonDataToListedMenuConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{listedmenu:";

    public static SerializableList<ListedMenuDto> GetList(@NonNull String[] stringArray) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (StringHelper.StringsAreEqual(stringArray)) {
            _logger.Debug("StringsAreEqual");
            return ParseStringToList(stringArray[0]);
        } else {
            _logger.Debug("SelectString");
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            _logger.Debug("usedEntry: " + usedEntry);
            return ParseStringToList(usedEntry);
        }
    }

    public static ListedMenuDto Get(@NonNull String value) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
            if (value.contains(_searchParameter)) {
                value = value.replace(_searchParameter, "").replace("};};", "");

                String[] data = value.split("\\};");
                ListedMenuDto newValue = ParseStringToValue(data);
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

    private static SerializableList<ListedMenuDto> ParseStringToList(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<ListedMenuDto> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (String entry : entries) {
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    ListedMenuDto newValue = ParseStringToValue(data);
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

    private static ListedMenuDto ParseStringToValue(@NonNull String[] data) {
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

                ListedMenuDto newValue = new ListedMenuDto(id, description, rating, lastSuggestion);
                _logger.Debug(String.format(Locale.getDefault(), "New ListedMenuDto %s", newValue));

                return newValue;
            } else {
                _logger.Error(String.format(Locale.getDefault(), "Data %s has invalid entry!", data));
            }
        } else {
            _logger.Error(String.format(Locale.getDefault(), "Data %s has wrong length %d!", data, data.length));
        }

        _logger.Error("Data has an error!");
        return null;
    }
}