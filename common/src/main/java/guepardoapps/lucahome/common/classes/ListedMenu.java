package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class ListedMenu implements Serializable, ILucaClass {
    private static final long serialVersionUID = 3749104839275047381L;
    private static final String TAG = ListedMenu.class.getSimpleName();

    private int _id;
    private String _description;
    private int _rating;
    private boolean _lastSuggestion;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public ListedMenu(
            int id,
            @NonNull String description,
            int rating,
            boolean lastSuggestion,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;

        _description = description;
        _rating = rating;
        _lastSuggestion = lastSuggestion;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
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
    public void SetIsOnServer(boolean isOnServer) {
        _isOnServer = isOnServer;
    }

    @Override
    public boolean GetIsOnServer() {
        return _isOnServer;
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    @Override
    public LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    @Override
    public String CommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandAdd for ListedMenu");
    }

    @Override
    public String CommandUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandUpdate for ListedMenu");
    }

    @Override
    public String CommandDelete() throws NoSuchMethodException {
        throw new NoSuchMethodException("No method CommandDelete for ListedMenu");
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Description: %s );(Rating: %d );(LastSuggestion: %s ))", TAG, _id, _description, _rating, _lastSuggestion);
    }
}
