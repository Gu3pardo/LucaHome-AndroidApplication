package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.ListedMenu;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToListedMenuConverter;
import guepardoapps.lucahome.common.converter.JsonDataToMenuConverter;
import guepardoapps.lucahome.common.database.DatabaseListedMenuList;
import guepardoapps.lucahome.common.database.DatabaseMenuList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class MenuService implements IDataService {
    public static class ListedMenuDownloadFinishedContent extends ObjectChangeFinishedContent {
        SerializableList<ListedMenu> ListedMenuList;

        ListedMenuDownloadFinishedContent(SerializableList<ListedMenu> listedMenuList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            ListedMenuList = listedMenuList;
        }
    }

    public static class MenuDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<LucaMenu> MenuList;

        MenuDownloadFinishedContent(SerializableList<LucaMenu> menuList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MenuList = menuList;
        }
    }

    public static final String MenuIntent = "MenuIntent";

    public static final String ListedMenuDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.listedmenu.download.finished";
    public static final String ListedMenuDownloadFinishedBundle = "ListedMenuDownloadFinishedBundle";

    public static final String MenuDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.menu.download.finished";
    public static final String MenuDownloadFinishedBundle = "MenuDownloadFinishedBundle";

    public static final String MenuUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.menu.update.finished";
    public static final String MenuUpdateFinishedBundle = "MenuUpdateFinishedBundle";

    public static final String MenuClearFinishedBroadcast = "guepardoapps.lucahome.data.service.menu.clear.finished";
    public static final String MenuClearFinishedBundle = "MenuClearFinishedBundle";

    private static final MenuService SINGLETON = new MenuService();
    private boolean _isInitialized;

    private static final String TAG = MenuService.class.getSimpleName();
    private Logger _logger;

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
            _logger.Debug("_reloadListRunnable run");

            LoadListedMenuList();
            LoadData();

            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private Context _context;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseListedMenuList _databaseListedMenuList;
    private DatabaseMenuList _databaseMenuList;

    private JsonDataToListedMenuConverter _jsonDataToListedMenuConverter;
    private JsonDataToMenuConverter _jsonDataToMenuConverter;

    private SerializableList<ListedMenu> _listedMenuList = new SerializableList<>();
    private SerializableList<LucaMenu> _menuList = new SerializableList<>();

    private BroadcastReceiver _listedMenuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_listedMenuDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ListedMenu) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                _listedMenuList = _databaseListedMenuList.GetListedMenuList();
                sendFailedListedMenuDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                _listedMenuList = _databaseListedMenuList.GetListedMenuList();
                sendFailedListedMenuDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<ListedMenu> listedMenuList = _jsonDataToListedMenuConverter.GetList(contentResponse);
            if (listedMenuList == null) {
                _logger.Error("Converted listedMenuList is null!");
                _listedMenuList = _databaseListedMenuList.GetListedMenuList();
                sendFailedListedMenuDownloadBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _listedMenuList = listedMenuList;

            clearListedMenuListFromDatabase();
            saveListedMenuListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    ListedMenuDownloadFinishedBroadcast,
                    ListedMenuDownloadFinishedBundle,
                    new ListedMenuDownloadFinishedContent(_listedMenuList, true, content.Response));
        }
    };

    private BroadcastReceiver _menuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Menu) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                _menuList = _databaseMenuList.GetMenuList();
                sendFailedMenuDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                _menuList = _databaseMenuList.GetMenuList();
                sendFailedMenuDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<LucaMenu> menuList = _jsonDataToMenuConverter.GetList(contentResponse);
            if (menuList == null) {
                _logger.Error("Converted menuList is null!");
                _menuList = _databaseMenuList.GetMenuList();
                sendFailedMenuDownloadBroadcast(contentResponse);
                return;
            }

            // Sort list
            int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            int startIndex = -1;
            for (int index = 0; index < menuList.getSize(); index++) {
                if (menuList.getValue(index).GetDate().DayOfMonth() == dayOfMonth) {
                    startIndex = index;
                    break;
                }
            }
            if (startIndex != -1) {
                SerializableList<LucaMenu> sortedList = new SerializableList<>();
                int selectedIndex = startIndex;
                for (int index = 0; index < menuList.getSize(); index++) {
                    if (selectedIndex >= menuList.getSize()) {
                        selectedIndex = selectedIndex - menuList.getSize();
                    }
                    sortedList.addValue(menuList.getValue(selectedIndex));
                    selectedIndex++;
                }
                menuList = sortedList;
            }
            _menuList = menuList;

            _lastUpdate = new Date();

            controlMenus();

            clearMenuListFromDatabase();
            saveMenuListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    MenuDownloadFinishedBroadcast,
                    MenuDownloadFinishedBundle,
                    new MenuDownloadFinishedContent(_menuList, true, content.Response));
        }
    };

    private BroadcastReceiver _menuUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MenuUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedMenuUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedMenuUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    MenuUpdateFinishedBroadcast,
                    MenuUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _menuClearFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuClearFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MenuClear) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedMenuClearBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedMenuClearBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    MenuClearFinishedBroadcast,
                    MenuClearFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_homeNetworkAvailableReceiver onReceive");
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastReceiver _homeNetworkNotAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_homeNetworkNotAvailableReceiver onReceive");
            _reloadHandler.removeCallbacks(_reloadListRunnable);
        }
    };

    private MenuService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static MenuService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _lastUpdate = new Date();

        _reloadEnabled = reloadEnabled;

        _loadDataEnabled = true;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _downloadController = new DownloadController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);
        _settingsController = SettingsController.getInstance();

        _databaseListedMenuList = new DatabaseListedMenuList(_context);
        _databaseListedMenuList.Open();
        _databaseMenuList = new DatabaseMenuList(_context);
        _databaseMenuList.Open();

        _receiverController.RegisterReceiver(_listedMenuDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuClearFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        _jsonDataToListedMenuConverter = new JsonDataToListedMenuConverter();
        _jsonDataToMenuConverter = new JsonDataToMenuConverter();

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseListedMenuList.Close();
        _databaseMenuList.Close();
        _isInitialized = false;
    }

    public SerializableList<ListedMenu> GetListedMenuList() {
        return _listedMenuList;
    }

    public ArrayList<String> GetDescriptionList() {
        ArrayList<String> descriptionList = new ArrayList<>();

        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            descriptionList.add(_listedMenuList.getValue(index).GetDescription());
        }

        return descriptionList;
    }

    @Override
    public SerializableList<LucaMenu> GetDataList() {
        return _menuList;
    }

    public ArrayList<String> GetMenuNameList() {
        ArrayList<String> nameList = new ArrayList<>();

        for (int index = 0; index < _menuList.getSize(); index++) {
            nameList.add(_menuList.getValue(index).GetTitle());
        }

        return nameList;
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

    public LucaMenu GetMenuById(int id) {
        for (int index = 0; index < _menuList.getSize(); index++) {
            LucaMenu entry = _menuList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public SerializableList<LucaMenu> SearchDataList(@NonNull String searchKey) {
        SerializableList<LucaMenu> foundMenus = new SerializableList<>();

        for (int index = 0; index < _menuList.getSize(); index++) {
            LucaMenu entry = _menuList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetTitle().contains(searchKey)
                    || entry.GetDescription().contains(searchKey)
                    || entry.GetDate().toString().contains(searchKey)) {
                foundMenus.addValue(entry);
            }
        }

        return foundMenus;
    }

    public void LoadListedMenuList() {
        _logger.Debug("LoadListedMenuList");

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _listedMenuList = _databaseListedMenuList.GetListedMenuList();
            _broadcastController.SendSerializableBroadcast(
                    ListedMenuDownloadFinishedBroadcast,
                    ListedMenuDownloadFinishedBundle,
                    new ListedMenuDownloadFinishedContent(_listedMenuList, true, Tools.CompressStringToByteArray("Loaded from database!")));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedListedMenuDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_LISTEDMENU.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ListedMenu, true);
    }

    @Override
    public void LoadData() {
        _logger.Debug("LoadData");

        if (!_loadDataEnabled) {
            _logger.Debug("_loadDataEnabled is false!");
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _menuList = _databaseMenuList.GetMenuList();
            _broadcastController.SendSerializableBroadcast(
                    MenuDownloadFinishedBroadcast,
                    MenuDownloadFinishedBundle,
                    new MenuDownloadFinishedContent(_menuList, true, Tools.CompressStringToByteArray("Loaded from database!")));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedMenuDownloadBroadcast("No user");
            return;
        }

        if (hasMenuEntryNotOnServer()) {
            _loadDataEnabled = false;

            for (int index = 0; index < notOnServerMenu().getSize(); index++) {
                LucaMenu lucaMenu = notOnServerMenu().getValue(index);

                switch (lucaMenu.GetServerDbAction()) {
                    case Update:
                        UpdateMenu(lucaMenu);
                        break;
                    case Delete:
                        ClearMenu(lucaMenu);
                        break;
                    case Add:
                    case Null:
                    default:
                        _logger.Debug(String.format(Locale.getDefault(), "Nothing todo with %s.", lucaMenu));
                        break;
                }

            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_MENU.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Menu, true);
    }

    public void UpdateMenu(@NonNull LucaMenu entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateMenu: Updating entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);

            _databaseMenuList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedMenuUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MenuUpdate, true);
    }

    public void ClearMenu(@NonNull LucaMenu entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteMenu: Deleting entry %s", entry));

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);

            _databaseMenuList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedMenuClearBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MenuClear, true);
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
            _logger.Warning(String.format(Locale.getDefault(), "reloadTimeout %d is lower then MIN_TIMEOUT_MIN %d! Setting to MIN_TIMEOUT_MIN!", reloadTimeout, MIN_TIMEOUT_MIN));
            reloadTimeout = MIN_TIMEOUT_MIN;
        }
        if (reloadTimeout > MAX_TIMEOUT_MIN) {
            _logger.Warning(String.format(Locale.getDefault(), "reloadTimeout %d is higher then MAX_TIMEOUT_MIN %d! Setting to MAX_TIMEOUT_MIN!", reloadTimeout, MAX_TIMEOUT_MIN));
            reloadTimeout = MAX_TIMEOUT_MIN;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    public void ShareMenuList() {
        StringBuilder shareText = new StringBuilder("Menu:\n");

        for (int index = 0; index < _menuList.getSize(); index++) {
            LucaMenu entry = _menuList.getValue(index);
            shareText.append(entry.GetDateString()).append("\n").append(entry.GetTitle()).append("\n").append(entry.GetDescription()).append("\n\n");
        }

        Intent sendIntent = new Intent();

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        sendIntent.setType("text/plain");

        _context.startActivity(sendIntent);
    }

    public Date GetLastUpdate() {
        return _lastUpdate;
    }

    private void clearListedMenuListFromDatabase() {
        _logger.Debug("clearListedMenuListFromDatabase");

        SerializableList<ListedMenu> listedMenuList = _databaseListedMenuList.GetListedMenuList();
        for (int index = 0; index < listedMenuList.getSize(); index++) {
            ListedMenu listedMenu = listedMenuList.getValue(index);
            _databaseListedMenuList.Delete(listedMenu);
        }
    }

    private void saveListedMenuListToDatabase() {
        _logger.Debug("saveListedMenuListToDatabase");

        for (int index = 0; index < _listedMenuList.getSize(); index++) {
            ListedMenu listedMenu = _listedMenuList.getValue(index);
            _databaseListedMenuList.CreateEntry(listedMenu);
        }
    }

    private void clearMenuListFromDatabase() {
        _logger.Debug("clearMenuListFromDatabase");

        SerializableList<LucaMenu> menuList = _databaseMenuList.GetMenuList();
        for (int index = 0; index < menuList.getSize(); index++) {
            LucaMenu menu = menuList.getValue(index);
            _databaseMenuList.Delete(menu);
        }
    }

    private void saveMenuListToDatabase() {
        _logger.Debug("saveMenuListToDatabase");

        for (int index = 0; index < _menuList.getSize(); index++) {
            LucaMenu menu = _menuList.getValue(index);
            _databaseMenuList.CreateEntry(menu);
        }
    }

    private void sendFailedListedMenuDownloadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for listedmenu failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                ListedMenuDownloadFinishedBroadcast,
                ListedMenuDownloadFinishedBundle,
                new ListedMenuDownloadFinishedContent(_listedMenuList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedMenuDownloadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for menu failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MenuDownloadFinishedBroadcast,
                MenuDownloadFinishedBundle,
                new MenuDownloadFinishedContent(_menuList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedMenuUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update for menu failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MenuUpdateFinishedBroadcast,
                MenuUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedMenuClearBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Clear for menu failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MenuClearFinishedBroadcast,
                MenuClearFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void controlMenus() {
        _logger.Debug("controlMenus");

        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        for (int index = 0; index < _menuList.getSize(); index++) {
            LucaMenu menu = _menuList.getValue(index);
            _logger.Debug(String.format("Checking menu %s", menu.toString()));

            if (menu.GetDate().Year() < year) {
                _logger.Debug(String.format("Year of menu %s is lower then this year! Updating...", menu.toString()));
                menu = resetMenu(menu);
                if (menu == null) {
                    continue;
                }
                UpdateMenu(menu);
                continue;
            }

            if (menu.GetDate().Month() < month) {
                _logger.Debug(String.format("Month of menu %s is lower then this year! Updating...", menu.toString()));
                menu = resetMenu(menu);
                if (menu == null) {
                    continue;
                }
                UpdateMenu(menu);
                continue;
            }

            if (menu.GetDate().DayOfMonth() < dayOfMonth && menu.GetDate().Month() <= month) {
                _logger.Debug(String.format(
                        Locale.getDefault(),
                        "Day of menu %s is lower then this day and month is lower then this month! Updating...",
                        menu.toString()));
                menu = resetMenu(menu);
                if (menu == null) {
                    continue;
                }
                UpdateMenu(menu);
            }
        }
    }

    private LucaMenu resetMenu(@NonNull LucaMenu menu) {
        _logger.Debug(String.format("Resetting menu %s", menu.toString()));

        menu.SetTitle("-");
        menu.SetDescription("-");

        Calendar today = Calendar.getInstance();

        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        int menuDayOfWeek = menu.GetWeekday().GetInt();

        if (menuDayOfWeek <= 0) {
            _logger.Error("Day of week was not found!");
            return null;
        }

        int dayOfWeekDifference = menuDayOfWeek - dayOfWeek + 1;
        if (dayOfWeekDifference < 0) {
            dayOfWeekDifference += 7;
        }

        if (menu.GetDate().Year() < year || menu.GetDate().Month() < month || menu.GetDate().DayOfMonth() < dayOfMonth) {
            return calculateDate(menu, year, month, dayOfMonth, dayOfWeekDifference);
        }

        return null;
    }

    private LucaMenu calculateDate(
            @NonNull LucaMenu menu,
            int year,
            int month,
            int dayOfMonth,
            int dayOfWeekDifference) {
        _logger.Debug(String.format(Locale.getDefault(), "calculateDate for menu %s", menu));
        dayOfMonth += dayOfWeekDifference;

        switch (month - 1) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
                if (dayOfMonth > 31) {
                    dayOfMonth -= 31;
                    month++;
                }
                break;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                if (dayOfMonth > 30) {
                    dayOfMonth -= 30;
                    month++;
                }
                break;
            case Calendar.FEBRUARY:
                if (year % 4 == 0) {
                    if (dayOfMonth > 29) {
                        dayOfMonth -= 29;
                        month++;
                    }
                } else {
                    if (dayOfMonth > 28) {
                        dayOfMonth -= 28;
                        month++;
                    }
                }
                break;
            case Calendar.DECEMBER:
                if (dayOfMonth > 31) {
                    dayOfMonth -= 31;
                    month = 1;
                    year++;
                }
                break;
            default:
                _logger.Error(String.format(Locale.getDefault(), "Invalid month %d!", month));
                return null;
        }

        menu.SetDate(new SerializableDate(year, month, dayOfMonth));

        return menu;
    }

    private SerializableList<LucaMenu> notOnServerMenu() {
        SerializableList<LucaMenu> notOnServerMenuList = new SerializableList<>();

        for (int index = 0; index < _menuList.getSize(); index++) {
            if (!_menuList.getValue(index).GetIsOnServer()) {
                notOnServerMenuList.addValue(_menuList.getValue(index));
            }
        }

        return notOnServerMenuList;
    }

    private boolean hasMenuEntryNotOnServer() {
        return notOnServerMenu().getSize() > 0;
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
