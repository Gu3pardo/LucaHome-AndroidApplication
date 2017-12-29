package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;

public class MoneyMeter implements Serializable {
    private static final long serialVersionUID = 8799048598384903746L;
    private static final String TAG = MoneyMeter.class.getSimpleName();

    private int _typeId;
    private String _bank;
    private String _plan;
    private String _user;
    private SerializableList<MoneyMeterData> _moneyMeterDataList;

    public MoneyMeter(
            int typeId,
            @NonNull String bank,
            @NonNull String plan,
            @NonNull String user,
            @NonNull SerializableList<MoneyMeterData> moneyMeterDataList) {
        _typeId = typeId;
        _bank = bank;
        _plan = plan;
        _user = user;
        _moneyMeterDataList = moneyMeterDataList;
    }

    public int GetTypeId() {
        return _typeId;
    }

    public void SetTypeId(int typeId) {
        _typeId = typeId;
    }

    public String GetBank() {
        return _bank;
    }

    public void SetBank(@NonNull String bank) {
        _bank = bank;
    }

    public String GetPlan() {
        return _plan;
    }

    public void SetPlan(@NonNull String plan) {
        _plan = plan;
    }

    public String GetUser() {
        return _user;
    }

    public void SetUser(@NonNull String user) {
        _user = user;
    }

    public SerializableList<MoneyMeterData> GetMoneyMeterDataList() {
        return _moneyMeterDataList;
    }

    public void SetMoneyMeterDataList(@NonNull SerializableList<MoneyMeterData> moneyMeterDataList) {
        _moneyMeterDataList = moneyMeterDataList;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (TypeId: %d );(Bank: %s );(Plan: %s );(User: %s ))", TAG, _typeId, _bank, _plan, _user);
    }
}
