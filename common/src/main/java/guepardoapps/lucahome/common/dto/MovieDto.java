package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class MovieDto implements Serializable {
    private static final long serialVersionUID = -7601102839110680392L;
    private static final String TAG = MovieDto.class.getSimpleName();

    private int _id;

    private String _title;
    private String _genre;
    private String _description;
    private int _rating;
    private int _watched;

    public MovieDto(
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

    public String GetGenre() {
        return _genre;
    }

    public String GetDescription() {
        return _description;
    }

    public int GetRating() {
        return _rating;
    }

    public int GetWatched() {
        return _watched;
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
