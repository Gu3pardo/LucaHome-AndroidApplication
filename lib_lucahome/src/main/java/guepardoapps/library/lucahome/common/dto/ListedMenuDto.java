package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class ListedMenuDto implements Serializable {

    private static final long serialVersionUID = -6478432655737694103L;

    private static final String TAG = ListedMenuDto.class.getSimpleName();

    private int _id;
    private String _description;
    private int _rating;
    private boolean _lastSuggestion;

    public ListedMenuDto(
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

    public int GetRating() {
        return _rating;
    }

    public boolean IsLastSuggestion() {
        return _lastSuggestion;
    }

    @Override
    public String toString() {
        return "{" + TAG + ":{Id:" + String.valueOf(_id)
                + "};{Description:" + _description
                + "};{Rating:" + String.valueOf(_rating)
                + "};{LastSuggestion:" + String.valueOf(_lastSuggestion) + "};}";
    }
}
