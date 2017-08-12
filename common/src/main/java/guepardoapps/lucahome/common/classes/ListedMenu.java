package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.sql.Date;
import java.util.Locale;

public class    ListedMenu implements Serializable {
    private static final long serialVersionUID = 3749104839275047381L;
    private static final String TAG = ListedMenu.class.getSimpleName();

    private int _id;
    private String _description;
    private int _rating;
    private boolean _lastSuggestion;

    public ListedMenu(
            int id,
            @NonNull String description,
            int rating,
            boolean lastSuggestion) {
        _id = id;
        _description = description;
        _rating = rating;
        _lastSuggestion = lastSuggestion;
    }

    public int GetId() {
        return _id;
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

    public boolean GetLastSuggestion() {
        return _lastSuggestion;
    }

    public void SetLastSuggestion(boolean lastSuggestion) {
        _lastSuggestion = lastSuggestion;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Description: %s );(Rating: %d );(LastSuggestion: %s ))", TAG, _id, _description, _rating, _lastSuggestion);
    }
}
