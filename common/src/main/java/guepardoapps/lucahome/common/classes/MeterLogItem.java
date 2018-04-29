package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MeterLogItem implements ILucaClass {
    private static final String Tag = MeterLogItem.class.getSimpleName();

    private UUID _uuid;
    private UUID _roomUuid;
    private UUID _typeUuid;
    private String _type;

    private Calendar _saveDateTime;

    private String _meterId;
    private double _value;
    private String _imageName;

    private boolean _isOnServer;
    private ILucaClass.LucaServerDbAction _serverDbAction;

    public MeterLogItem(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            @NonNull UUID typeUuid,
            @NonNull String type,
            @NonNull Calendar saveDateTime,
            @NonNull String meterId,
            double value,
            @NonNull String imageName,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _roomUuid = roomUuid;
        _typeUuid = typeUuid;
        _type = type;
        _saveDateTime = saveDateTime;
        _meterId = meterId;
        _value = value;
        _imageName = imageName;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() {
        return _roomUuid;
    }

    public void SetTypeUuid(UUID typeUuid) {
        _typeUuid = typeUuid;
    }

    public UUID GetTypeUuid() {
        return _typeUuid;
    }

    public void SetType(@NonNull String type) {
        _type = type;
    }

    public String GetType() {
        return _type;
    }

    public void SetSaveDateTime(@NonNull Calendar saveDateTime) {
        _saveDateTime = saveDateTime;
    }

    public Calendar GetSaveDateTime() {
        return _saveDateTime;
    }

    public void SetMeterId(@NonNull String meterId) {
        _meterId = meterId;
    }

    public String GetMeterId() {
        return _meterId;
    }

    public void SetValue(double value) {
        _value = value;
    }

    public double GetValue() {
        return _value;
    }

    public void SetImageName(@NonNull String imageName) {
        _imageName = imageName;
    }

    public String GetImageName() {
        return _imageName;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&roomUuid=%s&typeUuid=%s&type=%s&day=%d&month=%d&year=%d&hour=%d&minute=%d&meterId=%s&value=%.6f&imageName=%s",
                LucaServerActionTypes.ADD_METER_LOG.toString(),
                _uuid, _roomUuid, _typeUuid, _type,
                _saveDateTime.get(Calendar.DAY_OF_MONTH), _saveDateTime.get(Calendar.MONTH), _saveDateTime.get(Calendar.YEAR),
                _saveDateTime.get(Calendar.HOUR), _saveDateTime.get(Calendar.MINUTE),
                _meterId, _value, _imageName);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&roomUuid=%s&typeUuid=%s&type=%s&day=%d&month=%d&year=%d&hour=%d&minute=%d&meterId=%s&value=%.6f&imageName=%s",
                LucaServerActionTypes.UPDATE_METER_LOG.toString(),
                _uuid, _roomUuid, _typeUuid, _type,
                _saveDateTime.get(Calendar.DAY_OF_MONTH), _saveDateTime.get(Calendar.MONTH), _saveDateTime.get(Calendar.YEAR),
                _saveDateTime.get(Calendar.HOUR), _saveDateTime.get(Calendar.MINUTE),
                _meterId, _value, _imageName);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(), "%s%s", LucaServerActionTypes.DELETE_METER_LOG.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"RoomUuid\":\"%s\",\"TypeUuid\":\"%s\",\"Type\":\"%s\",\"SaveDateTime\":\"%s\",\"MeterId\":\"%s\",\"Value\":%.6f,\"ImageName\":\"%s\"}",
                Tag, _uuid, _roomUuid, _typeUuid, _type, _saveDateTime, _meterId, _value, _imageName);
    }
}
