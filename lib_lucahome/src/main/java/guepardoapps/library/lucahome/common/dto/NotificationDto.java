package guepardoapps.library.lucahome.common.dto;

import java.io.Serializable;
import java.util.Locale;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.receiver.notifications.SocketActionReceiver;

public class NotificationDto implements Serializable {

    private static final long serialVersionUID = 5583702640190370633L;

    private static final String TAG = NotificationDto.class.getSimpleName();

    private int _iconActive;
    private int _iconDeActive;

    private String _textActive;
    private String _textDeActive;

    private WirelessSocketDto _socket;

    private boolean _iconIsVisible;

    private int _broadcastCodeActive;
    private int _broadcastCodeDeActive;

    private Context _context;

    public NotificationDto(
            int iconActive,
            int iconDeActive,
            @NonNull String textActive,
            @NonNull String textDeActive,
            @NonNull WirelessSocketDto socket,
            boolean iconIsVisible,
            int broadcastActiveCode,
            int broadcastDeActiveCode,
            @NonNull Context context) {
        _iconActive = iconActive;
        _iconDeActive = iconDeActive;
        _textActive = textActive;
        _textDeActive = textDeActive;

        _socket = socket;

        _iconIsVisible = iconIsVisible;

        _broadcastCodeActive = broadcastActiveCode;
        _broadcastCodeDeActive = broadcastDeActiveCode;

        _context = context;
    }

    public int GetIconActive() {
        return _iconActive;
    }

    public int GetIconDeActive() {
        return _iconDeActive;
    }

    public String GetTextActive() {
        return _textActive;
    }

    public String GetTextDeActive() {
        return _textDeActive;
    }

    public WirelessSocketDto GetSocket() {
        return _socket;
    }

    public boolean GetCurrentSocketState() {
        return _socket.IsActivated();
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

    public int GetBroadCastDeActiveCode() {
        return _broadcastCodeDeActive;
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

        if (_socket.IsActivated()) {
            pendingIntent = PendingIntent.getBroadcast(_context, _broadcastCodeActive, GetIntent(),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getBroadcast(_context, _broadcastCodeDeActive, GetIntent(),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return pendingIntent;
    }

    public NotificationCompat.Action GetWearableAction() {
        NotificationCompat.Action action;

        if (_socket.IsActivated()) {
            action = new NotificationCompat.Action.Builder(_iconActive, _textActive, GetPendingIntent()).build();
        } else {
            action = new NotificationCompat.Action.Builder(_iconDeActive, _textDeActive, GetPendingIntent()).build();
        }

        return action;
    }

    public String toString() {
        return String.format(Locale.getDefault(),
                "{%s: {IconActive: %d};{IconDeActive: %d};{TextActive: %s};{TextDeActive: %s};{Socket: %s};{IsVisible: %s};{BroadcastCodeActive: %s};{BroadcastCodeActive: %s}}",
                TAG,
                _iconActive,
                _iconDeActive,
                _textActive,
                _textDeActive,
                _socket.GetName(),
                _iconIsVisible,
                _broadcastCodeActive,
                _broadcastCodeDeActive);
    }
}
