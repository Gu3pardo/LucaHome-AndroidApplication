package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.ShoppingEntry;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToShoppingListConverter;
import guepardoapps.lucahome.common.database.DatabaseShoppingList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class ShoppingListService {
    public static class ShoppingListDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<ShoppingEntry> ShoppingList;

        public ShoppingListDownloadFinishedContent(SerializableList<ShoppingEntry> shoppingList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            ShoppingList = shoppingList;
        }
    }

    public static final String ShoppingIntent = "ShoppingIntent";

    public static final String ShoppingListDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.shoppinglist.download.finished";
    public static final String ShoppingListDownloadFinishedBundle = "ShoppingListDownloadFinishedBundle";

    public static final String ShoppingListAddFinishedBroadcast = "guepardoapps.lucahome.data.service.shoppinglist.add.finished";
    public static final String ShoppingListAddFinishedBundle = "ShoppingListAddFinishedBundle";

    public static final String ShoppingListUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.shoppinglist.update.finished";
    public static final String ShoppingListUpdateFinishedBundle = "ShoppingListUpdateFinishedBundle";

    public static final String ShoppingListDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.shoppinglist.delete.finished";
    public static final String ShoppingListDeleteFinishedBundle = "ShoppingListDeleteFinishedBundle";

    private static final ShoppingListService SINGLETON = new ShoppingListService();
    private boolean _isInitialized;

    private static final String TAG = ShoppingListService.class.getSimpleName();
    private Logger _logger;

    private static final int TIMEOUT_MS = 30 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadShoppingList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseShoppingList _databaseShoppingList;

    private JsonDataToShoppingListConverter _jsonDataToShoppingListConverter;

    private SerializableList<ShoppingEntry> _shoppingList = new SerializableList<>();

    private BroadcastReceiver _shoppingListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingList) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<ShoppingEntry> shoppingList = _jsonDataToShoppingListConverter.GetList(contentResponse);
            if (shoppingList == null) {
                _logger.Error("Converted shoppingList is null!");
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _shoppingList = shoppingList;

            clearShoppingListFromDatabase();
            saveShoppingListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    ShoppingListDownloadFinishedBroadcast,
                    ShoppingListDownloadFinishedBundle,
                    new ShoppingListDownloadFinishedContent(_shoppingList, true, content.Response));
        }
    };

    private BroadcastReceiver _shoppingListAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingListAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    ShoppingListAddFinishedBroadcast,
                    ShoppingListAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadShoppingList();
        }
    };

    private BroadcastReceiver _shoppingListUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingListUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    ShoppingListUpdateFinishedBroadcast,
                    ShoppingListUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadShoppingList();
        }
    };

    private BroadcastReceiver _shoppingListDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingListDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    ShoppingListDeleteFinishedBroadcast,
                    ShoppingListDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadShoppingList();
        }
    };

    private ShoppingListService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static ShoppingListService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _databaseShoppingList = new DatabaseShoppingList(context);
        _databaseShoppingList.Open();

        _receiverController.RegisterReceiver(_shoppingListDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingListAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingListUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingListDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToShoppingListConverter = new JsonDataToShoppingListConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseShoppingList.Close();
        _isInitialized = false;
    }

    public SerializableList<ShoppingEntry> GetShoppingList() {
        return _shoppingList;
    }

    public ArrayList<String> GetShoppingNameList() {
        ArrayList<String> nameList = new ArrayList<>();

        for (int index = 0; index < _shoppingList.getSize(); index++) {
            nameList.add(_shoppingList.getValue(index).GetName());
        }

        return nameList;
    }

    public ArrayList<String> GetShoppingDetailList() {
        ArrayList<String> nameList = new ArrayList<>();

        for (int index = 0; index < _shoppingList.getSize(); index++) {
            String detail = String.format(
                    Locale.getDefault(),
                    "%d x %s",
                    _shoppingList.getValue(index).GetQuantity(), _shoppingList.getValue(index).GetName());
            nameList.add(detail);
        }

        return nameList;
    }

    public ArrayList<String> GetShoppingGroupList() {
        ArrayList<String> groupList = new ArrayList<>();

        for (int index = 0; index < ShoppingEntryGroup.values().length; index++) {
            groupList.add(ShoppingEntryGroup.GetById(index).toString());
        }

        return groupList;
    }

    public ShoppingEntry GetById(int id) {
        for (int index = 0; index < _shoppingList.getSize(); index++) {
            ShoppingEntry entry = _shoppingList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    public SerializableList<ShoppingEntry> FoundShoppingEntries(@NonNull String searchKey) {
        SerializableList<ShoppingEntry> foundShoppingEntries = new SerializableList<>();

        for (int index = 0; index < _shoppingList.getSize(); index++) {
            ShoppingEntry entry = _shoppingList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetGroup().toString().contains(searchKey)
                    || String.valueOf(entry.GetQuantity()).contains(searchKey)) {
                foundShoppingEntries.addValue(entry);
            }
        }

        return foundShoppingEntries;
    }

    public void LoadShoppingList() {
        _logger.Debug("LoadShoppingList");

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _shoppingList = _databaseShoppingList.GetShoppingList();
            _broadcastController.SendSerializableBroadcast(
                    ShoppingListDownloadFinishedBroadcast,
                    ShoppingListDownloadFinishedBundle,
                    new ShoppingListDownloadFinishedContent(_shoppingList, true, Tools.CompressStringToByteArray("Loaded from database!")));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_SHOPPING_LIST.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingList, true);
    }

    public void AddShoppingEntry(ShoppingEntry entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddShoppingEntry: Adding new entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingListAdd, true);
    }

    public void UpdateShoppingEntry(ShoppingEntry entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateShoppingEntry: Updating entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingListUpdate, true);
    }

    public void DeleteShoppingEntry(ShoppingEntry entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteShoppingEntry: Deleting entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingListDelete, true);
    }

    private void clearShoppingListFromDatabase() {
        _logger.Debug("clearShoppingListFromDatabase");

        SerializableList<ShoppingEntry> shoppingList = _databaseShoppingList.GetShoppingList();
        for (int index = 0; index < shoppingList.getSize(); index++) {
            ShoppingEntry shoppingEntry = shoppingList.getValue(index);
            _databaseShoppingList.Delete(shoppingEntry);
        }
    }

    private void saveShoppingListToDatabase() {
        _logger.Debug("saveShoppingListToDatabase");

        for (int index = 0; index < _shoppingList.getSize(); index++) {
            ShoppingEntry shoppingEntry = _shoppingList.getValue(index);
            _databaseShoppingList.CreateEntry(shoppingEntry);
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ShoppingListDownloadFinishedBroadcast,
                ShoppingListDownloadFinishedBundle,
                new ShoppingListDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ShoppingListAddFinishedBroadcast,
                ShoppingListAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ShoppingListUpdateFinishedBroadcast,
                ShoppingListUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ShoppingListDeleteFinishedBroadcast,
                ShoppingListDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
