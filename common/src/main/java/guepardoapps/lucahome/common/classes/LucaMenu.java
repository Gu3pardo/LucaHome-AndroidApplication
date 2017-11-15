package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.enums.Weekday;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class LucaMenu implements Serializable, ILucaClass {
    private static final long serialVersionUID = 3749147384275047381L;
    private static final String TAG = LucaMenu.class.getSimpleName();

    private int _id;

    private String _title;
    private String _description;
    private Weekday _weekday;
    private SerializableDate _date;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public LucaMenu(
            int id,
            @NonNull String title,
            @NonNull String description,
            @NonNull Weekday weekday,
            @NonNull SerializableDate date,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;

        _title = title;
        _description = description;
        _weekday = weekday;
        _date = date;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
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

    public Weekday GetWeekday() {
        return _weekday;
    }

    public void SetWeekday(@NonNull Weekday weekday) {
        _weekday = weekday;
    }

    public SerializableDate GetDate() {
        return _date;
    }

    public void SetDate(@NonNull SerializableDate date) {
        _date = date;
    }

    public String GetDateString() {
        return String.format(Locale.getDefault(), "%s, %s", _weekday, _date.DDMMYYYY());
    }

    public String GetSingleLineString() {
        return String.format(Locale.getDefault(), "%s: %s, %s", GetDateString(), _title, _description);
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
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&day=%d&month=%d&year=%d&title=%s&description=%s", LucaServerAction.UPDATE_MENU.toString(), _weekday.GetEnglishDay(), _date.DayOfMonth(), _date.Month(), _date.Year(), _title, _description);
    }

    @Override
    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerAction.CLEAR_MENU.toString(), _weekday.GetEnglishDay());
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(Title: %s );(Description: %s );(Weekday: %s );(Date: %s ))", TAG, _id, _title, _description, _weekday, _date);
    }
}
