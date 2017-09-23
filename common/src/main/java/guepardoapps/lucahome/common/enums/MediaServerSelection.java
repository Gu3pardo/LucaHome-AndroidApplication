package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum MediaServerSelection implements Serializable {

    MEDIAMIRROR_1(0, "192.168.178.147", "MediaMirror1", "Workspace Jonas", false),
    MEDIAMIRROR_2(1, "192.168.178.20", "MediaMirror2", "Sleeping Room", true),
    NULL(-1, "", "", "", false);

    private int _id;
    private String _ip;
    private String _socket;
    private String _location;
    private boolean _isSleepingMirror;

    MediaServerSelection(int id, String ip, String socket, String location, boolean isSleepingMirror) {
        _id = id;
        _ip = ip;
        _socket = socket;
        _location = location;
        _isSleepingMirror = isSleepingMirror;
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

    public boolean IsSleepingMirror() {
        return _isSleepingMirror;
    }

    @Override
    public String toString() {
        return String.format("MediaMirrorSelection:{%s:%s:%s:%s:%s}", _id, _ip, _socket, _location, _isSleepingMirror);
    }

    public static MediaServerSelection GetById(int id) {
        for (MediaServerSelection e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return NULL;
    }

    public static MediaServerSelection GetByIp(String ip) {
        for (MediaServerSelection e : values()) {
            if (e._ip.equalsIgnoreCase(ip)) {
                return e;
            }
        }
        return NULL;
    }

    public static MediaServerSelection GetBySocket(String socket) {
        for (MediaServerSelection e : values()) {
            if (e._socket.contains(socket)) {
                return e;
            }
        }
        return NULL;
    }

    public static MediaServerSelection GetByLocation(String location) {
        for (MediaServerSelection e : values()) {
            if (e._location.contains(location)) {
                return e;
            }
        }
        return NULL;
    }
}
