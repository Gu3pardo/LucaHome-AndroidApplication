package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.constants.IDs;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.services.MainService;

import guepardoapps.toolset.controller.DialogController;
import guepardoapps.toolset.controller.NetworkController;
import guepardoapps.toolset.services.AndroidSystemService;

import guepardoapps.toolset.controller.BroadcastController;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = BootReceiver.class.getName();
	private LucaHomeLogger _logger;

	private static final String WIFI = "Wifi:";

	private static final int CHECK_CONNETION_TIMEOUT = 15 * 1000;

	private Context _context;

	private AndroidSystemService _androidSystemService;
	private BroadcastController _broadcastController;
	private DialogController _dialogController;
	private NetworkController _networkController;
	private ServiceController _serviceController;

	private boolean _checkConnectionEnabled;
	private Runnable _checkConnectionRunnable = new Runnable() {
		@Override
		public void run() {
			_logger.Info("_checkConnectionRunnable run");
			_checkConnectionEnabled = false;
			checkConnection();
		}
	};

	@Override
	public void onReceive(Context context, Intent intent) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Debug("WIFIReceiver onReceive");
		_logger.Info("Context is " + context.toString());

		_context = context;
		_checkConnectionEnabled = true;

		int textColor = ContextCompat.getColor(_context, R.color.TextIcon);
		int backgroundColor = ContextCompat.getColor(_context, R.color.Background);

		if (_androidSystemService == null) {
			_androidSystemService = new AndroidSystemService(_context);
		}
		if (_broadcastController == null) {
			_broadcastController = new BroadcastController(_context);
		}
		if (_dialogController == null) {
			_dialogController = new DialogController(_context, textColor, backgroundColor);
		}
		if (_networkController == null) {
			_networkController = new NetworkController(_context, _dialogController);
		}
		if (_serviceController == null) {
			_serviceController = new ServiceController(_context);
		}

		checkConnection();
	}

	private void checkConnection() {
		_logger.Debug("checkConnection");
		if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
			_logger.Debug("We are in the homenetwork!");
			if (_androidSystemService.IsServiceRunning(MainService.class)) {
				_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
						new String[] { Constants.BUNDLE_MAIN_SERVICE_ACTION },
						new Object[] { MainServiceAction.DOWNLOAD_ALL });
			} else {
				Intent startMainService = new Intent(_context, MainService.class);
				Bundle mainServiceBundle = new Bundle();
				mainServiceBundle.putSerializable(Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.BOOT);
				startMainService.putExtras(mainServiceBundle);
				_context.startService(startMainService);
			}
			_serviceController.SendMessageToWear(WIFI + "HOME");
		} else {
			_logger.Warn("We are NOT in the homenetwork!");

			_serviceController.CloseNotification(IDs.NOTIFICATION_TEMPERATURE);
			_serviceController.CloseNotification(IDs.NOTIFICATION_WEAR);
			_serviceController.SendMessageToWear(WIFI + "NO");

			if (_checkConnectionEnabled) {
				Handler checkConnectionHandler = new Handler();
				checkConnectionHandler.postDelayed(_checkConnectionRunnable, CHECK_CONNETION_TIMEOUT);
			}
		}
	}
}