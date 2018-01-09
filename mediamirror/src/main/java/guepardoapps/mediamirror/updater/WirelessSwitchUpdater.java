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
import guepardoapps.lucahome.common.service.WirelessSwitchService;

import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.receiver.WirelessSwitchActionReceiver;

public class WirelessSwitchUpdater {
    private static final String TAG = WirelessSwitchUpdater.class.getSimpleName();

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WirelessSwitchService.WirelessSwitchDownloadFinishedContent result = (WirelessSwitchService.WirelessSwitchDownloadFinishedContent) intent.getSerializableExtra(WirelessSwitchService.WirelessSwitchDownloadFinishedBundle);
            if (result != null) {
                if (result.WirelessSwitchList != null) {
                    _broadcastController.SendSerializableBroadcast(
                            Broadcasts.WIRELESS_SWITCH_LIST,
                            Bundles.WIRELESS_SWITCH_LIST,
                            result.WirelessSwitchList);
                } else {
                    Toasty.error(_context, "Failed to convert wireless switch list from string array!", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadWirelessSwitchList();
        }
    };

    public WirelessSwitchUpdater(@NonNull Context context) {
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
        WirelessSwitchService.getInstance().Initialize(_context, WirelessSwitchActionReceiver.class, false, true, 15);
    }

    public void Start() {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }
        _receiverController.RegisterReceiver(_updateReceiver, new String[]{WirelessSwitchService.WirelessSwitchDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_WIRELESS_SWITCH_UPDATE});
        _isRunning = true;
        DownloadWirelessSwitchList();
    }

    public void Dispose() {
        WirelessSwitchService.getInstance().Dispose();
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadWirelessSwitchList() {
        WirelessSwitchService.getInstance().LoadData();
    }
}
