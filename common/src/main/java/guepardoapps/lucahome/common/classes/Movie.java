package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.LucaServerAction;

@SuppressWarnings({"unused"})
public class Movie implements Serializable {
    private static final long serialVersionUID = -7601101130730680392L;
    private static final String TAG = Movie.class.getSimpleName();
    private static final int MAX_RATING = 5;

    private int _id;

    private String _title;
    private String _genre;
    private String _description;
    private int _rating;
    private int _watched;

    public Movie(
            int id,
            @NonNull String title,
            @NonNull String genre,
            @NonNull String description,
            int rating,
            int watched) {
        _id = id;
        _title = title;
        _genre = genre;
        _description = description;
        _rating = rating;
        _watched = watched;
    }

    public int GetId() {
        return _id;
    }

    public String GetTitle() {
        return _title;
    }

    public void SetTitle(@NonNull String title) {
        _title = title;
    }

    public String GetGenre() {
        return _genre;
    }

    public void SetGenre(@NonNull String genre) {
        _genre = genre;
    }

    public String GetDescription() {
        return _description;
    }

    public void SetDescription(@NonNull String description) {
        _description = description;
    }

    public int GetRating() {
        return _rating;
    }

    public void SetRating(int rating) {
        _rating = rating;
    }

    public int GetWatched() {
        return _watched;
    }

    public String GetRatingString() {
        return String.format(Locale.getDefault(), "%d/%d", _rating, MAX_RATING);
    }

    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&genre=%s&description=%s&rating=%d&watched=%d", LucaServerAction.UPDATE_MOVIE.toString(), _title, _genre, _description, _rating, _watched);
    }

    @Override
    public String toString() {
        return "{" + TAG
                + ": {Id: " + String.valueOf(_id)
                + "};{Title: " + _title
                + "};{Genre: " + _genre
                + "};{Description: " + _description
                + "};{Rating: " + String.valueOf(_rating)
                + "};{Watched: " + String.valueOf(_watched) + "}}";
    }
}
