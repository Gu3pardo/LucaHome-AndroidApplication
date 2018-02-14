package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.SuggestedMeal;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToSuggestedMealConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToSuggestedMealConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToSuggestedMealConverter Singleton = new JsonDataToSuggestedMealConverter();

    public static JsonDataToSuggestedMealConverter getInstance() {
        return Singleton;
    }

    private JsonDataToSuggestedMealConverter() {
    }

    @Override
    public ArrayList<SuggestedMeal> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<SuggestedMeal> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<SuggestedMeal> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<SuggestedMeal> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("SuggestedMeal");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String title = child.getString("Title");
                    String description = child.getString("Description");

                    int rating = child.getInt("Rating");
                    int useCounter = child.getInt("UseCounter");

                    ArrayList<UUID> shoppingItemUuidList = new ArrayList<>();

                    JSONArray shoppingItemUuidArray = child.getJSONArray("ShoppingItemUuidList");
                    for (int uuidArrayIndex = 0; uuidArrayIndex < shoppingItemUuidArray.length(); uuidArrayIndex++) {
                        JSONObject shoppingItemUuidArrayChild = shoppingItemUuidArray.getJSONObject(uuidArrayIndex).getJSONObject("ShoppingItemUuid");
                        String shoppingItemUuidString = shoppingItemUuidArrayChild.getString("Uuid");
                        UUID shoppingItemUuid = UUID.fromString(shoppingItemUuidString);
                        shoppingItemUuidList.add(shoppingItemUuid);
                    }

                    SuggestedMeal newSuggestedMeal = new SuggestedMeal(uuid, title, description, rating, useCounter, shoppingItemUuidList, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newSuggestedMeal);
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