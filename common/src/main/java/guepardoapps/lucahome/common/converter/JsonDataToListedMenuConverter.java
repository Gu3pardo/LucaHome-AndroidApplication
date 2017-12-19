package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.ListedMenu;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToListedMenuConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToListedMenuConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToListedMenuConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<ListedMenu> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            _logger.Debug("StringsAreEqual");
            return parseStringToList(stringArray[0]);
        } else {
            _logger.Debug("SelectString");
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            _logger.Debug("usedEntry: " + usedEntry);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<ListedMenu> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<ListedMenu> parseStringToList(@NonNull String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<ListedMenu> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("ListedMenu");

                    int id = child.getInt("ID");

                    String title = child.getString("Title");
                    String description = child.getString("Description");

                    int rating = child.getInt("Rating");
                    int useCounter = child.getInt("UseCounter");

                    ListedMenu newListedMenu = new ListedMenu(id, title, description, rating, useCounter, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newListedMenu);
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