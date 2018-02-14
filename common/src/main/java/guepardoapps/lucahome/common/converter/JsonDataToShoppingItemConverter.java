package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.ShoppingItem;
import guepardoapps.lucahome.common.enums.ShoppingItemType;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToShoppingItemConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToShoppingItemConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToShoppingItemConverter Singleton = new JsonDataToShoppingItemConverter();

    public static JsonDataToShoppingItemConverter getInstance() {
        return Singleton;
    }

    private JsonDataToShoppingItemConverter() {
    }

    @Override
    public ArrayList<ShoppingItem> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<ShoppingItem> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static ArrayList<ShoppingItem> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<ShoppingItem> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("ShoppingItem");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String name = child.getString("Name");
                    String groupString = child.getString("Group");
                    ShoppingItemType type = ShoppingItemType.OTHER;
                    try {
                        type = ShoppingItemType.GetByString(groupString);
                    } catch (Exception exception) {
                        Logger.getInstance().Error(Tag, exception.toString());
                    }

                    int quantity = child.getInt("Quantity");
                    String unit = child.getString("Unit");

                    ShoppingItem newShoppingEntry = new ShoppingItem(uuid, name, type, quantity, unit, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newShoppingEntry);
                }

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(Tag, value + " has an error!");
        return new ArrayList<>();
    }
}