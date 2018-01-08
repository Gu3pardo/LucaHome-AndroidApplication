package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

@SuppressWarnings({"unused"})
public class Coin implements Serializable, ILucaClass {
    private static final long serialVersionUID = 1027483947363954860L;
    private static final String TAG = Coin.class.getSimpleName();

    public enum Trend {NULL, Fall, Rise}

    private int _id;
    private String _user;
    private String _type;
    private double _amount;

    private double _currentConversion;
    private Trend _currentTrend;

    private int _icon;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public Coin(
            int id,
            @NonNull String user,
            @NonNull String type,
            double amount,
            double currentConversion,
            @NonNull Trend currentTrend,
            int icon,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _id = id;
        _user = user;
        _type = type;
        _amount = amount;

        _currentConversion = currentConversion;
        _currentTrend = currentTrend;

        _icon = icon;

        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public int GetId() {
        return _id;
    }

    public String GetUser() {
        return _user;
    }

    public void SetUser(@NonNull String user) {
        _user = user;
    }

    public String GetType() {
        return _type;
    }

    public void SetType(@NonNull String type) {
        _type = type;
    }

    public double GetAmount() {
        return _amount;
    }

    public void SetAmount(double amount) {
        _amount = amount;
    }

    public double GetCurrentConversion() {
        return _currentConversion;
    }

    public void SetCurrentConversion(double currentConversion) {
        _currentConversion = currentConversion;
    }

    public String GetCurrentConversionString() {
        return String.format(Locale.getDefault(), "%.2f €", _currentConversion);
    }

    public Trend GetCurrentTrend() {
        return _currentTrend;
    }

    public void SetCurrentTrend(@NonNull Trend currentTrend) {
        _currentTrend = currentTrend;
    }

    public double GetValue() {
        return _amount * _currentConversion;
    }

    public String GetValueString() {
        return String.format(Locale.getDefault(), "%.2f €", GetValue());
    }

    public int GetIcon() {
        return _icon;
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
        return String.format(Locale.getDefault(), "%s%d&username=%s&type=%s&amount=%.6f", LucaServerAction.ADD_COIN.toString(), _id, _user, _type, _amount);
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%d&username=%s&type=%s&amount=%.6f", LucaServerAction.UPDATE_COIN.toString(), _id, _user, _type, _amount);
    }

    @Override
    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%d", LucaServerAction.DELETE_COIN.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s: {Id: %d};{User: %s};{Type: %s};{Amount: %.6f};{CurrentConversion: %.6f};{Trend: %s}}", TAG, _id, _user, _type, _amount, _currentConversion, _currentTrend);
    }
}
