package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.RssFeed;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

@SuppressWarnings({"unused"})
public class JsonDataToRssFeedConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToRssFeedConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToRssFeedConverter Singleton = new JsonDataToRssFeedConverter();

    public static JsonDataToRssFeedConverter getInstance() {
        return Singleton;
    }

    private JsonDataToRssFeedConverter() {
    }

    @Override
    public ArrayList<RssFeed> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<RssFeed> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<RssFeed> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<RssFeed> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("RssFeed");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String title = child.getString("Title");
                    String url = child.getString("Url");

                    int playCount = child.getInt("PlayCount");

                    RssFeed newRssFeed = new RssFeed(uuid, title, url, playCount, true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newRssFeed);
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