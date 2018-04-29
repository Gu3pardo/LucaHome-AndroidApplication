package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"WeakerAccess"})
public class RadioStream implements ILucaClass {
    private static final String Tag = RadioStream.class.getSimpleName();

    private UUID _uuid;
    private String _title;
    private String _url;
    private int _playCount;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public RadioStream(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String url,
            int playCount,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _title = title;
        _url = url;
        _playCount = playCount;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    public RadioStream() {
        this(UUID.randomUUID(), "", "", 0, false, LucaServerDbAction.Null);
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public String GetTitle() {
        return _title;
    }

    public String GetUrl() {
        return _url;
    }

    public int GetPlayCount() {
        return _playCount;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&url=%s&playcount=%d",
                LucaServerActionTypes.ADD_RADIO_STREAM.toString(), _uuid, _title, _url, _playCount);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&url=%s&playcount=%d",
                LucaServerActionTypes.UPDATE_RADIO_STREAM.toString(), _uuid, _title, _url, _playCount);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_RADIO_STREAM.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"Url\":\"%s\",\"PlayCount\":%d}",
                Tag, _uuid, _title, _url, _playCount);
    }
}
