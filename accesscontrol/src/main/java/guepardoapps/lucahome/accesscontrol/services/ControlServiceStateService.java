package guepardoapps.lucahome.accesscontrol.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.common.controller.AndroidSystemController;

public class ControlServiceStateService extends Service {
    private static final int TimeoutCheckMainService = 30 * 1000;

    private Context _context;
    private AndroidSystemController _androidSystemController;

    private boolean _isInitialized;
    private Handler _checkApplicationHandler = new Handler();

    private Runnable _checkApplication = new Runnable() {
        public void run() {
            if (!_androidSystemController.IsServiceRunning(MainService.class)) {
                String message = String.format("%s not running! Restarting!", MainService.class.getSimpleName());
                Toasty.warning(_context, message, Toast.LENGTH_LONG).show();

                Intent serviceIntent = new Intent(_context, MainService.class);
                startService(serviceIntent);
            }

            if (!_androidSystemController.IsBaseActivityRunning()) {
                String message = String.format("%s not running! Restarting!", MainService.class.getSimpleName());
                Toasty.error(_context, message, Toast.LENGTH_LONG).show();

                Intent activityIntent = new Intent(_context, MainService.class);
                startActivity(activityIntent);
            }

            _checkApplicationHandler.postDelayed(_checkApplication, TimeoutCheckMainService);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!_isInitialized) {
            _context = this;
            _androidSystemController = new AndroidSystemController(_context);
            _isInitialized = true;
            _checkApplication.run();
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _isInitialized = false;
    }
}
