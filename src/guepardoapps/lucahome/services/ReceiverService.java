package guepardoapps.lucahome.services;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class ReceiverService extends Service {

	private static final String TAG = ReceiverService.class.getName();
	private LucaHomeLogger _logger;

	private static final int NOTIFICATION_HOUR = 21;
	private static final int NOTIFICATION_MINUTE = 30;

	private Context _context;
	private ServiceController _serviceController;

	private boolean _isInitialized;

	private BroadcastReceiver _timeTickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			_logger.Debug("action: " + action);

			if (action.equals(Intent.ACTION_TIME_TICK)) {
				Calendar calendar = Calendar.getInstance();
				if (calendar.get(Calendar.HOUR_OF_DAY) == NOTIFICATION_HOUR) {
					if (calendar.get(Calendar.MINUTE) == NOTIFICATION_MINUTE) {
						_logger.Debug("Showing notification go to sleep!");
						_serviceController.StartNotificationService("", "", -1, LucaObject.GO_TO_BED);
					} else {
						_logger.Debug("invalid minute!");
					}
				} else {
					_logger.Debug("invalid hour!");
				}
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new LucaHomeLogger(TAG);
			_context = this;

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
