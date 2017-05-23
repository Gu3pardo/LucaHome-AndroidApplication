package guepardoapps.lucahome.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.AndroidSystemController;
import guepardoapps.library.toolset.controller.ReceiverController;

public class ControlServiceStateService extends Service {

    private static final String TAG = ControlServiceStateService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;

    private Context _context;
    private AndroidSystemController _androidSystemController;
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!_isInitialized) {
            _logger = new LucaHomeLogger(TAG);

            _context = this;
            _androidSystemController = new AndroidSystemController(_context);
            _receiverController = new ReceiverController(_context);
            _receiverController.RegisterReceiver(_timeTickReceiver, new String[]{Intent.ACTION_TIME_TICK});

            _checkServiceHandler = new Handler();
            _checkServicesRunning.run();

            _isInitialized = true;
        }

        _logger.Debug("onStartCommand");

        return Service.START_STICKY;
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

    private class CustomRunnable implements Runnable {
        private void runSingle() {
            _logger.Debug("CustomRunnable runSingle");

            checkServices();
        }

        public void run() {
            _logger.Debug("CustomRunnable run");

            checkServices();

            _checkServiceHandler.postDelayed(_checkServicesRunning, Timeouts.CONTROL_SERVICES);
        }

        private void checkServices() {
            _logger.Debug("CustomRunnable checkServices");

            if (!_androidSystemController.IsServiceRunning(MainService.class)) {
                _logger.Warn("MainService not running! Restarting!");
                Toasty.warning(_context, "Restarting MainService!", Toast.LENGTH_LONG).show();
                Intent serviceIntent = new Intent(_context, MainService.class);
                startService(serviceIntent);
            }
        }
    }
}
