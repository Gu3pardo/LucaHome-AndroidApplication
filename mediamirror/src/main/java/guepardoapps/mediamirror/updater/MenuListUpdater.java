package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class MenuListUpdater {
    private static final String TAG = MenuListUpdater.class.getSimpleName();

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private MenuService _menuService;

    private boolean _isRunning;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MenuService.MenuDownloadFinishedContent result =
                    (MenuService.MenuDownloadFinishedContent) intent.getSerializableExtra(MenuService.MenuDownloadFinishedBundle);

            if (result != null) {
                _broadcastController.SendSerializableBroadcast(Broadcasts.MENU, Bundles.MENU, result.MenuList);
            } else {
                Logger.getInstance().Warning(TAG, "Result is null!");
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadMenuList();
        }
    };

    public MenuListUpdater(@NonNull Context context) {
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
        _menuService = MenuService.getInstance();
        _menuService.Initialize(context, true, 2 * 60);
    }

    public void Start() {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }

        _receiverController.RegisterReceiver(_updateReceiver, new String[]{MenuService.MenuDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_MENU_UPDATE});

        _isRunning = true;
        DownloadMenuList();
    }

    public void Dispose() {
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadMenuList() {
        _menuService.LoadData();
    }
}
