package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.Movie;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToMovieConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToMovieConverter.class.getSimpleName();
    private static final String SEARCH_PARAMETER = "{\"Data\":";

    private static final JsonDataToMovieConverter SINGLETON = new JsonDataToMovieConverter();

    public static JsonDataToMovieConverter getInstance() {
        return SINGLETON;
    }

    private JsonDataToMovieConverter() {
    }

    @Override
    public SerializableList<Movie> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, SEARCH_PARAMETER);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Movie> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<Movie> parseStringToList(@NonNull String value) {
        if (!value.contains("Error")) {
            SerializableList<Movie> list = new SerializableList<>();

            try {
                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Movie");

                    //TODO Disabled due to error on server
                    // int id = child.getInt("Id");

                    String title = child.getString("Title");
                    String genre = child.getString("Genre");
                    String description = child.getString("Description");

                    int rating = child.getInt("Rating");
                    int watched = child.getInt("Watched");

                    Movie newMovie = new Movie(dataIndex, title, genre, description, rating, watched);
                    list.addValue(newMovie);
                }

            } catch (JSONException jsonException) {
                Logger.getInstance().Error(TAG, jsonException.getMessage());
            }
            return list;
        }

        Logger.getInstance().Error(TAG, value + " has an error!");
        return new SerializableList<>();
    }
}