package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

public class WirelessTimer extends WirelessSchedule {
    private static final String Tag = WirelessTimer.class.getSimpleName();

    public WirelessTimer(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull Calendar dateTime,
            boolean isActive,
            UUID wirelessSocketUuid,
            boolean wirelessSocketAction,
            UUID wirelessSwitchUuid,
            boolean isOnServer,
            @NonNull LucaServerDbAction lucaServerDbAction) {
        super(uuid, name, dateTime, isActive, wirelessSocketUuid, wirelessSocketAction, wirelessSwitchUuid, isOnServer, lucaServerDbAction);
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&wirelesssocket=%s&gpio=&wirelessswitch=%s&weekday=%d&hour=%d&minute=%d&action=%s&isTimer=1",
                LucaServerActionTypes.ADD_WIRELESS_SCHEDULE.toString(),
                _uuid, _name, _wirelessSocketUuid, _wirelessSwitchUuid,
                _dateTime.get(Calendar.DAY_OF_WEEK), _dateTime.get(Calendar.HOUR), _dateTime.get(Calendar.MINUTE),
                _wirelessSocketAction ? "1" : "0");
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&wirelesssocket=%s&gpio=&wirelessswitch=%s&weekday=%d&hour=%d&minute=%d&action=%s&isTimer=1&isactive=%s",
                LucaServerActionTypes.UPDATE_WIRELESS_SCHEDULE.toString(),
                _uuid, _name, _wirelessSocketUuid, _wirelessSwitchUuid,
                _dateTime.get(Calendar.DAY_OF_WEEK), _dateTime.get(Calendar.HOUR), _dateTime.get(Calendar.MINUTE),
                _wirelessSocketAction ? "1" : "0", _isActive ? "1" : "0");
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Name\":\"%s\",\"WirelessSocketId\":\"%s\",\"WirelessSocketAction\":\"%s\",\"WirelessSwitchId\":\"%s\",\"DateTime\":\"%s\",\"IsActive\":\"%s\"}",
                Tag, _uuid, _name, _wirelessSocketUuid, _wirelessSocketAction, _wirelessSwitchUuid, _dateTime, _isActive);
    }
}
