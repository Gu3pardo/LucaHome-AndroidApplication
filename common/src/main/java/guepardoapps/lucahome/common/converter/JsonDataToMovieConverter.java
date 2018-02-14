package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.Movie;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.StringHelper;

public final class JsonDataToMovieConverter implements IJsonDataConverter {
    private static final String Tag = JsonDataToMovieConverter.class.getSimpleName();
    private static final String SearchParameter = "{\"Data\":";

    private static final JsonDataToMovieConverter Singleton = new JsonDataToMovieConverter();

    public static JsonDataToMovieConverter getInstance() {
        return Singleton;
    }

    private JsonDataToMovieConverter() {
    }

    @Override
    public ArrayList<Movie> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SearchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public ArrayList<Movie> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private ArrayList<Movie> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            ArrayList<Movie> list = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int index = 0; index < dataArray.length(); index++) {
                    JSONObject child = dataArray.getJSONObject(index).getJSONObject("Movie");

                    UUID uuid = UUID.fromString(child.getString("Uuid"));

                    String title = child.getString("Title");
                    String genre = child.getString("Genre");
                    String description = child.getString("Description");

                    int rating = child.getInt("Rating");
                    int watched = child.getInt("Watched");

                    Movie newMovie = new Movie(uuid, title, genre, description, rating, watched);
                    list.add(newMovie);
                }

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(Tag, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(Tag, value + " has an error!");
        return new ArrayList<>();
    }
}