package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"unused", "WeakerAccess"})
public class User implements ILucaClass {
    private static final String Tag = User.class.getSimpleName();

    private UUID _uuid;
    private String _name;
    private String _passphrase;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public User(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull String passphrase,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _name = name;
        _passphrase = passphrase;
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

    public void SetPassphrase(@NonNull String passphrase) {
        _passphrase = passphrase;
    }

    public String GetPassphrase() {
        return _passphrase;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&passphrase=%s",
                LucaServerActionTypes.ADD_USER.toString(), _uuid, _name, _passphrase);
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&passphrase=%s",
                LucaServerActionTypes.UPDATE_USER.toString(), _uuid, _name, _passphrase);
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_USER.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Name\":\"%s\",\"Passphrase\":\"-/-\"}",
                Tag, _uuid, _name);
    }
}
