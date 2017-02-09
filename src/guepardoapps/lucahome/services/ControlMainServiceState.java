package guepardoapps.lucahome.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class ControlMainServiceState extends Service {

	private static final int CHECK_TIMEOUT = 5 * 60 * 1000;

	private static final String TAG = ControlMainServiceState.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
	private int _errorCount;

	private Context _context;

	private Handler _checkMainServiceHandler;

	private Runnable _checkMainService = new Runnable() {
		public void run() {
			_logger.Debug("_checkMainService");

			if (!isMainServiceRunning(MainService.class)) {
				_logger.Warn("MainService not running! Restarting!");
				_errorCount++;
				_logger.Warn("_errorCount: " + String.valueOf(_errorCount));
				if (_errorCount >= 5) {
					// TODO: send warning mail to me!
					_logger.Info("TODO: send warning mail to me!");
				} else {
					Intent serviceIntent = new Intent(_context, MainService.class);
					startService(serviceIntent);
				}
			} else {
				_errorCount = 0;
			}

			_checkMainServiceHandler.postDelayed(_checkMainService, CHECK_TIMEOUT);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new LucaHomeLogger(TAG);

			_isInitialized = true;
			_errorCount = 0;

			_context = this;

			_checkMainServiceHandler = new Handler();
			_checkMainService.run();
		}

		_logger.Debug("onStartCommand");

		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private boolean isMainServiceRunning(Class<?> serviceClass) {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
