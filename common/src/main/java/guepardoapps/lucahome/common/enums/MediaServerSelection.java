package guepardoapps.lucahome.common.enums;

import android.support.annotation.NonNull;

import java.io.Serializable;

public enum MediaServerSelection implements Serializable {

    NULL(-1, "", "", "", false),
    MEDIA_SERVER_KITCHEN(0, "192.168.178.22", "MediaServer_Kitchen", "Kitchen", false),
    MEDIA_SERVER_SLEEPING(1, "192.168.178.24", "MediaServer_Sleeping", "Sleeping Room", true),
    MEDIA_SERVER_LIVING(1, "192.168.178.20", "MediaServer_Living", "Living Room", false);

    private int _id;
    private String _ip;
    private String _socket;
    private String _location;
    private boolean _isSleepingServer;

    MediaServerSelection(int id, @NonNull String ip, @NonNull String socket, @NonNull String location, boolean isSleepingServer) {
        _id = id;
        _ip = ip;
        _socket = socket;
        _location = location;
        _isSleepingServer = isSleepingServer;
    }

    public int GetId() {
        return _id;
    }

    public String GetIp() {
        return _ip;
    }

    public String GetSocket() {
        return _socket;
    }

    public String GetLocation() {
        return _location;
    }

    public boolean IsSleepingServer() {
        return _isSleepingServer;
    }

    @Override
    public String toString() {
        return String.format("MediaMirrorSelection:{%s:%s:%s:%s:%s}", _id, _ip, _socket, _location, _isSleepingServer);
    }

    public static MediaServerSelection GetById(int id) {
        for (MediaServerSelection e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return NULL;
    }

    public static MediaServerSelection GetByIp(@NonNull String ip) {
        for (MediaServerSelection e : values()) {
            if (e._ip.equalsIgnoreCase(ip)) {
                return e;
            }
        }
        return NULL;
    }

    public static MediaServerSelection GetByLocation(@NonNull String location) {
        for (MediaServerSelection e : values()) {
            if (e._location.contains(location)) {
                return e;
            }
        }
        return NULL;
    }
}
