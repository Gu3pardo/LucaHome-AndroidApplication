package guepardoapps.library.lucahome.services.sockets;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.controller.SocketController;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

public class SocketActionService extends Service {

    private static final String TAG = SocketActionService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private BroadcastReceiver _notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_notificationReceiver onReceive");
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_SOCKETS);
            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("Received new socket change!");

        _broadcastController = new BroadcastController(this);
        _receiverController = new ReceiverController(this);
        ServiceController serviceController = new ServiceController(this);
        SocketController socketController = new SocketController(this);

        WirelessSocketDto socket = (WirelessSocketDto) intent.getExtras().getSerializable(Bundles.SOCKET_DATA);
        if (socket != null) {
            _logger.Debug(String.format(Locale.getDefault(), "socket: %s", socket));

            _receiverController.RegisterReceiver(_notificationReceiver, new String[]{socket.GetNotificationBroadcast()});
            serviceController.StartRestService(
                    socket.GetName(),
                    socket.GetCommandSet(!socket.IsActivated()),
                    socket.GetNotificationBroadcast());
            socketController.CheckMedia(socket);
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if (_logger != null) {
            _logger.Debug("onBind");
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (_logger != null) {
            _logger.Debug("onCreate");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_logger != null) {
            _logger.Debug("onDestroy");
        }
        _receiverController.UnregisterReceiver(_notificationReceiver);
    }
}
