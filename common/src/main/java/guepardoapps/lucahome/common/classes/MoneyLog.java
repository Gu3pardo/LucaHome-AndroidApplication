package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MoneyLog implements ILucaClass {
    private static final String Tag = MoneyLog.class.getSimpleName();

    private UUID _uuid;
    private String _bank;
    private String _plan;
    private String _user;
    private ArrayList<MoneyLogItem> _moneyLogItemList;

    public MoneyLog(
            @NonNull UUID uuid,
            @NonNull String bank,
            @NonNull String plan,
            @NonNull String user,
            @NonNull ArrayList<MoneyLogItem> moneyLogItemList) {
        _uuid = uuid;
        _bank = bank;
        _plan = plan;
        _user = user;
        _moneyLogItemList = moneyLogItemList;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
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

    public void SetUser(@NonNull String user) {
        _user = user;
    }

    public String GetUser() {
        return _user;
    }

    public void SetMoneyLogItemList(@NonNull ArrayList<MoneyLogItem> moneyLogItemList) {
        _moneyLogItemList = moneyLogItemList;
    }

    public ArrayList<MoneyLogItem> GetMoneyLogItemList() {
        return _moneyLogItemList;
    }

    @Override
    public String GetCommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandAdd not implemented for " + Tag);
    }

    @Override
    public String GetCommandUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandUpdate not implemented for " + Tag);
    }

    @Override
    public String GetCommandDelete() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandDelete not implemented for " + Tag);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetIsOnServer not implemented for " + Tag);
    }

    @Override
    public boolean GetIsOnServer() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetIsOnServer not implemented for " + Tag);
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction lucaServerDbAction) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetServerDbAction not implemented for " + Tag);
    }

    @Override
    public LucaServerDbAction GetServerDbAction() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetServerDbAction not implemented for " + Tag);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Bank\":\"%s\",\"Plan\":\"%s\",\"User\":\"%s\"}",
                Tag, _uuid, _bank, _plan, _user);
    }
}
