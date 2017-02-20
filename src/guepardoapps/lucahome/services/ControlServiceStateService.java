package guepardoapps.lucahome.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;

public class ControlServiceStateService extends Service {

	private static final int CHECK_TIMEOUT = 5 * 60 * 1000;

	private static final String TAG = ControlServiceStateService.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;

	private Context _context;

	private Handler _checkMainServiceHandler;

	private Runnable _checkMainService = new Runnable() {
		public void run() {
			_logger.Debug("_checkMainService");

			if (!isServiceRunning(MainService.class)) {
				_logger.Warn("MainService not running! Restarting!");
				Toasty.warning(_context, "Restarting MainService!", Toast.LENGTH_LONG).show();
				Intent serviceIntent = new Intent(_context, MainService.class);
				startService(serviceIntent);
			}

			if (!isServiceRunning(ReceiverService.class)) {
				_logger.Warn("ReceiverService not running! Restarting!");
				Toasty.warning(_context, "Restarting ReceiverService!", Toast.LENGTH_LONG).show();
				Intent serviceIntent = new Intent(_context, ReceiverService.class);
				startService(serviceIntent);
			}

			_checkMainServiceHandler.postDelayed(_checkMainService, CHECK_TIMEOUT);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new LucaHomeLogger(TAG);

			_isInitialized = true;

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

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
