package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.SuggestedShoppingItem;
import guepardoapps.lucahome.common.enums.ShoppingItemType;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToSuggestedShoppingItemConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToSuggestedShoppingItemConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";


    private static final JsonDataToSuggestedShoppingItemConverter Singleton = new JsonDataToSuggestedShoppingItemConverter();

    public static JsonDataToSuggestedShoppingItemConverter getInstance() {
        return Singleton;
    }

    private JsonDataToSuggestedShoppingItemConverter() {
    }

    @Override
    public ArrayList<SuggestedShoppingItem> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<SuggestedShoppingItem> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private static ArrayList<SuggestedShoppingItem> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<SuggestedShoppingItem> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("SuggestedShoppingItem");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String name = child.getString("Name");
                    String typeString = child.getString("Type");
                    ShoppingItemType type = ShoppingItemType.OTHER;
                    try {
                        type = ShoppingItemType.GetByString(typeString);
                    } catch (Exception ex) {
                        Logger.getInstance().Error(Tag, ex.toString());
                    }

                    int quantity = child.getInt("Quantity");
                    String unit = child.getString("Unit");

                    SuggestedShoppingItem newShoppingEntry = new SuggestedShoppingItem(uuid, name, type, quantity, unit, true, ILucaClass.LucaServerDbAction.Null);
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