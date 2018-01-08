package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;

@SuppressWarnings({"unused"})
public class MoneyDate implements Serializable {
    private static final long serialVersionUID = 8709304673384442111L;
    private static final String TAG = MoneyDate.class.getSimpleName();

    private SerializableDate _saveDate;
    private String _user;
    private SerializableList<MoneyMeterData> _moneyMeterDataList;

    public MoneyDate(
            @NonNull SerializableDate saveDate,
            @NonNull String user,
            @NonNull SerializableList<MoneyMeterData> moneyMeterDataList) {
        _saveDate = saveDate;
        _user = user;
        _moneyMeterDataList = moneyMeterDataList;
    }

    public SerializableDate GetSaveDate() {
        return _saveDate;
    }

    public void SetSaveDate(@NonNull SerializableDate saveDate) {
        _saveDate = saveDate;
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

    public double GetAmount() {
        double amount = 0;
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            amount += _moneyMeterDataList.getValue(index).GetAmount();
        }
        return amount;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (SaveDate: %s );(User: %s ))", TAG, _saveDate, _user);
    }
}
