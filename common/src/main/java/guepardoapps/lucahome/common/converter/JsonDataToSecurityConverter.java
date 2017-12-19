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
    private Logger _logger;

    private static String _searchParameter = "{\"MotionData\":";

    public JsonDataToSecurityConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<Security> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Security> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private SerializableList<Security> parseStringToList(@NonNull String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<Security> list = new SerializableList<>();

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

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}
