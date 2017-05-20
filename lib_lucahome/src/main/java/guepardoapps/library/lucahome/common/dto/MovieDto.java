package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.ServerActions;

public class MovieDto implements Serializable {

    private static final long serialVersionUID = -7601101130730680392L;

    @SuppressWarnings("unused")
    private static final String TAG = MovieDto.class.getSimpleName();

    private static final int MAX_RATING = 5;

    private String _title;
    private String _genre;
    private String _description;
    private int _rating;
    private int _watched;
    private String[] _sockets;

    private String _deleteBroadcastReceiverString;
    private String _updateBroadcastReceiverString;

    public MovieDto(String title, String genre, String description, int rating, int watched, String[] sockets) {
        _title = title;
        _genre = genre;
        _description = description;
        _rating = rating;
        _watched = watched;
        _sockets = sockets;

        _deleteBroadcastReceiverString = Broadcasts.DELETE_MOVIE + _title.toUpperCase(Locale.GERMAN).replace(" ", "_");
        _updateBroadcastReceiverString = Broadcasts.UPDATE_MOVIE + _title.toUpperCase(Locale.GERMAN).replace(" ", "_");
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
        return String.format(Locale.GERMAN, "%d/%d", _rating, MAX_RATING);
    }

    public void SetRating(int rating) {
        _rating = rating;
    }

    public int GetWatched() {
        return _watched;
    }

    public String[] GetSockets() {
        return _sockets;
    }

    public String GetDeleteBroadcast() {
        return _deleteBroadcastReceiverString;
    }

    public String GetUpdateBroadcast() {
        return _updateBroadcastReceiverString;
    }

    public String GetCommandAdd() {
        return ServerActions.ADD_MOVIE + _title + "&genre=" + _genre + "&description=" + _description + "&rating="
                + String.valueOf(_rating) + "&watched=" + String.valueOf(_watched) + "&sockets=" + GetSocketsString();
    }

    public String GetCommandUpdate() {
        return ServerActions.UPDATE_MOVIE + _title + "&genre=" + _genre + "&description=" + _description + "&rating="
                + String.valueOf(_rating) + "&watched=" + String.valueOf(_watched) + "&sockets=" + GetSocketsString();
    }

    public String GetCommandDelete() {
        return ServerActions.DELETE_MOVIE + _title;
    }

    public String GetCommandStart() {
        return ServerActions.START_MOVIE + _title;
    }

    public String toString() {
        return "{Movie: {Title: " + _title + "};{Genre: " + _genre + "};{Description: " + _description + "};{Rating: "
                + String.valueOf(_rating) + "};{Watched: " + String.valueOf(_watched) + "};{Sockets: "
                + GetSocketsString() + "};{DeleteBroadcastReceiverString: " + _deleteBroadcastReceiverString
                + "};{UpdateBroadcastReceiverString: " + _updateBroadcastReceiverString + "}}";
    }

    private String GetSocketsString() {
        String socketsString = "";
        if (_sockets != null) {
            for (String socket : _sockets) {
                socketsString += socket + "|";
            }
            socketsString = socketsString.substring(0, socketsString.length() - 1);
        }
        return socketsString;
    }
}
