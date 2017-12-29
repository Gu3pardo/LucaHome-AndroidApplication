package guepardoapps.mediamirror.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import guepardoapps.lucahome.basic.controller.AndroidSystemController;
import guepardoapps.lucahome.basic.utils.Logger;

public class ControlServiceStateService extends Service {
    private static final String TAG = ControlServiceStateService.class.getSimpleName();

    private static final int TIMEOUT_CHECK = 60 * 1000;

    private boolean _isInitialized;

    private Context _context;
    private AndroidSystemController _systemController;

    private Handler _checkServicesHandler;

    private Runnable _checkServices = new Runnable() {
        public void run() {
            if (!_systemController.IsServiceRunning(MainService.class)) {
                Logger.getInstance().Warning(TAG, "MainService not running! Restarting!");
                startService(new Intent(_context, MainService.class));
            }
            _checkServicesHandler.postDelayed(_checkServices, TIMEOUT_CHECK);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!_isInitialized) {
            _isInitialized = true;
            _context = this;
            _systemController = new AndroidSystemController(_context);
            _checkServicesHandler = new Handler();
            _checkServices.run();
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
