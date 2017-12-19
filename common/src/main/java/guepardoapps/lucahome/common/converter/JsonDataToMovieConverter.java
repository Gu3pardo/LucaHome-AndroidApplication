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
    private Logger _logger;

    private static String _searchParameter = "{\"Data\":";

    public JsonDataToMovieConverter() {
        _logger = new Logger(TAG);
    }

    @Override
    public SerializableList<Movie> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return parseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return parseStringToList(usedEntry);
        }
    }

    @Override
    public SerializableList<Movie> GetList(@NonNull String jsonString) {
        return parseStringToList(jsonString);
    }

    private SerializableList<Movie> parseStringToList(@NonNull String value) {
        try {
            if (!value.contains("Error")) {
                SerializableList<Movie> list = new SerializableList<>();

                JSONObject jsonObject = new JSONObject(value);
                JSONArray dataArray = jsonObject.getJSONArray("Data");

                for (int dataIndex = 0; dataIndex < dataArray.length(); dataIndex++) {
                    JSONObject child = dataArray.getJSONObject(dataIndex).getJSONObject("Movie");

                    String title = child.getString("Title");
                    String genre = child.getString("Genre");
                    String description = child.getString("Description");

                    int rating = child.getInt("Rating");
                    int watched = child.getInt("Watched");

                    Movie newMovie = new Movie(dataIndex, title, genre, description, rating, watched);
                    list.addValue(newMovie);
                }

                return list;
            }
        } catch (JSONException jsonException) {
            _logger.Error(jsonException.getMessage());
        }

        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }
}