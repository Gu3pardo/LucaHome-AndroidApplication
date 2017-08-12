package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Handler;

import guepardoapps.lucahome.basic.controller.AndroidSystemController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.data.controller.SettingsController;
import guepardoapps.lucahome.data.service.WirelessSocketService;
import guepardoapps.lucahome.service.MainService;

public class WIFIReceiver extends BroadcastReceiver {
    private static final String TAG = WIFIReceiver.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private AndroidSystemController _androidSystemController;
    private NetworkController _networkController;
    private SettingsController _settingsController;

    private WirelessSocketService _wirelessSocketService;

    private boolean _checkConnectionEnabled;
    private Runnable _checkConnectionRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Information("_checkConnectionRunnable run");
            _checkConnectionEnabled = false;
            checkConnection();
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        _logger = new Logger(TAG);
        _logger.Debug("WIFIReceiver onReceive.\nContext is " + context.toString());

        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

            if (!(SupplicantState.isValidState(state) && state == SupplicantState.COMPLETED)) {
                _logger.Warning("Not yet a valid connection!");
                return;
            }
        }
        _logger.Information("valid connection!");

        _context = context;

        _androidSystemController = new AndroidSystemController(_context);
        _networkController = new NetworkController(_context);
        _settingsController = SettingsController.getInstance();

        _wirelessSocketService = WirelessSocketService.getInstance();

        _checkConnectionEnabled = true;

        checkConnection();
    }

    private void checkConnection() {
        _logger.Debug("checkConnection");

        if (_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _logger.Debug("We are in the homeNetwork!");

            if (!_androidSystemController.IsServiceRunning(MainService.class)) {
                _context.startService(new Intent(_context, MainService.class));
            }

            // check if actions are stored to perform after entering home wifi
            checkSavedActionsDatabase();
        } else {
            _logger.Warning("We are NOT in the homeNetwork!");

            _wirelessSocketService.CloseNotification();

            if (_checkConnectionEnabled) {
                Handler checkConnectionHandler = new Handler();
                checkConnectionHandler.postDelayed(_checkConnectionRunnable, 3000);
            }
        }
    }

    private void checkSavedActionsDatabase() {
        _logger.Debug("checkSavedActionsDatabase");
        /*TODO*/
    }
}