package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.ServiceController;

import guepardoapps.library.toolset.controller.AndroidSystemController;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.DialogController;
import guepardoapps.library.toolset.controller.NetworkController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.services.MainService;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = BootReceiver.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String WIFI = "Wifi:";

    private Context _context;

    private AndroidSystemController _androidSystemController;
    private BroadcastController _broadcastController;
    private DialogController _dialogController;
    private LucaNotificationController _notificationController;
    private NetworkController _networkController;
    private ServiceController _serviceController;

    private boolean _checkConnectionEnabled;
    private Runnable _checkConnectionRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Info("_checkConnectionRunnable run");
            _checkConnectionEnabled = false;
            checkConnection();
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Debug("WIFIReceiver onReceive");
        _logger.Info("Context is " + context.toString());

        _context = context;
        _checkConnectionEnabled = true;

        int textColor = ContextCompat.getColor(_context, R.color.TextIcon);
        int backgroundColor = ContextCompat.getColor(_context, R.color.Background);

        if (_androidSystemController == null) {
            _androidSystemController = new AndroidSystemController(_context);
        }
        if (_broadcastController == null) {
            _broadcastController = new BroadcastController(_context);
        }
        if (_dialogController == null) {
            _dialogController = new DialogController(_context, textColor, backgroundColor);
        }
        if (_networkController == null) {
            _networkController = new NetworkController(_context, _dialogController);
        }
        if (_notificationController == null) {
            _notificationController = new LucaNotificationController(_context);
        }
        if (_serviceController == null) {
            _serviceController = new ServiceController(_context);
        }

        checkConnection();
    }

    private void checkConnection() {
        _logger.Debug("checkConnection");
        if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
            _logger.Debug("We are in the homeNetwork!");
            if (_androidSystemController.IsServiceRunning(MainService.class)) {
                _broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                        new String[]{Bundles.MAIN_SERVICE_ACTION}, new Object[]{MainServiceAction.DOWNLOAD_ALL});
            } else {
                Intent startMainService = new Intent(_context, MainService.class);
                Bundle mainServiceBundle = new Bundle();
                mainServiceBundle.putSerializable(Bundles.MAIN_SERVICE_ACTION, MainServiceAction.BOOT);
                startMainService.putExtras(mainServiceBundle);
                _context.startService(startMainService);
            }
            _serviceController.SendMessageToWear(WIFI + "HOME");
        } else {
            _logger.Warn("We are NOT in the homeNetwork!");

            _notificationController.CloseNotification(IDs.NOTIFICATION_TEMPERATURE);
            _notificationController.CloseNotification(IDs.NOTIFICATION_WEAR);
            _serviceController.SendMessageToWear(WIFI + "NO");

            if (_checkConnectionEnabled) {
                Handler checkConnectionHandler = new Handler();
                checkConnectionHandler.postDelayed(_checkConnectionRunnable, Timeouts.CHECK_WIFI_CONNECTION);
            }
        }
    }
}