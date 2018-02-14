package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.enums.LucaServerActionTypes;

@SuppressWarnings({"WeakerAccess"})
public class Room implements ILucaClass {
    private static final String Tag = Room.class.getSimpleName();

    public enum RoomType {Null, Balcony, Basement, Bath, Cubby, Hall, Kitchen, LivingRoom, SleepingRoom}

    private UUID _uuid;
    private String _name;
    private RoomType _roomType;

    private boolean _isOnServer;
    private LucaServerDbAction _serverDbAction;

    public Room(
            @NonNull UUID uuid,
            @NonNull String name,
            @NonNull RoomType roomType,
            boolean isOnServer,
            @NonNull LucaServerDbAction serverDbAction) {
        _uuid = uuid;
        _name = name;
        _roomType = roomType;
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

    public String GetName() {
        return _name;
    }

    public RoomType GetRoomType() {
        return _roomType;
    }

    @Override
    public String GetCommandAdd() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&roomtype=%d",
                LucaServerActionTypes.ADD_ROOM.toString(), _uuid, _name, _roomType.ordinal());
    }

    @Override
    public String GetCommandUpdate() {
        return String.format(Locale.getDefault(),
                "%s%s&name=%s&roomtype=%d",
                LucaServerActionTypes.UPDATE_ROOM.toString(), _uuid, _name, _roomType.ordinal());
    }

    @Override
    public String GetCommandDelete() {
        return String.format(Locale.getDefault(),
                "%s%s",
                LucaServerActionTypes.DELETE_ROOM.toString(), _uuid);
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
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Name\":\"%s\",\"RoomType\":\"%s\"}",
                Tag, _uuid, _name, _roomType);
    }
}
