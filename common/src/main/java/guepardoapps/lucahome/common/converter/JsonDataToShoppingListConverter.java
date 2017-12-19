package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToShoppingListConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToShoppingListConverter.class.getSimpleName();
    private static Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToShoppingListConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<ShoppingEntry> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<ShoppingEntry> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static SerializableList<ShoppingEntry> parseStringToList(@NonNull String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<ShoppingEntry> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("ShoppingEntry");

                    int id = child.getInt("ID");

                    String name = child.getString("Name");
                    String groupString = child.getString("Group");
                    ShoppingEntryGroup group = ShoppingEntryGroup.OTHER;
                    try {
                        group = ShoppingEntryGroup.GetByString(groupString);
                    } catch (Exception ex) {
                        _logger.Error(ex.toString());
                    }

                    int quantity = child.getInt("Quantity");

                    ShoppingEntry newShoppingEntry = new ShoppingEntry(id, name, group, quantity, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newShoppingEntry);
                }

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}