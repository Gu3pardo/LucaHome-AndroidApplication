package guepardoapps.lucahome.mediaserver.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import guepardoapps.lucahome.common.controller.AndroidSystemController;
import guepardoapps.lucahome.common.utils.Logger;

public class ControlServiceStateService extends Service {
    private static final String Tag = ControlServiceStateService.class.getSimpleName();
    private static final int TimeoutCheck = 60 * 1000;

    private Context _context;
    private AndroidSystemController _androidSystemController;
    private boolean _isInitialized;
    private Handler _checkServicesHandler;
    private Runnable _checkServices = new Runnable() {
        public void run() {
            if (!_androidSystemController.IsServiceRunning(MainService.class)) {
                Logger.getInstance().Warning(Tag, "MainService not running! Restarting!");
                startService(new Intent(_context, MainService.class));
            }
            _checkServicesHandler.postDelayed(_checkServices, TimeoutCheck);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!_isInitialized) {
            _isInitialized = true;
            _context = this;
            _androidSystemController = new AndroidSystemController(_context);
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
