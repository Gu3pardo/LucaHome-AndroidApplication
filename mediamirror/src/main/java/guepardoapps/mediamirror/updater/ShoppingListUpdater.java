package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;

public class ShoppingListUpdater {
    private static final String TAG = ShoppingListUpdater.class.getSimpleName();

    private Handler _updater;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private ShoppingListService _shoppingListService;

    private int _updateTime;
    private boolean _isRunning;

    private Runnable _updateRunnable = new Runnable() {
        public void run() {
            DownloadShoppingList();
            _updater.postDelayed(_updateRunnable, _updateTime);
        }
    };

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ShoppingListService.ShoppingListDownloadFinishedContent result =
                    (ShoppingListService.ShoppingListDownloadFinishedContent) intent.getSerializableExtra(ShoppingListService.ShoppingListDownloadFinishedBundle);

            if (result != null) {
                _broadcastController.SendSerializableBroadcast(
                        Broadcasts.SHOPPING_LIST,
                        Bundles.SHOPPING_LIST,
                        result.ShoppingList);
            } else {
                Toasty.error(_context, "Result is null!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver _performUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadShoppingList();
        }
    };

    public ShoppingListUpdater(@NonNull Context context) {
        _updater = new Handler();
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
        _shoppingListService = ShoppingListService.getInstance();
        _shoppingListService.Initialize(_context, true, 60);
    }

    public void Start(int updateTime) {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }
        _updateTime = updateTime;
        _receiverController.RegisterReceiver(_updateReceiver, new String[]{ShoppingListService.ShoppingListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_SHOPPING_LIST_UPDATE});
        _updateRunnable.run();
        _isRunning = true;
        DownloadShoppingList();
    }

    public void Dispose() {
        _updater.removeCallbacks(_updateRunnable);
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadShoppingList() {
        _shoppingListService.LoadData();
    }
}
