package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess"})
public class Change implements ILucaClass {
    private static final String Tag = Change.class.getSimpleName();

    private UUID _uuid;
    private String _type;
    private Calendar _dateTime;
    private String _user;

    public Change(
            @NonNull UUID uuid,
            @NonNull String type,
            @NonNull Calendar dateTime,
            @NonNull String user) {
        _uuid = uuid;
        _type = type;
        _dateTime = dateTime;
        _user = user;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetRoomUuid not implemented for " + Tag);
    }

    public String GetType() {
        return _type;
    }

    public Calendar GetDateTime() {
        return _dateTime;
    }

    public String GetUser() {
        return _user;
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
                "{\"Class\":\"%s\",\"UUid\":\"%s\",\"Type\":\"%s\",\"DateTime\":\"%s\",\"User\":\"%s\"}",
                Tag, _uuid, _type, _dateTime, _user);
    }
}
