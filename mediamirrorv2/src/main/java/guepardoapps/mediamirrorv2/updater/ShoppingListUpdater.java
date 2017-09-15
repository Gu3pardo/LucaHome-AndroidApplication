package guepardoapps.mediamirrorv2.updater;

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
import guepardoapps.mediamirrorv2.common.constants.Broadcasts;
import guepardoapps.mediamirrorv2.common.constants.Bundles;

public class ShoppingListUpdater {
    private static final String TAG = ShoppingListUpdater.class.getSimpleName();
    private Logger _logger;

    private Handler _updater;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private ShoppingListService _shoppingListService;

    private int _updateTime;
    private boolean _isRunning;

    private Runnable _updateRunnable = new Runnable() {
        public void run() {
            _logger.Debug("_updateRunnable run");
            DownloadShoppingList();
            _updater.postDelayed(_updateRunnable, _updateTime);
        }
    };

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
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
            _logger.Debug("_performUpdateReceiver onReceive");
            DownloadShoppingList();
        }
    };

    public ShoppingListUpdater(@NonNull Context context) {
        _logger = new Logger(TAG);
        _updater = new Handler();
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
        _shoppingListService = ShoppingListService.getInstance();
        _shoppingListService.Initialize(_context, true, 60);
    }

    public void Start(int updateTime) {
        _logger.Debug("Initialize");

        if (_isRunning) {
            _logger.Warning("Already running!");
            return;
        }

        _updateTime = updateTime;
        _logger.Debug("UpdateTime is: " + String.valueOf(_updateTime));
        _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.DOWNLOAD_SHOPPING_LIST_FINISHED});
        _receiverController.RegisterReceiver(_performUpdateReceiver, new String[]{Broadcasts.PERFORM_SHOPPING_LIST_UPDATE});
        _updateRunnable.run();

        _isRunning = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _updater.removeCallbacks(_updateRunnable);
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void DownloadShoppingList() {
        _logger.Debug("startDownloadShoppingList");
        _shoppingListService.LoadData();
    }
}
