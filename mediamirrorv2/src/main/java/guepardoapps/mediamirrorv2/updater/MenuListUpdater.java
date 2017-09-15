package guepardoapps.mediamirrorv2.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.mediamirrorv2.common.constants.Broadcasts;
import guepardoapps.mediamirrorv2.common.constants.Bundles;

public class MenuListUpdater {
    private static final String TAG = MenuListUpdater.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private MenuService _menuService;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
            MenuService.MenuDownloadFinishedContent result =
                    (MenuService.MenuDownloadFinishedContent) intent.getSerializableExtra(MenuService.MenuDownloadFinishedBundle);

            if (result != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.MENU, Bundles.MENU, result.MenuList);
            } else {
                _logger.Warning("Result is null!");
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_performUpdateReceiver onReceive");
            DownloadMenuList();
        }
    };

    public MenuListUpdater(@NonNull Context context) {
        _logger = new Logger(TAG);

        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);

        _menuService = MenuService.getInstance();
        _menuService.Initialize(context, true, 2 * 60);
    }

    public void Start() {
        _logger.Debug("Initialize");

        if (_isRunning) {
            _logger.Warning("Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.DOWNLOAD_MENU_FINISHED});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_MENU_UPDATE});

        _isRunning = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadMenuList() {
        _logger.Debug("startDownloadMenuList");
        _menuService.LoadData();
    }
}
