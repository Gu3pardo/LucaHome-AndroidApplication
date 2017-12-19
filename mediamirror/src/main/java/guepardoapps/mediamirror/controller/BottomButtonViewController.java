package guepardoapps.mediamirror.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.rey.material.widget.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.interfaces.IViewController;

public class BottomButtonViewController implements IViewController {
    private static final String TAG = BottomButtonViewController.class.getSimpleName();
    private Logger _logger;

    private boolean _isInitialized;

    private Context _context;
    private BroadcastController _broadcastController;
    private DialogController _dialogController;
    private ReceiverController _receiverController;

    private SerializableList<LucaMenu> _menu;
    private SerializableList<ShoppingEntry> _shoppingList;
    private SerializableList<WirelessSocket> _socketList;

    private BroadcastReceiver _menuListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            @SuppressWarnings("unchecked")
            SerializableList<LucaMenu> menu = (SerializableList<LucaMenu>) intent.getSerializableExtra(Bundles.MENU);
            if (menu != null) {
                _menu = menu;
            } else {
                _logger.Warning("menu is null!");
            }
        }
    };

    private BroadcastReceiver _shoppingListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            @SuppressWarnings("unchecked")
            SerializableList<ShoppingEntry> shoppingList = (SerializableList<ShoppingEntry>) intent
                    .getSerializableExtra(Bundles.SHOPPING_LIST);
            if (shoppingList != null) {
                _shoppingList = shoppingList;
            } else {
                _logger.Warning("shoppingList is null!");
            }
        }
    };

    private BroadcastReceiver _socketListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            @SuppressWarnings("unchecked")
            SerializableList<WirelessSocket> socketList = (SerializableList<WirelessSocket>) intent
                    .getSerializableExtra(Bundles.SOCKET_LIST);
            if (socketList != null) {
                _socketList = socketList;
            } else {
                _logger.Warning("socketList is null!");
            }
        }
    };

    public BottomButtonViewController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _dialogController = new DialogController(_context);
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        FloatingActionButton floatingActionButtonMenu = ((Activity) _context).findViewById(R.id.floating_action_button_menu);
        FloatingActionButton floatingActionButtonShopping = ((Activity) _context).findViewById(R.id.floating_action_button_shopping);
        FloatingActionButton floatingActionButtonSocket = ((Activity) _context).findViewById(R.id.floating_action_button_socket);
        FloatingActionButton floatingActionButtonReload = ((Activity) _context).findViewById(R.id.floating_action_button_reload);

        floatingActionButtonMenu.setOnClickListener(view -> {
            _logger.Debug("Show Menu Dialog");
            if (_menu != null) {
                ArrayList<String> menuTitleList = new ArrayList<>();
                for (int index = 0; index < _menu.getSize(); index++) {
                    menuTitleList.add(_menu.getValue(index).GetSingleLineString());
                }

                _dialogController.DisplayListViewDialog("Menu", menuTitleList);
            } else {
                _logger.Error("_menu is null!");
                Toasty.warning(_context, "Menu is null!!", Toast.LENGTH_LONG).show();
            }
        });

        floatingActionButtonShopping.setOnClickListener(view -> {
            _logger.Debug("Show Shopping Dialog");
            if (_shoppingList != null) {
                ArrayList<String> shoppingListTitleList = new ArrayList<>();
                for (int index = 0; index < _shoppingList.getSize(); index++) {
                    shoppingListTitleList.add(String.format(Locale.getDefault(), "%dx %s", _shoppingList.getValue(index).GetQuantity(), _shoppingList.getValue(index).GetName()));
                }

                _dialogController.DisplayListViewDialog("Shopping List", shoppingListTitleList);
            } else {
                _logger.Error("_shoppingList is null!");
                Toasty.warning(_context, "ShoppingList is null!!", Toast.LENGTH_LONG).show();
            }
        });

        floatingActionButtonSocket.setOnClickListener(view -> {
            _logger.Debug("Show Socket Dialog");
            if (_socketList != null) {
                _dialogController.DisplaySocketListViewDialog(_socketList);
            } else {
                _logger.Error("_socketList is null!");
                Toasty.warning(_context, "SocketList is null!!", Toast.LENGTH_LONG).show();
            }
        });

        floatingActionButtonReload.setOnClickListener(view -> {
            _logger.Debug("Reload Data");
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_ALL);
        });
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");
    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_menuListReceiver, new String[]{Broadcasts.MENU});
            _receiverController.RegisterReceiver(_shoppingListReceiver, new String[]{Broadcasts.SHOPPING_LIST});
            _receiverController.RegisterReceiver(_socketListReceiver, new String[]{Broadcasts.SOCKET_LIST});

            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warning("Is ALREADY initialized!");
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
