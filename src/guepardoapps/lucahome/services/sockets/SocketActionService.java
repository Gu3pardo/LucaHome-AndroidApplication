package guepardoapps.lucahome.services.sockets;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.view.controller.SocketController;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class SocketActionService extends Service {

	private static final String TAG = SocketActionService.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;

	private BroadcastController _broadcastController;
	private ReceiverController _receiverController;
	private ServiceController _serviceController;
	private SocketController _socketController;

	private BroadcastReceiver _notificationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_notificationReceiver onReceive");
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
					new String[] { Bundles.MAIN_SERVICE_ACTION }, new Object[] { MainServiceAction.DOWLOAD_SOCKETS });
			stopSelf();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("Received new socket change!");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_receiverController = new ReceiverController(_context);
		_serviceController = new ServiceController(_context);
		_socketController = new SocketController(_context);

		WirelessSocketDto socket = (WirelessSocketDto) intent.getExtras().getSerializable(Bundles.SOCKET_DATA);
		_logger.Debug("socket: " + socket.toString());

		_receiverController.RegisterReceiver(_notificationReceiver, new String[] { socket.GetNotificationBroadcast() });
		_serviceController.StartRestService(socket.GetName(), socket.GetCommandSet(!socket.GetIsActivated()),
				socket.GetNotificationBroadcast(), LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
		_socketController.CheckMedia(socket);

		return 0;
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
