package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

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
    private Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToMenuConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<LucaMenu> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<LucaMenu> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<LucaMenu> parseStringToList(@NonNull String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<LucaMenu> list = new SerializableList<>();

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

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}