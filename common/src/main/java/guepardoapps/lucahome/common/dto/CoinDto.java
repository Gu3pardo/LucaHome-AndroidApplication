package guepardoapps.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class CoinDto implements Serializable {
    private static final long serialVersionUID = 8796770534436483719L;
    private static final String TAG = CoinDto.class.getSimpleName();

    public enum Action {Add, Update}

    private int _id;
    private String _user;
    private String _type;
    private double _amount;
    private Action _action;

    public CoinDto(
            int id,
            String user,
            String type,
            double amount,
            @NonNull Action action) {
        _id = id;
        _user = user;
        _type = type;
        _amount = amount;
        _action = action;
    }

    public int GetId() {
        return _id;
    }

    public String GetUser() {
        return _user;
    }

    public String GetType() {
        return _type;
    }

    public double GetAmount() {
        return _amount;
    }

    public Action GetAction() {
        return _action;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (User: %s );(Type: %s );(Amount: %s );(Action: %s ))", TAG, _user, _type, _amount, _action);
    }
}
