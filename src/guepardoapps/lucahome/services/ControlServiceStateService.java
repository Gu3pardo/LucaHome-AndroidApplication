package guepardoapps.lucahome.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;
import guepardoapps.toolset.controller.ReceiverController;

public class ControlServiceStateService extends Service {

	private static final int CHECK_TIMEOUT = 5 * 60 * 1000;

	private static final String TAG = ControlServiceStateService.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;

	private Context _context;
	private ReceiverController _receiverController;

	private Handler _checkServiceHandler;
	private CustomRunnable _checkServicesRunning = new CustomRunnable();

	private BroadcastReceiver _timeTickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_timeTickReceiver onReceive");
			_checkServicesRunning.runSingle();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (!_isInitialized) {
			_logger = new LucaHomeLogger(TAG);

			_context = this;
			_receiverController = new ReceiverController(_context);
			_receiverController.RegisterReceiver(_timeTickReceiver, new String[] { Intent.ACTION_TIME_TICK });

			_checkServiceHandler = new Handler();
			_checkServicesRunning.run();

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
		super.onDestroy();
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_timeTickReceiver);
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

	private class CustomRunnable implements Runnable {
		public void runSingle() {
			_logger.Debug("CustomRunnable runSingle");

			checkServices();
		};

		public void run() {
			_logger.Debug("CustomRunnable run");

			checkServices();

			_checkServiceHandler.postDelayed(_checkServicesRunning, CHECK_TIMEOUT);
		}

		private void checkServices() {
			_logger.Debug("CustomRunnable checkServices");

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
		}
	}
}
