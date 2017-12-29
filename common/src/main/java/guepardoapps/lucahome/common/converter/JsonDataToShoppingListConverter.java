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
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToShoppingListConverter SINGLETON = new JsonDataToShoppingListConverter();

    public static JsonDataToShoppingListConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToShoppingListConverter() {
    }

    @Override
    public SerializableList<ShoppingEntry> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<ShoppingEntry> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static SerializableList<ShoppingEntry> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            SerializableList<ShoppingEntry> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("ShoppingEntry");

                    int id = child.getInt("Id");

                    String name = child.getString("Name");
                    String groupString = child.getString("Group");
                    ShoppingEntryGroup group = ShoppingEntryGroup.OTHER;
                    try {
                        group = ShoppingEntryGroup.GetByString(groupString);
                    } catch (Exception ex) {
                        Logger.getInstance().Error(TAG, ex.toString());
                    }

                    int quantity = child.getInt("Quantity");
                    String unit = child.getString("Unit");

                    ShoppingEntry newShoppingEntry = new ShoppingEntry(id, name, group, quantity, unit, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newShoppingEntry);
                }

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }
}