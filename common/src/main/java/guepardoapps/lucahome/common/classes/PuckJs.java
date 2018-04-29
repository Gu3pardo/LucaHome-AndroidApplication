package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PuckJs implements ILucaClass {
    private static final String Tag = PuckJs.class.getSimpleName();

    private UUID _uuid;
    private UUID _roomUuid;
    private String _name;
    private String _mac;

    private int _batteryLevel;
    private int _lightValue;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public PuckJs(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            @NonNull String name,
            @NonNull String mac,
            int batteryLevel,
            int lightValue,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _roomUuid = roomUuid;
        _name = name;
        _mac = mac;
        _batteryLevel = batteryLevel;
        _lightValue = lightValue;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    public PuckJs(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            @NonNull String name,
            @NonNull String mac,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        this(uuid, roomUuid, name, mac, -1, -1, isOnServer, serverDbAction);
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() {
        return _roomUuid;
    }

    public void SetName(@NonNull String name) {
        _name = name;
    }

    public String GetName() {
        return _name;
    }

    public void SetMac(@NonNull String mac) {
        _mac = mac;
    }

    public String GetMac() {
        return _mac;
    }

    public void SetBatteryLevel(int batteryLevel) {
        _batteryLevel = batteryLevel;
    }

    public int GetBatteryLevel() {
        return _batteryLevel;
    }

    public void SetLightValue(int lightValue) {
        _lightValue = lightValue;
    }

    public int GetLightValue() {
        return _lightValue;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(), LucaServerActionTypes.ADD_PUCK_JS_F.toString(), _uuid, _roomUuid, _name, _mac);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(), LucaServerActionTypes.UPDATE_PUCK_JS_F.toString(), _uuid, _roomUuid, _name, _mac);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(), LucaServerActionTypes.DELETE_PUCK_JS_F.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"RoomUuid\":\"%s\",\"Name\":\"%s\",\"Mac\":\"%s\",\"BatteryLevel\":%d,\"LightValue\":%d}",
                Tag, _uuid, _roomUuid, _name, _mac, _batteryLevel, _lightValue);
    }
}
