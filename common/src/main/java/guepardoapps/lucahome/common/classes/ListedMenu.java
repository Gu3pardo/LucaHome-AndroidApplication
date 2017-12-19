package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class ListedMenu implements Serializable, ILucaClass {
    private static final long serialVersionUID = 3749104839275047381L;
    private static final String TAG = ListedMenu.class.getSimpleName();

    private int _id;
    private String _title;
    private String _description;
    private int _rating;
    private int _useCounter;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public ListedMenu(
            int id,
            @NonNull String title,
            @NonNull String description,
            int rating,
            int useCounter,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;
        _title = title;
        _description = description;
        _rating = rating;
        _useCounter = useCounter;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
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

    public int GetUseCounter() {
        return _useCounter;
    }

    public void SetUseCounter(int useCounter) {
        _useCounter = useCounter;
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
    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%d&title=%s&description=%s&rating=%d", LucaServerAction.ADD_LISTEDMENU.toString(), _id, _title, _description, _rating);
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%d&title=%s&description=%s&rating=%d", LucaServerAction.UPDATE_LISTEDMENU.toString(), _id, _title, _description, _rating);
    }

    @Override
    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%d", LucaServerAction.DELETE_LISTEDMENU.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Title: %s );(Description: %s );(Rating: %d );(LastSuggestion: %s ))", TAG, _id, _title, _description, _rating, _useCounter);
    }
}
