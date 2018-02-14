package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"WeakerAccess"})
public class WirelessSchedule implements ILucaClass {
    private static final String Tag = WirelessSchedule.class.getSimpleName();

    protected UUID _uuid;
    protected String _name;
    protected Calendar _dateTime;
    protected boolean _isActive;

    protected UUID _wirelessSocketUuid;
    protected boolean _wirelessSocketAction;
    protected UUID _wirelessSwitchUuid;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public WirelessSchedule(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull Calendar dateTime,
            boolean isActive,
            UUID wirelessSocketUuid,
            boolean wirelessSocketAction,
            UUID wirelessSwitchUuid,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _name = name;
        _dateTime = dateTime;
        _isActive = isActive;
        _wirelessSocketUuid = wirelessSocketUuid;
        _wirelessSocketAction = wirelessSocketAction;
        _wirelessSwitchUuid = wirelessSwitchUuid;
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

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetName() {
        return _name;
    }

    public void SetDateTime(@NonNull Calendar dateTime) {
        _dateTime = dateTime;
    }

    public Calendar GetDateTime() {
        return _dateTime;
    }

    public void SetActive(boolean isActive) {
        _isActive = isActive;
    }

    public boolean IsActive() {
        return _isActive;
    }

    public void SetWirelessSocketUuid(UUID wirelessSocketUuid) {
        _wirelessSocketUuid = wirelessSocketUuid;
    }

    public UUID GetWirelessSocketUuid() {
        return _wirelessSocketUuid;
    }

    public void SetWirelessSocketAction(boolean wirelessSocketAction) {
        _wirelessSocketAction = wirelessSocketAction;
    }

    public boolean GetWirelessSocketAction() {
        return _wirelessSocketAction;
    }

    public void SetWirelessSwitch(UUID wirelessSwitchUuid) {
        _wirelessSwitchUuid = wirelessSwitchUuid;
    }

    public UUID GetWirelessSwitchUuid() {
        return _wirelessSwitchUuid;
    }

    public String GetCommandSetState() {
        return String.format(Locale.getDefault(), "%s%s%s",
                LucaServerActionTypes.SET_WIRELESS_SCHEDULE.toString(),
                _uuid, (_isActive ? Constants.StateOn : Constants.StateOff));
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&wirelesssocket=%s&gpio=&wirelessswitch=%s&weekday=%d&hour=%d&minute=%d&action=%s&isTimer=0",
                LucaServerActionTypes.ADD_WIRELESS_SCHEDULE.toString(),
                _uuid, _name, _wirelessSocketUuid, _wirelessSwitchUuid,
                _dateTime.get(Calendar.DAY_OF_WEEK), _dateTime.get(Calendar.HOUR), _dateTime.get(Calendar.MINUTE),
                _wirelessSocketAction ? "1" : "0");
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&wirelesssocket=%s&gpio=&wirelessswitch=%s&weekday=%d&hour=%d&minute=%d&action=%s&isTimer=0&isactive=%s",
                LucaServerActionTypes.UPDATE_WIRELESS_SCHEDULE.toString(),
                _uuid, _name, _wirelessSocketUuid, _wirelessSwitchUuid,
                _dateTime.get(Calendar.DAY_OF_WEEK), _dateTime.get(Calendar.HOUR), _dateTime.get(Calendar.MINUTE),
                _wirelessSocketAction ? "1" : "0", _isActive ? "1" : "0");
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_WIRELESS_SCHEDULE.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Name\":\"%s\",\"WirelessSocketId\":\"%s\",\"WirelessSocketAction\":\"%s\",\"WirelessSwitchId\":\"%s\",\"DateTime\":\"%s\",\"IsActive\":\"%s\"}",
                Tag, _uuid, _name, _wirelessSocketUuid, _wirelessSocketAction, _wirelessSwitchUuid, _dateTime, _isActive);
    }
}
