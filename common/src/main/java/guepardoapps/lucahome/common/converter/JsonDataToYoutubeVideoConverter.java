package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.YoutubeVideo;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public class JsonDataToYoutubeVideoConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToYoutubeVideoConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToYoutubeVideoConverter Singleton = new JsonDataToYoutubeVideoConverter();

    public static JsonDataToYoutubeVideoConverter getInstance() {
        return Singleton;
    }

    private JsonDataToYoutubeVideoConverter() {
    }

    @Override
    public ArrayList<YoutubeVideo> GetList(@NonNull String[] jsonStringArray) {
        if (StringHelper.StringsAreEqual(jsonStringArray)) {
            return parseStringToList(jsonStringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(jsonStringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<YoutubeVideo> GetList(@NonNull String responseString) {
        return parseStringToList(responseString);
    }

    private ArrayList<YoutubeVideo> parseStringToList(@NonNull String jsonString) {
        if (!jsonString.contains("Error")) {
            ArrayList<YoutubeVideo> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("YoutubeVideo");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String title = child.getString("Title");
                    String youtubeId = child.getString("YoutubeId");

                    int playCount = child.getInt("PlayCount");

                    YoutubeVideo newYoutubeVideo = new YoutubeVideo(uuid, title, youtubeId, playCount, "", "", true, ILucaClass.LucaServerDbAction.Null);
                    list.add(newYoutubeVideo);
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