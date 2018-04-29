package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Coin implements ILucaClass {
    private static final String Tag = Coin.class.getSimpleName();

    public enum Trend {Null, Fall, Rise}

    private UUID _uuid;
    private String _user;
    private String _type;
    private double _amount;

    private double _currentConversion;
    private Trend _currentTrend;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public Coin(
            @NonNull UUID uuid,
            @NonNull String user,
            @NonNull String type,
            double amount,
            double currentConversion,
            @NonNull Trend currentTrend,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _user = user;
        _type = type;
        _amount = amount;
        _currentConversion = currentConversion;
        _currentTrend = currentTrend;
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

    public void SetUser(@NonNull String user) {
        _user = user;
    }

    public String GetUser() {
        return _user;
    }

    public void SetType(@NonNull String type) {
        _type = type;
    }

    public String GetType() {
        return _type;
    }

    public void SetAmount(double amount) {
        _amount = amount;
    }

    public double GetAmount() {
        return _amount;
    }

    public void SetCurrentConversion(double currentConversion) {
        _currentConversion = currentConversion;
    }

    public double GetCurrentConversion() {
        return _currentConversion;
    }

    public String GetCurrentConversionString() {
        return String.format(Locale.getDefault(), "%.2f €", _currentConversion);
    }

    public void SetCurrentTrend(@NonNull Trend currentTrend) {
        _currentTrend = currentTrend;
    }

    public Trend GetCurrentTrend() {
        return _currentTrend;
    }

    public double GetValue() {
        return _amount * _currentConversion;
    }

    public String GetValueString() {
        return String.format(Locale.getDefault(), "%.2f €", GetValue());
    }

    public int GetIcon() {
        switch (_type) {
            case "BCH":
                return R.drawable.coin_bch;
            case "BTC":
                return R.drawable.coin_btc;
            case "DASH":
                return R.drawable.coin_dash;
            case "ETC":
                return R.drawable.coin_etc;
            case "ETH":
                return R.drawable.coin_eth;
            case "IOTA":
                return R.drawable.coin_iota;
            case "LTC":
                return R.drawable.coin_ltc;
            case "XLM":
                return R.drawable.coin_xlm;
            case "XMR":
                return R.drawable.coin_xmr;
            case "XRP":
                return R.drawable.coin_xrp;
            case "ZEC":
                return R.drawable.coin_zec;
            default:
                return R.drawable.coin_btc;
        }
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&username=%s&type=%s&amount=%.6f",
                LucaServerActionTypes.ADD_COIN.toString(), _uuid, _user, _type, _amount);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&username=%s&type=%s&amount=%.6f",
                LucaServerActionTypes.UPDATE_COIN.toString(), _uuid, _user, _type, _amount);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_COIN.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"User\":\"%s\",\"Type\":\"%s\",\"Amount\":%.6f,\"CurrentConversion\":%.6f,\"Trend\":\"%s\"}",
                Tag, _uuid, _user, _type, _amount, _currentConversion, _currentTrend);
    }
}
