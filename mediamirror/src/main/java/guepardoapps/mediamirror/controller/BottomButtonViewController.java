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
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.interfaces.IViewController;

@SuppressWarnings("unchecked")
public class BottomButtonViewController implements IViewController {
    private static final String TAG = BottomButtonViewController.class.getSimpleName();

    private boolean _isInitialized;

    private Context _context;
    private BroadcastController _broadcastController;
    private DialogController _dialogController;
    private ReceiverController _receiverController;

    private SerializableList<LucaMenu> _menu;
    private SerializableList<ShoppingEntry> _shoppingList;
    private SerializableList<WirelessSocket> _wirelessSocketList;
    private SerializableList<WirelessSwitch> _wirelessSwitchList;

    private BroadcastReceiver _menuListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SerializableList<LucaMenu> menu = (SerializableList<LucaMenu>) intent.getSerializableExtra(Bundles.MENU);
            if (menu != null) {
                _menu = menu;
            } else {
                Logger.getInstance().Warning(TAG, "menu is null!");
            }
        }
    };

    private BroadcastReceiver _shoppingListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SerializableList<ShoppingEntry> shoppingList = (SerializableList<ShoppingEntry>) intent
                    .getSerializableExtra(Bundles.SHOPPING_LIST);
            if (shoppingList != null) {
                _shoppingList = shoppingList;
            } else {
                Logger.getInstance().Warning(TAG, "shoppingList is null!");
            }
        }
    };

    private BroadcastReceiver _wirelessSocketListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SerializableList<WirelessSocket> wirelessSocketList = (SerializableList<WirelessSocket>) intent.getSerializableExtra(Bundles.WIRELESS_SOCKET_LIST);
            if (wirelessSocketList != null) {
                _wirelessSocketList = wirelessSocketList;
            } else {
                Logger.getInstance().Warning(TAG, "wirelessSocketList is null!");
            }
        }
    };

    private BroadcastReceiver _wirelessSwitchListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SerializableList<WirelessSwitch> wirelessSwitchList = (SerializableList<WirelessSwitch>) intent.getSerializableExtra(Bundles.WIRELESS_SWITCH_LIST);
            if (wirelessSwitchList != null) {
                _wirelessSwitchList = wirelessSwitchList;
            } else {
                Logger.getInstance().Warning(TAG, "wirelessSwitchList is null!");
            }
        }
    };

    public BottomButtonViewController(@NonNull Context context) {
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _dialogController = new DialogController(_context);
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        FloatingActionButton floatingActionButtonMenu = ((Activity) _context).findViewById(R.id.floating_action_button_menu);
        FloatingActionButton floatingActionButtonShopping = ((Activity) _context).findViewById(R.id.floating_action_button_shopping);
        FloatingActionButton floatingActionButtonWirelessSocket = ((Activity) _context).findViewById(R.id.floating_action_button_socket);
        FloatingActionButton floatingActionButtonWirelessSwitch = ((Activity) _context).findViewById(R.id.floating_action_button_switch);
        FloatingActionButton floatingActionButtonReload = ((Activity) _context).findViewById(R.id.floating_action_button_reload);

        floatingActionButtonMenu.setOnClickListener(view -> {
            if (_menu != null) {
                ArrayList<String> menuTitleList = new ArrayList<>();
                for (int index = 0; index < _menu.getSize(); index++) {
                    menuTitleList.add(_menu.getValue(index).GetSingleLineString());
                }

                _dialogController.DisplayListViewDialog("Menu", menuTitleList);
            } else {
                Logger.getInstance().Error(TAG, "_menu is null!");
                Toasty.warning(_context, "Menu is null!!", Toast.LENGTH_LONG).show();
            }
        });

        floatingActionButtonShopping.setOnClickListener(view -> {
            if (_shoppingList != null) {
                ArrayList<String> shoppingListTitleList = new ArrayList<>();
                for (int index = 0; index < _shoppingList.getSize(); index++) {
                    shoppingListTitleList.add(String.format(Locale.getDefault(), "%dx %s", _shoppingList.getValue(index).GetQuantity(), _shoppingList.getValue(index).GetName()));
                }

                _dialogController.DisplayListViewDialog("Shopping List", shoppingListTitleList);
            } else {
                Logger.getInstance().Error(TAG, "_shoppingList is null!");
                Toasty.warning(_context, "ShoppingList is null!!", Toast.LENGTH_LONG).show();
            }
        });

        floatingActionButtonWirelessSocket.setOnClickListener(view -> {
            if (_wirelessSocketList != null) {
                _dialogController.DisplaySocketListViewDialog(_wirelessSocketList);
            } else {
                Logger.getInstance().Error(TAG, "_wirelessSocketList is null!");
                Toasty.warning(_context, "WirelessSocketList is null!!", Toast.LENGTH_LONG).show();
            }
        });

        floatingActionButtonWirelessSwitch.setOnClickListener(view -> {
            if (_wirelessSocketList != null) {
                _dialogController.DisplaySwitchListViewDialog(_wirelessSwitchList);
            } else {
                Logger.getInstance().Error(TAG, "_wirelessSwitchList is null!");
                Toasty.warning(_context, "WirelessSwitchList is null!!", Toast.LENGTH_LONG).show();
            }
        });

        floatingActionButtonReload.setOnClickListener(view -> _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_ALL));
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_menuListReceiver, new String[]{Broadcasts.MENU});
            _receiverController.RegisterReceiver(_shoppingListReceiver, new String[]{Broadcasts.SHOPPING_LIST});
            _receiverController.RegisterReceiver(_wirelessSocketListReceiver, new String[]{Broadcasts.WIRELESS_SOCKET_LIST});
            _receiverController.RegisterReceiver(_wirelessSwitchListReceiver, new String[]{Broadcasts.WIRELESS_SWITCH_LIST});
            _isInitialized = true;
        } else {
            Logger.getInstance().Warning(TAG, "Is ALREADY initialized!");
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
