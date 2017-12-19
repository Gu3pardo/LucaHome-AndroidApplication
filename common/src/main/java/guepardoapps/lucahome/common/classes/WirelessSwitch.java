package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.enums.LucaServerAction;

public class WirelessSwitch extends WirelessSocket {
    private static final long serialVersionUID = 5579389712706412340L;
    private static final String TAG = WirelessSwitch.class.getSimpleName();

    public static final String SETTINGS_HEADER = "SharedPref_Notification_Switch_";

    private int _remoteId;
    private char _keyCode;
    private boolean _action;

    public WirelessSwitch(
            int id,
            @NonNull String name,
            @NonNull String area,
            int remoteId,
            char keyCode,
            boolean isActivated,
            boolean action,
            @NonNull SerializableDate lastTriggerDate,
            @NonNull SerializableTime lastTriggerTime,
            @NonNull String lastTriggerUser,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        super(id, name, area, "", isActivated, lastTriggerDate, lastTriggerTime, lastTriggerUser, isOnServer, serverDbAction);
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
    public String CommandSetState() throws NoSuchMethodException {
        throw new NoSuchMethodException("CommandSetState not available for WirelessSwitch");
    }

    @Override
    public String CommandChangeState() throws NoSuchMethodException {
        throw new NoSuchMethodException("CommandChangeState not available for WirelessSwitch");
    }

    public String CommandToggle() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerAction.TOGGLE_SWITCH.toString(), _name);
    }

    @Override
    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%s&area=%s&remoteid=%s&keycode=%s", LucaServerAction.ADD_SWITCH.toString(), _name, _area, _remoteId, _keyCode);
    }

    @Override
    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%s&area=%s&remoteid=%s&keycode=%s", LucaServerAction.UPDATE_SWITCH.toString(), _name, _area, _remoteId, _keyCode);
    }

    @Override
    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerAction.DELETE_SWITCH.toString(), _name);
    }

    @Override
    public int GetDrawable() {
        if (_action) {
            return R.drawable.switch_on;
        } else {
            return R.drawable.switch_off;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "( %s: (Name: %s );(Area: %s );(KeyCode: %s );(Action: %s ))", TAG, _name, _area, _keyCode, (_action ? "1" : "0"));
    }
}
