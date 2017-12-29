package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToMenuConverter SINGLETON = new JsonDataToMenuConverter();

    public static JsonDataToMenuConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToMenuConverter() {
    }

    @Override
    public SerializableList<LucaMenu> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<LucaMenu> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<LucaMenu> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            SerializableList<LucaMenu> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Menu");

                    String title = child.getString("Title");
                    String description = child.getString("Description");

                    String weekdayString = child.getString("Weekday");
                    Weekday weekday = Weekday.GetByEnglishString(weekdayString);

                    JSONObject jsonDate = child.getJSONObject("Date");

                    int day = jsonDate.getInt("Day");
                    int month = jsonDate.getInt("Month");
                    int year = jsonDate.getInt("Year");

                    LucaMenu newMenu = new LucaMenu(dataIndex, title, description, weekday, new SerializableDate(year, month, day), true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newMenu);
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