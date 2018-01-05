package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.ListedMenu;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToListedMenuConverter;
import guepardoapps.lucahome.common.database.DatabaseListedMenuList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class ListedMenuService implements IDataService {
    public static class ListedMenuDownloadFinishedContent extends ObjectChangeFinishedContent {
        SerializableList<ListedMenu> ListedMenuList;

        ListedMenuDownloadFinishedContent(SerializableList<ListedMenu> listedMenuList, boolean succcess) {
            super(succcess, new byte[]{});
            ListedMenuList = listedMenuList;
        }
    }

    public static final String ListedMenuIntent = "ListedMenuIntent";

    public static final String ListedMenuDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.listedmenu.download.finished";
    public static final String ListedMenuDownloadFinishedBundle = "ListedMenuDownloadFinishedBundle";

    public static final String ListedMenuAddFinishedBroadcast = "guepardoapps.lucahome.data.service.listedmenu.add.finished";
    public static final String ListedMenuAddFinishedBundle = "ListedMenuAddFinishedBundle";

    public static final String ListedMenuUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.listedmenu.update.finished";
    public static final String ListedMenuUpdateFinishedBundle = "ListedMenuUpdateFinishedBundle";

    public static final String ListedMenuDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.listedmenu.delete.finished";
    public static final String ListedMenuDeleteFinishedBundle = "ListedMenuDeleteFinishedBundle";

    private static final ListedMenuService SINGLETON = new ListedMenuService();
    private boolean _isInitialized;

    private static final String TAG = ListedMenuService.class.getSimpleName();

    private boolean _loadDataEnabled;

    private Date _lastUpdate;

    private static final int MIN_TIMEOUT_MIN = 2 * 60;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            LoadData();
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private DatabaseListedMenuList _databaseListedMenuList;

    private SerializableList<ListedMenu> _listedMenuList = new SerializableList<>();

    private BroadcastReceiver _listedMenuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ListedMenu) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                _listedMenuList = _databaseListedMenuList.GetListedMenuList();
                sendFailedListedMenuDownloadBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _listedMenuList = _databaseListedMenuList.GetListedMenuList();
                sendFailedListedMenuDownloadBroadcast();
                return;
            }

            SerializableList<ListedMenu> listedMenuList = JsonDataToListedMenuConverter.getInstance().GetList(contentResponse);
            if (listedMenuList == null) {
                Logger.getInstance().Error(TAG, "Converted listedMenuList is null!");
                _listedMenuList = _databaseListedMenuList.GetListedMenuList();
                sendFailedListedMenuDownloadBroadcast();
                return;
            }

            _lastUpdate = new Date();

            _listedMenuList = listedMenuList;

            clearListedMenuListFromDatabase();
            saveListedMenuListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    ListedMenuDownloadFinishedBroadcast,
                    ListedMenuDownloadFinishedBundle,
                    new ListedMenuDownloadFinishedContent(_listedMenuList, true));
        }
    };

    private BroadcastReceiver _listedMenuAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ListedMenuAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedListedMenuAddBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedListedMenuAddBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    ListedMenuAddFinishedBroadcast,
                    ListedMenuAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _listedMenuUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ListedMenuUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedListedMenuUpdateBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedListedMenuUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    ListedMenuUpdateFinishedBroadcast,
                    ListedMenuUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _listedMenuDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ListedMenuDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedListedMenuDeleteBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedListedMenuDeleteBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    ListedMenuDeleteFinishedBroadcast,
                    ListedMenuDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastReceiver _homeNetworkNotAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
        }
    };

    private ListedMenuService() {
    }

    public static ListedMenuService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _lastUpdate = new Date();

        _reloadEnabled = reloadEnabled;
        _loadDataEnabled = true;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);

        _databaseListedMenuList = new DatabaseListedMenuList(context);
        _databaseListedMenuList.Open();

        _receiverController.RegisterReceiver(_listedMenuDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_listedMenuAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_listedMenuUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_listedMenuDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseListedMenuList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<ListedMenu> GetDataList() {
        return _listedMenuList;
    }

    public ArrayList<String> GetListedMenuNameList() {
        ArrayList<String> nameList = new ArrayList<>();
        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            nameList.add(_listedMenuList.getValue(index).GetTitle());
        }
        return new ArrayList<>(nameList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetListedMenuDescriptionList() {
        ArrayList<String> descriptionList = new ArrayList<>();
        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            descriptionList.add(_listedMenuList.getValue(index).GetDescription());
        }
        return new ArrayList<>(descriptionList.stream().distinct().collect(Collectors.toList()));
    }

    public ListedMenu GetListedMenuById(int id) {
        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            ListedMenu entry = _listedMenuList.getValue(index);
            if (entry.GetId() == id) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int GetHighestId() {
        int highestId = -1;
        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            int id = _listedMenuList.getValue(index).GetId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    @Override
    public SerializableList<ListedMenu> SearchDataList(@NonNull String searchKey) {
        SerializableList<ListedMenu> foundListedMenus = new SerializableList<>();
        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            ListedMenu entry = _listedMenuList.getValue(index);
            if (entry.toString().contains(searchKey)) {
                foundListedMenus.addValue(entry);
            }
        }
        return foundListedMenus;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _listedMenuList = _databaseListedMenuList.GetListedMenuList();
            _broadcastController.SendSerializableBroadcast(
                    ListedMenuDownloadFinishedBroadcast,
                    ListedMenuDownloadFinishedBundle,
                    new ListedMenuDownloadFinishedContent(_listedMenuList, true));
            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedListedMenuDownloadBroadcast();
            return;
        }

        if (hasListedMenuEntryNotOnServer()) {
            _loadDataEnabled = false;

            for (int index = 0; index < notOnServerListedMenu().getSize(); index++) {
                ListedMenu listedMenu = notOnServerListedMenu().getValue(index);

                switch (listedMenu.GetServerDbAction()) {
                    case Update:
                        UpdateListedMenu(listedMenu);
                        break;
                    case Delete:
                        DeleteListedMenu(listedMenu);
                        break;
                    case Add:
                        AddListedMenu(listedMenu);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Nothing todo with %s.", listedMenu));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_LISTEDMENU.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ListedMenu, true);
    }

    public void AddListedMenu(@NonNull ListedMenu entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);

            _databaseListedMenuList.CreateEntry(entry);

            LoadData();

            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedListedMenuAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ListedMenuAdd, true);
    }

    public void UpdateListedMenu(@NonNull ListedMenu entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);

            _databaseListedMenuList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedListedMenuUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ListedMenuUpdate, true);
    }

    public void DeleteListedMenu(@NonNull ListedMenu entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);

            _databaseListedMenuList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedListedMenuDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ListedMenuDelete, true);
    }

    @Override
    public boolean GetReloadEnabled() {
        return _reloadEnabled;
    }

    @Override
    public void SetReloadEnabled(boolean reloadEnabled) {
        _reloadEnabled = reloadEnabled;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    @Override
    public int GetReloadTimeout() {
        return _reloadTimeout;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        if (reloadTimeout < MIN_TIMEOUT_MIN) {
            reloadTimeout = MIN_TIMEOUT_MIN;
        }
        if (reloadTimeout > MAX_TIMEOUT_MIN) {
            reloadTimeout = MAX_TIMEOUT_MIN;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    public Date GetLastUpdate() {
        return _lastUpdate;
    }

    private void clearListedMenuListFromDatabase() {
        SerializableList<ListedMenu> listedMenuList = _databaseListedMenuList.GetListedMenuList();
        for (int index = 0; index < listedMenuList.getSize(); index++) {
            ListedMenu listedMenu = listedMenuList.getValue(index);
            _databaseListedMenuList.Delete(listedMenu);
        }
    }

    private void saveListedMenuListToDatabase() {
        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            ListedMenu listedMenu = _listedMenuList.getValue(index);
            _databaseListedMenuList.CreateEntry(listedMenu);
        }
    }

    private void sendFailedListedMenuDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                ListedMenuDownloadFinishedBroadcast,
                ListedMenuDownloadFinishedBundle,
                new ListedMenuDownloadFinishedContent(_listedMenuList, false));
    }

    private void sendFailedListedMenuAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for listedmenu failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                ListedMenuAddFinishedBroadcast,
                ListedMenuAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedListedMenuUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update for listedmenu failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                ListedMenuUpdateFinishedBroadcast,
                ListedMenuUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedListedMenuDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for listedmenu failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                ListedMenuDeleteFinishedBroadcast,
                ListedMenuDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private SerializableList<ListedMenu> notOnServerListedMenu() {
        SerializableList<ListedMenu> notOnServerListedMenuList = new SerializableList<>();

        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            if (!_listedMenuList.getValue(index).GetIsOnServer()) {
                notOnServerListedMenuList.addValue(_listedMenuList.getValue(index));
            }
        }

        return notOnServerListedMenuList;
    }

    private boolean hasListedMenuEntryNotOnServer() {
        return notOnServerListedMenu().getSize() > 0;
    }
}
