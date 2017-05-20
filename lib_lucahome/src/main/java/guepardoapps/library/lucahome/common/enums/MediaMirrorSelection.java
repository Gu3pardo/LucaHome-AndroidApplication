package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

public enum MediaMirrorSelection implements Serializable {

	NULL(0, "", "", "", false), 
	MEDIAMIRROR_1(1, "192.168.178.147", "MediaMirror1", "Workspace Jonas", false), 
	MEDIAMIRROR_2(2, "192.168.178.20", "MediaMirror2", "Sleeping Room", true), 
	MEDIAMIRROR_3(3, "192.168.178.26", "MediaMirror3", "Kitchen", false);

	private int _id;
	private String _ip;
	private String _socket;
	private String _location;
	private boolean _isSleepingMirror;

	MediaMirrorSelection(int id, String ip, String socket, String location, boolean isSleepingMirror) {
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

	public static MediaMirrorSelection GetById(int id) {
		for (MediaMirrorSelection e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return NULL;
	}

	public static MediaMirrorSelection GetByIp(String ip) {
		for (MediaMirrorSelection e : values()) {
			if (e._ip.equalsIgnoreCase(ip)) {
				return e;
			}
		}
		return NULL;
	}

	public static MediaMirrorSelection GetBySocket(String socket) {
		for (MediaMirrorSelection e : values()) {
			if (e._socket.contains(socket)) {
				return e;
			}
		}
		return NULL;
	}

	public static MediaMirrorSelection GetByLocation(String location) {
		for (MediaMirrorSelection e : values()) {
			if (e._location.contains(location)) {
				return e;
			}
		}
		return NULL;
	}
}
