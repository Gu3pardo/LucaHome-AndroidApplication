package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class WirelessSwitch extends WirelessSocket {
    private static final String Tag = WirelessSwitch.class.getSimpleName();

    public static final String SettingsHeader = "SharedPref_Notification_Switch_";

    private int _remoteId;
    private char _keyCode;
    private boolean _action;

    public WirelessSwitch(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            @NonNull String name,
            int remoteId,
            char keyCode,
            boolean action,
            @NonNull Calendar lastTriggerDateTime,
            @NonNull String lastTriggerUser,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        super(uuid, roomUuid, name, "", false, lastTriggerDateTime, lastTriggerUser, isOnServer, serverDbAction);
        _remoteId = remoteId;
        _keyCode = keyCode;
        _action = action;
    }

    public int GetRemoteId() {
        return _remoteId;
    }

    public void SetRemoteId(int remoteId) {
        _remoteId = remoteId;
    }

    public char GetKeyCode() {
        return _keyCode;
    }

    public void SetKeyCode(char keyCode) {
        _keyCode = keyCode;
    }

    public boolean GetAction() {
        return _action;
    }

    public void SetAction(boolean action) {
        _action = action;
    }

    @Override
    public String GetCommandSetState() throws NoSuchMethodException {
        throw new NoSuchMethodException("CommandSetState not available for WirelessSwitch");
    }

    public String GetCommandToggle() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.TOGGLE_WIRELESS_SWITCH.toString(), _uuid);
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&name=%s&remoteid=%s&keycode=%s",
                LucaServerActionTypes.ADD_WIRELESS_SWITCH.toString(), _uuid, _roomUuid, _name, _remoteId, _keyCode);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&name=%s&remoteid=%s&keycode=%s",
                LucaServerActionTypes.UPDATE_WIRELESS_SWITCH.toString(), _uuid, _roomUuid, _name, _remoteId, _keyCode);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_WIRELESS_SWITCH.toString(), _uuid);
    }

    @Override
    public int GetDrawable() {
        if (_action) {
            return R.drawable.wireless_switch_on;
        }
        return R.drawable.wireless_switch_off;
    }

    @Override
    public String GetSettingsKey() {
        return String.format(Locale.getDefault(), "%s%s", SettingsHeader, _name);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"RoomUuid\":\"%s\",\"Name\":\"%s\",\"KeyCode\":\"%s\",\"Action\":\"%s\"}",
                Tag, _uuid, _roomUuid, _name, _keyCode, (_action ? "1" : "0"));
    }
}
