package guepardoapps.lucahome.services;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.toolset.controller.NetworkController;
import guepardoapps.toolset.controller.SharedPrefController;

public class ReceiverService extends Service {

	private static final String TAG = ReceiverService.class.getName();
	private LucaHomeLogger _logger;

	private static final int NOTIFICATION_HOUR = 21;
	private static final int NOTIFICATION_MINUTE = 30;

	private static final int CLEAR_NOTIFICATION_DISPLAY_HOUR = 2;
	private static final int CLEAR_NOTIFICATION_DISPLAY_MINUTE = 0;

	private Context _context;

	private NetworkController _networkController;
	private SharedPrefController _sharedPrefController;
	private ServiceController _serviceController;

	private boolean _isInitialized;

	// Receiver used for receiving the current time and check if time is ready
	// to display sleep notification or set a flag if not or to delete this flag
	private BroadcastReceiver _timeTickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			_logger.Debug("action: " + action);

			if (action.equals(Intent.ACTION_TIME_TICK)) {
				Calendar calendar = Calendar.getInstance();

				// Check, if the time is ready for displaying the notification
				// time needs to be NOTIFICATION_HOUR:NOTIFICATION_MINUTE
				if (calendar.get(Calendar.HOUR_OF_DAY) == NOTIFICATION_HOUR) {
					if (calendar.get(Calendar.MINUTE) == NOTIFICATION_MINUTE) {

						// Check also if we are in the home network, otherwise
						// it's senseless to display the notification, but we
						// set a flag to display notification later
						if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
							_logger.Debug("Showing notification go to sleep!");
							_serviceController.StartNotificationService("", "", -1, LucaObject.GO_TO_BED);
						} else {
							_sharedPrefController
									.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, true);
						}
					}
				}
				// Check if time is ready to delete the flag to display the
				// sleep notification
				else if (calendar.get(Calendar.HOUR_OF_DAY) == CLEAR_NOTIFICATION_DISPLAY_HOUR) {
					if (calendar.get(Calendar.MINUTE) == CLEAR_NOTIFICATION_DISPLAY_MINUTE) {
						if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(
								SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE)) {
							_sharedPrefController
									.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, false);
						}
					}
				}
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new LucaHomeLogger(TAG);
			_context = this;

			_networkController = new NetworkController(_context, null);
			_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
			_serviceController = new ServiceController(_context);

			IntentFilter intentFilterTime = new IntentFilter(Intent.ACTION_TIME_TICK);
			registerReceiver(_timeTickReceiver, intentFilterTime);

			_isInitialized = true;
		}

		_logger.Debug("onStartCommand");

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(_timeTickReceiver);
		_isInitialized = false;
	}
}
