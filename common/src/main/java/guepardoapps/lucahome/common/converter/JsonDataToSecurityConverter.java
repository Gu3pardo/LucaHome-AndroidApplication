package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.Security;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public class JsonDataToSecurityConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToSecurityConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"MotionData\":";

    private static final JsonDataToSecurityConverter SINGLETON = new JsonDataToSecurityConverter();

    public static JsonDataToSecurityConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToSecurityConverter() {
    }

    @Override
    public SerializableList<Security> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Security> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<Security> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            SerializableList<Security> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONObject jsonObjectData = jsonObject.getJSONObject("MotionData");

                boolean state = jsonObjectData.getString("State").contains("ON");
                boolean control = jsonObjectData.getString("Control").contains("ON");

                String url = jsonObjectData.getString("URL");

                JSONArray jsonArrayEvents = jsonObjectData.getJSONArray("Events");
                SerializableList<String> registeredEvents = new SerializableList<>();

                for (int dataIndex = 0; dataIndex < jsonArrayEvents.length(); dataIndex++) {
                    String name = jsonArrayEvents.getJSONObject(dataIndex).getString("FileName");
                    registeredEvents.addValue(name);
                }

                Security security = new Security(state, control, url, registeredEvents);
                list.addValue(security);

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }
}
