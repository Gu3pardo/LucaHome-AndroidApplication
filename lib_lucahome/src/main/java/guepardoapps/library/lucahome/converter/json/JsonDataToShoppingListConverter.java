package guepardoapps.library.lucahome.converter.json;

import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToShoppingListConverter {

    private static final String TAG = JsonDataToShoppingListConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{shopping_entry:";

    public static SerializableList<ShoppingEntryDto> GetList(String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return ParseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return ParseStringToList(usedEntry);
        }
    }

    public static ShoppingEntryDto Get(String value) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
            if (value.contains(_searchParameter)) {
                value = value.replace(_searchParameter, "").replace("};};", "");

                String[] data = value.split("\\};");
                ShoppingEntryDto newValue = ParseStringToValue(data);
                if (newValue != null) {
                    return newValue;
                } else {
                    _logger.Error("newValue is null!");
                }
            } else {
                _logger.Error("Value does not contain " + _searchParameter);
            }
        } else {
            _logger.Error(String.format("String %s contains %s times searchParameter %s", value,
                    StringHelper.GetStringCount(value, _searchParameter), _searchParameter));
        }

        _logger.Error(value + " has an error!");

        return null;
    }

    private static SerializableList<ShoppingEntryDto> ParseStringToList(String value) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (StringHelper.GetStringCount(value, _searchParameter) >= 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<ShoppingEntryDto> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (String entry : entries) {
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    ShoppingEntryDto newValue = ParseStringToValue(data);
                    if (newValue != null) {
                        list.addValue(newValue);
                    } else {
                        _logger.Error("newValue is null!");
                    }
                }
                return list;
            } else {
                _logger.Error("Value does not contain " + _searchParameter);
            }
        } else {
            _logger.Error(String.format("String %s contains %s times searchParameter %s", value,
                    StringHelper.GetStringCount(value, _searchParameter), _searchParameter));
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private static ShoppingEntryDto ParseStringToValue(String[] data) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (data.length == 4) {
            if (data[0].contains("{id:") && data[1].contains("{name:") && data[2].contains("{group:")
                    && data[3].contains("{quantity:")) {

                String idString = data[0].replace("{id:", "").replace("};", "");
                int id = -1;
                try {
                    id = Integer.parseInt(idString);
                } catch (Exception ex) {
                    _logger.Error(ex.toString());
                }

                String name = data[1].replace("{name:", "").replace("};", "");

                String groupString = data[2].replace("{group:", "").replace("};", "");
                ShoppingEntryGroup group = ShoppingEntryGroup.OTHER;
                try {
                    group = ShoppingEntryGroup.GetByString(groupString);
                } catch (Exception ex) {
                    _logger.Error(ex.toString());
                }

                String quantityString = data[3].replace("{quantity:", "").replace("};", "");
                int quantity = -1;
                try {
                    quantity = Integer.parseInt(quantityString);
                } catch (Exception ex) {
                    _logger.Error(ex.toString());
                }

                ShoppingEntryDto newValue = new ShoppingEntryDto(id, name, group, quantity, false);
                _logger.Debug(String.format(Locale.getDefault(), "New ShoppingEntryDto %s", newValue));

                return newValue;
            } else {
                _logger.Error("data contains invalid entries!");
            }
        } else {
            _logger.Error(String.format("Data has invalid length %s", data.length));
        }

        _logger.Error("Data has an error!");

        return null;
    }
}