package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public class JsonDataToPuckJsConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToPuckJsConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToPuckJsConverter Singleton = new JsonDataToPuckJsConverter();

    public static JsonDataToPuckJsConverter getInstance() {
        return Singleton;
    }

    private JsonDataToPuckJsConverter() {
    }

    public ArrayList<PuckJs> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    public ArrayList<PuckJs> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<PuckJs> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<PuckJs> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("PuckJs");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));
                    UUID roomUuid = UUID.fromString(child.getString("RoomUuid"));

                    String name = child.getString("Name");
                    String mac = child.getString("Mac");

                    PuckJs newPuckJs = new PuckJs(uuid, roomUuid, name, mac, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newPuckJs);
                }
            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }

            return list;
        }

        Logger.getInstance().Error(Tag, jsonString + " has an error!");
        return new ArrayList<>();
    }
}