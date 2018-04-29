package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MeterLog implements ILucaClass {
    private static final String Tag = MeterLog.class.getSimpleName();

    private UUID _uuid;
    private UUID _roomUuid;
    private String _type;
    private String _meterId;
    private ArrayList<MeterLogItem> _meterLogList;

    public MeterLog(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            @NonNull String type,
            @NonNull String meterId,
            @NonNull ArrayList<MeterLogItem> meterLogList) {
        _uuid = uuid;
        _roomUuid = roomUuid;
        _type = type;
        _meterId = meterId;
        _meterLogList = meterLogList;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() {
        return _roomUuid;
    }

    public void SetType(@NonNull String type) {
        _type = type;
    }

    public String GetType() {
        return _type;
    }

    public void SetMeterId(@NonNull String meterId) {
        _meterId = meterId;
    }

    public String GetMeterId() {
        return _meterId;
    }

    public void SetMeterLogList(@NonNull ArrayList<MeterLogItem> meterLogList) {
        _meterLogList = meterLogList;
    }

    public ArrayList<MeterLogItem> GetMeterLogList() {
        return _meterLogList;
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Type\":\"%s\",\"MeterId\":\"%s\"}",
                Tag, _uuid, _type, _meterId);
    }
}
