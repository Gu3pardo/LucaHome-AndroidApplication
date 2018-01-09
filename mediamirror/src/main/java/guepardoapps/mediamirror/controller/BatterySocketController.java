package guepardoapps.mediamirror.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.mediamirror.receiver.WirelessSocketActionReceiver;

@SuppressWarnings("WeakerAccess")
public class BatterySocketController {
    private static final String TAG = BatterySocketController.class.getSimpleName();

    public static final int UPPER_BATTERY_LIMIT = 90;
    public static final int LOWER_BATTERY_LIMIT = 15;

    private boolean _isInitialized;
    private String _wirelessSocketName;

    private Context _context;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level > UPPER_BATTERY_LIMIT) {
                disableBatterySocket();
            } else if (level < LOWER_BATTERY_LIMIT) {
                enableBatterySocket();
            }
        }
    };

    public BatterySocketController(@NonNull Context context) {
        _context = context;

        _receiverController = new ReceiverController(_context);
        _networkController = new NetworkController(_context);

        WirelessSocketService.getInstance().Initialize(_context, WirelessSocketActionReceiver.class, false, true, 15);

        searchWirelessSocketName();
    }

    public void Start() {
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
            _isInitialized = true;
        } else {
            Logger.getInstance().Warning(TAG, "Is ALREADY initialized!");
        }
    }

    public void Dispose() {
        _receiverController.Dispose();
        _isInitialized = false;
    }

    private void enableBatterySocket() {
        if (isSocketActive()) {
            Logger.getInstance().Warning(TAG, "Already activated socket!");
            return;
        }

        setBatterySocket(true);
    }

    private void disableBatterySocket() {
        if (!isSocketActive()) {
            Logger.getInstance().Warning(TAG, "Already deactivated socket!");
            return;
        }

        setBatterySocket(false);
    }

    private void setBatterySocket(boolean enable) {
        searchWirelessSocketName();

        if (_wirelessSocketName != null) {
            WirelessSocketService.getInstance().SetWirelessSocketState(_wirelessSocketName, enable);

            Toasty.success(_context,
                    String.format("Set socket %s to %s", _wirelessSocketName, enable ? "ON" : "OFF"),
                    Toast.LENGTH_LONG).show();
        } else {
            Logger.getInstance().Error(TAG, "Did not found socket!");
            Toasty.error(_context, "Did not found socket!", Toast.LENGTH_LONG).show();
        }
    }

    private void searchWirelessSocketName() {
        if (_wirelessSocketName != null) {
            Logger.getInstance().Information(TAG, "_wirelessSocketName is not null!");
            return;
        }

        try {
            String localIp = _networkController.GetIpAddress().replace("SiteLocalAddress: ", "").replace("\n", "");
            MediaServerSelection mediaServerSelection = MediaServerSelection.GetByIp(localIp);

            _wirelessSocketName = mediaServerSelection.GetSocket();
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.getMessage());
        }
    }

    private boolean isSocketActive() {
        searchWirelessSocketName();

        if (_wirelessSocketName == null) {
            Logger.getInstance().Error(TAG, "_wirelessSocketName is null!");
            return false;
        }

        return WirelessSocketService.getInstance().GetWirelessSocketState(_wirelessSocketName);
    }
}
