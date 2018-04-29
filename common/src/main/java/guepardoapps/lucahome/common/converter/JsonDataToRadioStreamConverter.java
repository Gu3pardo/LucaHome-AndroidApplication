package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.RadioStream;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

@SuppressWarnings({"unused"})
public class JsonDataToRadioStreamConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToRadioStreamConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToRadioStreamConverter Singleton = new JsonDataToRadioStreamConverter();

    public static JsonDataToRadioStreamConverter getInstance() {
        return Singleton;
    }

    private JsonDataToRadioStreamConverter() {
    }

    @Override
    public ArrayList<RadioStream> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<RadioStream> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<RadioStream> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<RadioStream> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("RadioStream");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String title = child.getString("Title");
                    String url = child.getString("Url");

                    int playCount = child.getInt("PlayCount");

                    RadioStream newRadioStream = new RadioStream(uuid, title, url, playCount, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newRadioStream);
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