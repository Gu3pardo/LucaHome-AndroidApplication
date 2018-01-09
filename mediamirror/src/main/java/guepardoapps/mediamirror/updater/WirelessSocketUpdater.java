package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.receiver.WirelessSocketActionReceiver;

public class WirelessSocketUpdater {
    private static final String TAG = WirelessSocketUpdater.class.getSimpleName();

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WirelessSocketService.WirelessSocketDownloadFinishedContent result = (WirelessSocketService.WirelessSocketDownloadFinishedContent) intent.getSerializableExtra(WirelessSocketService.WirelessSocketDownloadFinishedBundle);
            if (result != null) {
                if (result.WirelessSocketList != null) {
                    _broadcastController.SendSerializableBroadcast(
                            Broadcasts.WIRELESS_SOCKET_LIST,
                            Bundles.WIRELESS_SOCKET_LIST,
                            result.WirelessSocketList);
                } else {
                    Toasty.error(_context, "Failed to convert socket list from string array!", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadWirelessSocketList();
        }
    };

    public WirelessSocketUpdater(@NonNull Context context) {
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
        WirelessSocketService.getInstance().Initialize(_context, WirelessSocketActionReceiver.class, false, true, 15);
    }

    public void Start() {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }
        _receiverController.RegisterReceiver(_updateReceiver, new String[]{WirelessSocketService.WirelessSocketDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_WIRELESS_SOCKET_UPDATE});
        _isRunning = true;
        DownloadWirelessSocketList();
    }

    public void Dispose() {
        WirelessSocketService.getInstance().Dispose();
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadWirelessSocketList() {
        WirelessSocketService.getInstance().LoadData();
    }
}
