package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.Meal;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToMealConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToMealConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToMealConverter Singleton = new JsonDataToMealConverter();

    public static JsonDataToMealConverter getInstance() {
        return Singleton;
    }

    private JsonDataToMealConverter() {
    }

    @Override
    public ArrayList<Meal> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<Meal> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<Meal> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<Meal> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("Meal");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String title = child.getString("Title");
                    String description = child.getString("Description");

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    ArrayList<UUID> shoppingItemUuidList = new ArrayList<>();

                    JSONArray shoppingItemUuidArray = child.getJSONArray("ShoppingItemUuidList");
                    for (int uuidArrayIndex = 0; uuidArrayIndex < shoppingItemUuidArray.length(); uuidArrayIndex++) {
                        JSONObject shoppingItemUuidArrayChild = shoppingItemUuidArray.getJSONObject(uuidArrayIndex).getJSONObject("ShoppingItemUuid");
                        String shoppingItemUuidString = shoppingItemUuidArrayChild.getString("Uuid");
                        UUID shoppingItemUuid = UUID.fromString(shoppingItemUuidString);
                        shoppingItemUuidList.add(shoppingItemUuid);
                    }

                    Calendar date = Calendar.getInstance();
                    date.set(year, month, day);

                    Meal newMenu = new Meal(uuid, title, description, date, shoppingItemUuidList, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newMenu);
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