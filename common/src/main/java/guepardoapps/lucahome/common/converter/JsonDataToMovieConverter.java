package guepardoapps.lucahome.common.converter;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.StringHelper;
import guepardoapps.lucahome.common.classes.Movie;
import guepardoapps.lucahome.common.interfaces.converter.IJsonDataConverter;

public final class JsonDataToMovieConverter implements IJsonDataConverter {
    private static final String TAG = JsonDataToMovieConverter.class.getSimpleName();
    private Logger _logger;

    private static String _searchParameter = "movie::";

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
        if (StringHelper.GetStringCount(value, _searchParameter) > 0) {
            if (value.contains(_searchParameter)) {
                SerializableList<Movie> list = new SerializableList<>();

                String[] entries = value.split(_searchParameter);
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    entry = entry.replace(_searchParameter, "").replace(";", "");

                    String[] data = entry.split("::");
                    Movie newValue = parseStringToValue(index, data);
                    if (newValue != null) {
                        list.addValue(newValue);
                    }
                }
                return list;
            }
        }

        _logger.Error(value + " has an error!");
        return new SerializableList<>();
    }

    private Movie parseStringToValue(int id, @NonNull String[] data) {
        if (data.length == 5) {
            String title = data[0].replace("::", "");
            String genre = data[1].replace("::", "");
            String description = data[2].replace("::", "");

            String ratingString = data[3].replace("::", "");
            int rating = Integer.parseInt(ratingString);

            //String watchedString = data[4].replace("::", "");
            //int watched = Integer.parseInt(watchedString);

            Movie newValue = new Movie(id, title, genre, description, rating);
            _logger.Debug(String.format(Locale.getDefault(), "New MovieDto %s", newValue));

            return newValue;
        }

        _logger.Error("Data has an error!");
        return null;
    }
}