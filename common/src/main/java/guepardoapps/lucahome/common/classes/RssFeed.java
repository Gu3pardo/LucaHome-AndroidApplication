package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"WeakerAccess"})
public class RssFeed implements ILucaClass {
    private static final String Tag = RssFeed.class.getSimpleName();

    private UUID _uuid;
    private String _title;
    private String _url;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public RssFeed(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String url,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _title = title;
        _url = url;
        _isOnServer = isOnServer;
        _serverDbAction = serverDbAction;
    }

    public RssFeed() {
        this(UUID.randomUUID(), "", "", false, LucaServerDbAction.Null);
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

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&url=%s",
                LucaServerActionTypes.ADD_RSS_FEED.toString(), _uuid, _title, _url);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&title=%s&url=%s",
                LucaServerActionTypes.UPDATE_RSS_FEED.toString(), _uuid, _title, _url);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_RSS_FEED.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"Url\":\"%s\"}",
                Tag, _uuid, _title, _url);
    }
}
