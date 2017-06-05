package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.lucahome.common.enums.LucaServerAction;

public class MovieDto implements Serializable {

    private static final long serialVersionUID = -7601101130730680392L;

    private static final String TAG = MovieDto.class.getSimpleName();

    private static final int MAX_RATING = 5;

    private int _id;

    private String _title;
    private String _genre;
    private String _description;
    private int _rating;
    private int _watched;
    private String[] _sockets;

    public MovieDto(
            int id,
            @NonNull String title,
            @NonNull String genre,
            @NonNull String description,
            int rating,
            int watched,
            String[] sockets) {
        _id = id;
        _title = title;
        _genre = genre;
        _description = description;
        _rating = rating;
        _watched = watched;
        _sockets = sockets;
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

    public String GetRatingString() {
        return String.format(Locale.getDefault(), "%d/%d", _rating, MAX_RATING);
    }

    public int GetWatched() {
        return _watched;
    }

    public String[] GetSockets() {
        return _sockets;
    }

    public String GetSocketsString() {
        String socketsString = "";
        if (_sockets != null) {
            for (String socket : _sockets) {
                socketsString += socket + "|";
            }
            socketsString = socketsString.substring(0, socketsString.length() - 1);
        }
        return socketsString;
    }

    public String GetCommandAdd() {
        return LucaServerAction.ADD_MOVIE.toString() + _title
                + "&genre=" + _genre
                + "&description=" + _description
                + "&rating=" + String.valueOf(_rating)
                + "&watched=" + String.valueOf(_watched)
                + "&sockets=" + GetSocketsString();
    }

    public String GetCommandUpdate() {
        return LucaServerAction.UPDATE_MOVIE.toString() + _title
                + "&genre=" + _genre
                + "&description=" + _description
                + "&rating=" + String.valueOf(_rating)
                + "&watched=" + String.valueOf(_watched)
                + "&sockets=" + GetSocketsString();
    }

    public String GetCommandDelete() {
        return LucaServerAction.DELETE_MOVIE.toString() + _title;
    }

    public String toString() {
        return "{" + TAG
                + ": {Id: " + String.valueOf(_id)
                + "};{Title: " + _title
                + "};{Genre: " + _genre
                + "};{Description: " + _description
                + "};{Rating: " + String.valueOf(_rating)
                + "};{Watched: " + String.valueOf(_watched)
                + "};{Sockets: " + GetSocketsString() + "}}";
    }
}
