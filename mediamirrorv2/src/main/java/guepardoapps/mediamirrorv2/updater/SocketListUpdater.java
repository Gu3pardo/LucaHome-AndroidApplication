package guepardoapps.mediamirrorv2.updater;

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
import guepardoapps.mediamirrorv2.common.constants.Broadcasts;
import guepardoapps.mediamirrorv2.common.constants.Bundles;

public class SocketListUpdater {
    private static final String TAG = SocketListUpdater.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private WirelessSocketService _wirelessSocketService;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
            WirelessSocketService.WirelessSocketDownloadFinishedContent result =
                    (WirelessSocketService.WirelessSocketDownloadFinishedContent) intent.getSerializableExtra(WirelessSocketService.WirelessSocketDownloadFinishedBundle);

            if (result != null) {
                if (result.WirelessSocketList != null) {
                    _broadcastController.SendSerializableBroadcast(
                            Broadcasts.SOCKET_LIST,
                            Bundles.SOCKET_LIST,
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
            _logger.Debug("_performUpdateReceiver onReceive");
            DownloadSocketList();
        }
    };

    public SocketListUpdater(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
        _wirelessSocketService = WirelessSocketService.getInstance();
        _wirelessSocketService.Initialize(_context, null, false, true, 15);
    }

    public void Start() {
        _logger.Debug("Initialize");

        if (_isRunning) {
            _logger.Warning("Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.DOWNLOAD_SOCKET_FINISHED});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_SOCKET_UPDATE});

        _isRunning = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadSocketList() {
        _logger.Debug("startDownloadSocketList");
        _wirelessSocketService.LoadData();
    }
}
