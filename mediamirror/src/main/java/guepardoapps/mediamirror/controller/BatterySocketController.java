package guepardoapps.mediamirror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.mediamirror.common.constants.RaspPiConstants;

public class BatterySocketController {
    private static final String TAG = BatterySocketController.class.getSimpleName();
    private Logger _logger;

    public static final int UPPER_BATTERY_LIMIT = 90;
    public static final int LOWER_BATTERY_LIMIT = 10;

    private boolean _isInitialized;
    private String _wirelessSocketName;

    private Context _context;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private WirelessSocketService _wirelessSocketService;

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            _logger.Debug(String.format(Locale.GERMAN, "Received new battery level %d", level));
            if (level > UPPER_BATTERY_LIMIT) {
                disableBatterySocket();
            } else if (level < LOWER_BATTERY_LIMIT) {
                enableBatterySocket();
            }
        }
    };

    public BatterySocketController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _logger.Information(TAG + " created");

        _context = context;

        _receiverController = new ReceiverController(_context);
        _networkController = new NetworkController(_context);

        _wirelessSocketService = WirelessSocketService.getInstance();
        _wirelessSocketService.Initialize(_context, null, false, true, 15);

        searchWirelessSocketName();
    }

    public void Start() {
        _logger.Debug("Start");
        if (!_isInitialized) {
            _logger.Debug("Initializing!");
            _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
            _isInitialized = true;
        } else {
            _logger.Warning("Is ALREADY initialized!");
        }
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.UnregisterReceiver(_batteryInfoReceiver);
        _isInitialized = false;
    }

    private void enableBatterySocket() {
        if (isSocketActive()) {
            _logger.Warning("Already activated socket!");
            return;
        }

        _logger.Debug("enableBatterySocket");
        setBatterySocket(true);
    }

    private void disableBatterySocket() {
        if (!isSocketActive()) {
            _logger.Warning("Already deactivated socket!");
            return;
        }

        _logger.Debug("disableBatterySocket");
        setBatterySocket(false);
    }

    private void setBatterySocket(boolean enable) {
        searchWirelessSocketName();

        if (_wirelessSocketName != null) {
            _logger.Debug(String.format(Locale.GERMAN, "setBatterySocket %s to %s", _wirelessSocketName, ((enable) ? RaspPiConstants.SOCKET_STATE_ON : RaspPiConstants.SOCKET_STATE_OFF)));
            _wirelessSocketService.SetWirelessSocketState(_wirelessSocketName, enable);

            Toasty.success(_context,
                    String.format("Set socket %s to %s", _wirelessSocketName, enable ? "ON" : "OFF"),
                    Toast.LENGTH_LONG).show();
        } else {
            _logger.Error("Did not found socket!");
            Toasty.error(_context, "Did not found socket!", Toast.LENGTH_LONG).show();
        }
    }

    private void searchWirelessSocketName() {
        if (_wirelessSocketName != null) {
            _logger.Information("_wirelessSocketName is not null!");
            return;
        }

        try {
            String localIp = _networkController.GetIpAddress().replace("SiteLocalAddress: ", "").replace("\n", "");
            MediaServerSelection mediaServerSelection = MediaServerSelection.GetByIp(localIp);
            _logger.Debug(String.format("MediaServerSelection is %s", mediaServerSelection));

            _wirelessSocketName = mediaServerSelection.GetSocket();
        } catch (Exception exception) {
            _logger.Error(exception.getMessage());
        }
    }

    private boolean isSocketActive() {
        searchWirelessSocketName();

        if (_wirelessSocketName == null) {
            _logger.Error("_wirelessSocketName is null!");
            return false;
        }

        return _wirelessSocketService.GetWirelessSocketState(_wirelessSocketName);
    }
}
