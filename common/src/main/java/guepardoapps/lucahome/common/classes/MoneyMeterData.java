package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

@SuppressWarnings({"unused"})
public class MoneyMeterData implements Serializable {
    private static final long serialVersionUID = 8799048598384442111L;
    private static final String TAG = MoneyMeterData.class.getSimpleName();

    public enum ServerAction {Null, Add, Update}

    private int _id;
    private int _typeId;

    private String _bank;
    private String _plan;
    private double _amount;
    private String _unit;

    private SerializableDate _saveDate;
    private String _user;

    private ServerAction _serverAction;
    private ILucaClass.LucaServerDbAction _serverDbAction;

    public MoneyMeterData(
            int id,
            int typeId,

            @NonNull String bank,
            @NonNull String plan,
            double amount,
            @NonNull String unit,

            @NonNull SerializableDate saveDate,
            @NonNull String user) {
        _id = id;
        _typeId = typeId;

        _bank = bank;
        _plan = plan;
        _amount = amount;
        _unit = unit;

        _saveDate = saveDate;
        _user = user;

        _serverAction = MoneyMeterData.ServerAction.Null;
        _serverDbAction = ILucaClass.LucaServerDbAction.Null;
    }

    public int GetId() {
        return _id;
    }

    public void SetId(int id) {
        _id = id;
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

    public double GetAmount() {
        return _amount;
    }

    public void SetAmount(double amount) {
        _amount = amount;
    }

    public String GetUnit() {
        return _unit;
    }

    public void SetUnit(@NonNull String unit) {
        _unit = unit;
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

    public MoneyMeterData.ServerAction GetServerAction() {
        return _serverAction;
    }

    public void SetServerAction(@NonNull MoneyMeterData.ServerAction serverAction) {
        _serverAction = serverAction;
    }

    public void SetServerDbAction(@NonNull ILucaClass.LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    public ILucaClass.LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%d&typeid=%d&bank=%s&plan=%s&amount=%.2f&unit=%s&day=%d&month=%d&year=%d&username=%s",
                LucaServerAction.ADD_MONEY_METER_DATA.toString(), _id, _typeId, _bank, _plan, _amount, _unit, _saveDate.DayOfMonth(), _saveDate.Month(), _saveDate.Year(), _user);
    }

    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%d&typeid=%d&bank=%s&plan=%s&amount=%.2f&unit=%s&day=%d&month=%d&year=%d&username=%s",
                LucaServerAction.UPDATE_MONEY_METER_DATA.toString(), _id, _typeId, _bank, _plan, _amount, _unit, _saveDate.DayOfMonth(), _saveDate.Month(), _saveDate.Year(), _user);
    }

    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%d", LucaServerAction.DELETE_MONEY_METER_DATA.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Id: %d );(TypeId: %d );(Bank: %s );(Plan: %s );(Amount: %.2f );(Unit: %s );(SaveDate: %s );(User: %s ))",
                TAG, _id, _typeId, _bank, _plan, _amount, _unit, _saveDate, _user);
    }
}
