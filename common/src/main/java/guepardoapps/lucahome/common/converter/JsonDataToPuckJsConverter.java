package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public class JsonDataToPuckJsConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToPuckJsConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToPuckJsConverter SINGLETON = new JsonDataToPuckJsConverter();

    public static JsonDataToPuckJsConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToPuckJsConverter() {
    }

    public SerializableList<PuckJs> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    public SerializableList<PuckJs> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<PuckJs> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            SerializableList<PuckJs> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("PuckJs");

                    int id = child.getInt("Id");

                    String name = child.getString("Name");
                    String area = child.getString("Area");
                    String mac = child.getString("Mac");

                    PuckJs newPuckJs = new PuckJs(id, name, area, mac, true, ILucaClass.LucaServerDbAction.Null);
                    list.addValue(newPuckJs);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(TAG, jsonString + " has an error!");
        return new SerializableList<>();
    }
}