package guepardoapps.lucahome.common.controller.mediaserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.common.classes.MediaServer;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.UserInformationController;
import guepardoapps.lucahome.common.services.WirelessSocketService;
import guepardoapps.lucahome.common.services.MediaServerClientService;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class WirelessSocketBatteryController implements IWirelessSocketBatteryController {
    private static final String Tag = WirelessSocketBatteryController.class.getSimpleName();

    private Context _context;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level > BatteryLimitUpperPercent) {
                disableBatterySocket();
            } else if (level < BatteryLimitLowerPercent) {
                enableBatterySocket();
            }
        }
    };

    public WirelessSocketBatteryController(@NonNull Context context) {
        _context = context;
        _receiverController = new ReceiverController(_context);
        _userInformationController = new UserInformationController(_context);
        _receiverController.RegisterReceiver(_batteryInfoReceiver, new String[]{Intent.ACTION_BATTERY_CHANGED});
    }

    @Override
    public void Dispose() {
        _receiverController.Dispose();
    }

    private void enableBatterySocket() {
        WirelessSocket wirelessSocket = getWirelessSocket();
        if (wirelessSocket == null) {
            Toasty.error(_context, "WirelessSocket is null! Cannot enable!", Toast.LENGTH_LONG).show();
            return;
        }

        if (wirelessSocket.GetState()) {
            Logger.getInstance().Warning(Tag, "Already activated wireless socket!");
            return;
        }

        try {
            WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, true);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }
    }

    private void disableBatterySocket() {
        WirelessSocket wirelessSocket = getWirelessSocket();
        if (wirelessSocket == null) {
            Toasty.error(_context, "WirelessSocket is null! Cannot disable!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!wirelessSocket.GetState()) {
            Logger.getInstance().Warning(Tag, "Already deactivated wireless socket!");
            return;
        }

        try {
            WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, false);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }
    }

    private WirelessSocket getWirelessSocket() {
        String ip = _userInformationController.GetIp();
        MediaServer mediaServer = MediaServerClientService.getInstance().GetMediaServerByIp(ip);
        if (mediaServer != null) {
            return WirelessSocketService.getInstance().GetByUuid(mediaServer.GetWirelessSocketUuid());
        }
        return null;
    }
}
