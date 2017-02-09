package guepardoapps.lucahome.common.classes;

import java.io.Serializable;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.receiver.sockets.SocketActionReceiver;

public class NotificationDetails implements Serializable {

	private static final long serialVersionUID = 5583702640190370633L;

	@SuppressWarnings("unused")
	private static final String TAG = NotificationDetails.class.getName();

	private int _iconActive;
	private int _iconDeactive;

	private String _textActive;
	private String _textDeactive;

	private WirelessSocketDto _socket;

	private boolean _iconIsVisible;

	private int _broadcastCodeActive;
	private int _broadcastCodeDeactive;

	private Context _context;

	public NotificationDetails(int iconActive, int iconDeactive, String textActive, String textDeactive,
			WirelessSocketDto socket, boolean iconIsVisible, int broadcastActiveCode, int broadcastDeactiveCode,
			Context context) {
		_iconActive = iconActive;
		_iconDeactive = iconDeactive;
		_textActive = textActive;
		_textDeactive = textDeactive;

		_socket = socket;

		_iconIsVisible = iconIsVisible;

		_broadcastCodeActive = broadcastActiveCode;
		_broadcastCodeDeactive = broadcastDeactiveCode;

		_context = context;
	}

	public int GetIconActive() {
		return _iconActive;
	}

	public int GetIconDeactive() {
		return _iconDeactive;
	}

	public String GetTextActive() {
		return _textActive;
	}

	public String GetTextDeactive() {
		return _textDeactive;
	}

	public WirelessSocketDto GetSocket() {
		return _socket;
	}

	public boolean GetCurrentSocketState() {
		return _socket.GetIsActivated();
	}

	public boolean GetIconIsVisible() {
		return _iconIsVisible;
	}

	public void SetIconIsVisible(boolean iconIsVisible) {
		_iconIsVisible = iconIsVisible;
	}

	public int GetBroadCastActiveCode() {
		return _broadcastCodeActive;
	}

	public int GetBroadCastDeactiveCode() {
		return _broadcastCodeDeactive;
	}

	public Intent GetIntent() {
		Intent intent = new Intent(_context, SocketActionReceiver.class);

		Bundle data = new Bundle();
		data.putSerializable(Bundles.SOCKET_DATA, _socket);
		intent.putExtras(data);

		return intent;
	}

	public PendingIntent GetPendingIntent() {
		PendingIntent pendingIntent;

		if (_socket.GetIsActivated()) {
			pendingIntent = PendingIntent.getBroadcast(_context, _broadcastCodeActive, GetIntent(),
					PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			pendingIntent = PendingIntent.getBroadcast(_context, _broadcastCodeDeactive, GetIntent(),
					PendingIntent.FLAG_UPDATE_CURRENT);
		}

		return pendingIntent;
	}

	public NotificationCompat.Action GetWearableAction() {
		NotificationCompat.Action action;

		if (_socket.GetIsActivated()) {
			action = new NotificationCompat.Action.Builder(_iconActive, _textActive, GetPendingIntent()).build();
		} else {
			action = new NotificationCompat.Action.Builder(_iconDeactive, _textDeactive, GetPendingIntent()).build();
		}

		return action;
	}

	public String toString() {
		return "{NotificationDetails: {_iconActive: " + String.valueOf(_iconActive) + "};{_iconDeactive: "
				+ String.valueOf(_iconDeactive) + "};{_textActive: " + _textActive + "};{_textDeactive: "
				+ _textDeactive + "};{_socket: " + _socket.GetName() + "};{_iconIsVisible: "
				+ String.valueOf(_iconIsVisible) + "};{_broadcastCodeActive: " + String.valueOf(_broadcastCodeActive)
				+ "};{_broadcastCodeDeactive: " + String.valueOf(_broadcastCodeDeactive) + "}}";
	}
}
