package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.services.MainService;

import guepardoapps.toolset.controller.DialogController;
import guepardoapps.toolset.controller.NetworkController;
import guepardoapps.toolset.services.AndroidSystemService;

import guepardoapps.toolset.controller.BroadcastController;

public class WIFIReceiver extends BroadcastReceiver {

	private static final String TAG = WIFIReceiver.class.getName();
	private LucaHomeLogger _logger;

	private static final String WIFI = "Wifi:";

	private AndroidSystemService _androidSystemService;
	private BroadcastController _broadcastController;
	private DialogController _dialogController;
	private NetworkController _networkController;
	private ServiceController _serviceController;

	@Override
	public void onReceive(Context context, Intent intent) {
		_logger = new LucaHomeLogger(TAG);

		int textColor = ContextCompat.getColor(context, R.color.TextIcon);
		int backgroundColor = ContextCompat.getColor(context, R.color.Background);

		if (_androidSystemService == null) {
			_androidSystemService = new AndroidSystemService(context);
		}
		if (_broadcastController == null) {
			_broadcastController = new BroadcastController(context);
		}
		if (_dialogController == null) {
			_dialogController = new DialogController(context, textColor, backgroundColor);
		}
		if (_networkController == null) {
			_networkController = new NetworkController(context, _dialogController);
		}
		if (_serviceController == null) {
			_serviceController = new ServiceController(context);
		}

		if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
			_logger.Debug("We are in the homenetwork!");
			if (_androidSystemService.IsServiceRunning(MainService.class)) {
				_broadcastController.SendSerializableArrayBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
						new String[] { Constants.BUNDLE_MAIN_SERVICE_ACTION },
						new Object[] { MainServiceAction.DOWNLOAD_ALL });
			} else {
				Intent startMainService = new Intent(context, MainService.class);
				Bundle mainServiceBundle = new Bundle();
				mainServiceBundle.putSerializable(Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.BOOT);
				startMainService.putExtras(mainServiceBundle);
				context.startService(startMainService);
			}
			_serviceController.SendMessageToWear(WIFI + "HOME");
		} else {
			_logger.Warn("We are NOT in the homenetwork!");
			_serviceController.CloseNotification(Constants.ID_NOTIFICATION_TEMPERATURE);
			_serviceController.CloseNotification(Constants.ID_NOTIFICATION_WEAR);
			_serviceController.SendMessageToWear(WIFI + "NO");
		}
	}
}