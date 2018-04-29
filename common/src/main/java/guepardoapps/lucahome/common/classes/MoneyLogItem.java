package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MoneyLogItem implements ILucaClass {
    private static final String Tag = MoneyLogItem.class.getSimpleName();

    private UUID _uuid;
    private UUID _typeUuid;

    private String _bank;
    private String _plan;
    private double _amount;
    private String _unit;

    private Calendar _saveDate;
    private String _user;

    private boolean _isOnServer;
    private ILucaClass.LucaServerDbAction _serverDbAction;

    public MoneyLogItem(
            @NonNull UUID uuid,
            @NonNull UUID typeUuid,
            @NonNull String bank,
            @NonNull String plan,
            double amount,
            @NonNull String unit,
            @NonNull Calendar saveDate,
            @NonNull String user,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _typeUuid = typeUuid;
        _bank = bank;
        _plan = plan;
        _amount = amount;
        _unit = unit;
        _saveDate = saveDate;
        _user = user;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public void SetTypeUuid(UUID typeUuid) {
        _typeUuid = typeUuid;
    }

    public UUID GetTypeUuid() {
        return _typeUuid;
    }

    public void SetBank(@NonNull String bank) {
        _bank = bank;
    }

    public String GetBank() {
        return _bank;
    }

    public void SetPlan(@NonNull String plan) {
        _plan = plan;
    }

    public String GetPlan() {
        return _plan;
    }

    public void SetAmount(double amount) {
        _amount = amount;
    }

    public double GetAmount() {
        return _amount;
    }

    public void SetUnit(@NonNull String unit) {
        _unit = unit;
    }

    public String GetUnit() {
        return _unit;
    }

    public void SetSaveDate(@NonNull Calendar saveDate) {
        _saveDate = saveDate;
    }

    public Calendar GetSaveDate() {
        return _saveDate;
    }

    public void SetUser(@NonNull String user) {
        _user = user;
    }

    public String GetUser() {
        return _user;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&typeuuid=%s&bank=%s&plan=%s&amount=%.2f&unit=%s&day=%d&month=%d&year=%d&username=%s",
                LucaServerActionTypes.ADD_MONEY_LOG.toString(),
                _uuid, _typeUuid, _bank, _plan, _amount, _unit, _saveDate.get(Calendar.DAY_OF_MONTH), _saveDate.get(Calendar.MONTH), _saveDate.get(Calendar.YEAR), _user);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&typeuuid=%s&bank=%s&plan=%s&amount=%.2f&unit=%s&day=%d&month=%d&year=%d&username=%s",
                LucaServerActionTypes.UPDATE_MONEY_LOG.toString(),
                _uuid, _typeUuid, _bank, _plan, _amount, _unit, _saveDate.get(Calendar.DAY_OF_MONTH), _saveDate.get(Calendar.MONTH), _saveDate.get(Calendar.YEAR), _user);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerActionTypes.DELETE_MONEY_LOG.toString(), _uuid);
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
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"TypeUuid\":\"%s\",\"Bank\":\"%s\",\"Plan\":%s,\"Amount\":%.2f,\"Unit\":\"%s\",\"SaveDate\":\"%s\",\"User\":\"%s\"}",
                Tag, _uuid, _typeUuid, _bank, _plan, _amount, _unit, _saveDate, _user);
    }
}
