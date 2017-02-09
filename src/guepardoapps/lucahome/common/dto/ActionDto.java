package guepardoapps.lucahome.common.dto;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.ServerActions;

public class ActionDto implements Serializable {

	private static final long serialVersionUID = 2713570574400030853L;

	private int _id;
	private String _socket;
	private String _action;

	private static final String TAG = ActionDto.class.getName();

	public ActionDto(int id, String socket, String action) {
		_id = id;
		_socket = socket;
		_action = action;
	}

	public int GetId() {
		return _id;
	}

	public String GetSocket() {
		return _socket;
	}

	public String GetAction() {
		return _action;
	}

	public String GetCommandSet() {
		return ServerActions.SET_SOCKET + _socket + _action;
	}

	public String GetNotificationBroadcast() {
		return Broadcasts.NOTIFICATION_SOCKET + _socket.toUpperCase(Locale.GERMAN);
	}

	@Override
	public String toString() {
		return "{" + TAG + ":{Id:" + String.valueOf(_id) + "};{Socket:" + _socket + "};{Action:" + _action + "};}";
	}
}
