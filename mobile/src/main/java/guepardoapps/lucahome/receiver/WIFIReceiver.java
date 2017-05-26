package guepardoapps.lucahome.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.lucahome.common.dto.ActionDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.DatabaseController;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.controller.ServiceController;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.AndroidSystemController;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.DialogController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.SharedPrefController;
import guepardoapps.library.toolset.scheduler.ScheduleService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.services.MainService;

public class WIFIReceiver extends BroadcastReceiver {

    private static final String TAG = WIFIReceiver.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String WIFI = "Wifi:";

    private Context _context;

    private LucaNotificationController _notificationController;
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

        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

            if (!(SupplicantState.isValidState(state) && state == SupplicantState.COMPLETED)) {
                _logger.Warn("Not yet a valid connection!");
                return;
            }
        }

        _logger.Info("valid connection!");

        _context = context;
        _checkConnectionEnabled = true;

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

        if (new NetworkController(
                _context,
                new DialogController(
                        _context,
                        ContextCompat.getColor(_context, R.color.TextIcon),
                        ContextCompat.getColor(_context, R.color.Background)))
                .IsHomeNetwork(Constants.LUCAHOME_SSID)) {
            _logger.Debug("We are in the homeNetwork!");

            if (new AndroidSystemController(_context).IsServiceRunning(MainService.class)) {
                new BroadcastController(_context).SendSerializableArrayBroadcast(
                        Broadcasts.MAIN_SERVICE_COMMAND,
                        new String[]{Bundles.MAIN_SERVICE_ACTION},
                        new Object[]{MainServiceAction.DOWNLOAD_ALL});
            } else {
                Intent startMainService = new Intent(_context, MainService.class);
                Bundle mainServiceBundle = new Bundle();
                mainServiceBundle.putSerializable(Bundles.MAIN_SERVICE_ACTION, MainServiceAction.BOOT);
                startMainService.putExtras(mainServiceBundle);
                _context.startService(startMainService);
            }

            _serviceController.SendMessageToWear(WIFI + "HOME");

            // check if actions are stored to perform after entering home wifi
            checkDatabase();

            // check if flag is enabled to display the sleep notification
            checkSleepNotificationFlag();
        } else {
            _logger.Warn("We are NOT in the homeNetwork!");

            _notificationController.CloseNotification(IDs.NOTIFICATION_WEAR);
            _serviceController.SendMessageToWear(WIFI + "NO");

            if (_checkConnectionEnabled) {
                Handler checkConnectionHandler = new Handler();
                checkConnectionHandler.postDelayed(_checkConnectionRunnable, Timeouts.CHECK_WIFI_CONNECTION);
            }
        }
    }

    private void checkDatabase() {
        _logger.Debug("checkDatabase");
        DatabaseController databaseController = DatabaseController.getInstance();

        SerializableList<ActionDto> storedActions = databaseController.GetActions();
        if (storedActions.getSize() > 0) {
            for (int index = 0; index < storedActions.getSize(); index++) {
                ActionDto entry = storedActions.getValue(index);
                databaseController.DeleteAction(entry);
                ScheduleService.getInstance().DeleteSchedule(entry.GetName());
                _serviceController.StartRestService(entry.GetName(), entry.GetAction(), entry.GetBroadcast(),
                        LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
            }
        } else {
            _logger.Debug("No actions stored!");
        }
        databaseController.Dispose();
    }

    private void checkSleepNotificationFlag() {
        SharedPrefController sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
        if (sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE)) {
            _notificationController.CreateSleepHeatingNotification();
            sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, false);
        }
    }
}