package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.mediaserver.BundledData;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"WeakerAccess"})
public class MediaServer implements ILucaClass {
    private static final String Tag = Room.class.getSimpleName();

    private UUID _uuid;
    private UUID _roomUuid;
    private String _ip;
    private boolean _isSleepingServer;
    private UUID _wirelessSocketUuid;

    private boolean _isActive;
    private BundledData _bundledData;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public MediaServer(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            @NonNull String ip,
            boolean isSleepingServer,
            @NonNull UUID wirelessSocketUuid,
            boolean isActive,
            @NonNull BundledData bundledData,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _roomUuid = roomUuid;
        _ip = ip;
        _isSleepingServer = isSleepingServer;
        _wirelessSocketUuid = wirelessSocketUuid;
        _isActive = isActive;
        _bundledData = bundledData;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    public MediaServer() {
        this(UUID.randomUUID(), UUID.randomUUID(), "", false, UUID.randomUUID(), false, new BundledData(), false, LucaServerDbAction.Null);
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() {
        return _roomUuid;
    }

    public String GetIp() {
        return _ip;
    }

    public boolean IsSleepingServer() {
        return _isSleepingServer;
    }

    public UUID GetWirelessSocketUuid() {
        return _wirelessSocketUuid;
    }

    public void SetIsActive(boolean isActive) {
        _isActive = isActive;
    }

    public boolean IsActive() {
        return _isActive;
    }

    public void SetBundledData(@NonNull BundledData bundledData) {
        _bundledData = bundledData;
    }

    public BundledData GetBundledData() {
        return _bundledData;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&ip=%s&issleepingserver=%s&wirelesssocketuuid=%s",
                LucaServerActionTypes.ADD_MEDIA_SERVER.toString(), _uuid, _roomUuid, _ip, _isSleepingServer ? "1" : "0", _wirelessSocketUuid);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&roomuuid=%s&ip=%s&issleepingserver=%s&wirelesssocketuuid=%s",
                LucaServerActionTypes.UPDATE_MEDIA_SERVER.toString(), _uuid, _roomUuid, _ip, _isSleepingServer ? "1" : "0", _wirelessSocketUuid);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_MEDIA_SERVER.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"RoomUuid\":\"%s\",\"Ip\":\"%s\",\"IsSleepingServer\":\"%s\",\"WirelessSocketUuid\":\"%s\",\"BundledData\":\"%s\"}",
                Tag, _uuid, _roomUuid, _ip, _isSleepingServer, _wirelessSocketUuid, _bundledData);
    }
}
