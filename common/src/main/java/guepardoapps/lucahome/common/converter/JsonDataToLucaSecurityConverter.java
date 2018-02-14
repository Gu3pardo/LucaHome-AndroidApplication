package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.LucaSecurity;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public class JsonDataToLucaSecurityConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToLucaSecurityConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"MotionData\":";

    private static final JsonDataToLucaSecurityConverter Singleton = new JsonDataToLucaSecurityConverter();

    public static JsonDataToLucaSecurityConverter getInstance() {
        return Singleton;
    }

    private JsonDataToLucaSecurityConverter() {
    }

    @Override
    public ArrayList<LucaSecurity> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<LucaSecurity> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<LucaSecurity> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<LucaSecurity> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONObject jsonObjectData = jsonObject.getJSONObject("MotionData");

                UUID roomUuid = UUID.fromString(jsonObjectData.getString("RoomUuid"));

                boolean state = jsonObjectData.getString("State").contains("ON");
                boolean control = jsonObjectData.getString("Control").contains("ON");

                String url = jsonObjectData.getString("URL");

                JSONArray jsonArrayEvents = jsonObjectData.getJSONArray("Events");
                ArrayList<String> registeredEvents = new ArrayList<>();

                for (int index = 0; index < jsonArrayEvents.length(); index++) {
                    String name = jsonArrayEvents.getJSONObject(index).getString("FileName");
                    registeredEvents.add(name);
                }

                LucaSecurity security = new LucaSecurity(roomUuid, state, control, url, LucaSecurity.CameraType.Raspberry, registeredEvents);
                list.add(security);

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(Tag, value + " has an error!");
        return new ArrayList<>();
    }
}
