package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
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
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class MenuService {
    public static class ListedMenuDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<ListedMenu> ListedMenuList;

        public ListedMenuDownloadFinishedContent(SerializableList<ListedMenu> listedMenuList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            ListedMenuList = listedMenuList;
        }
    }

    public static class MenuDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<LucaMenu> MenuList;

        public MenuDownloadFinishedContent(SerializableList<LucaMenu> menuList, boolean succcess, @NonNull byte[] response) {
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

    private static final int TIMEOUT_MS = 3 * 60 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadListedMenuList();
            LoadMenuList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

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
                sendFailedListedMenuDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedListedMenuDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<ListedMenu> listedMenuList = _jsonDataToListedMenuConverter.GetList(contentResponse);
            if (listedMenuList == null) {
                _logger.Error("Converted listedMenuList is null!");
                sendFailedListedMenuDownloadBroadcast(contentResponse);
                return;
            }

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
                sendFailedMenuDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedMenuDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<LucaMenu> menuList = _jsonDataToMenuConverter.GetList(contentResponse);
            if (menuList == null) {
                _logger.Error("Converted menuList is null!");
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

            ControlMenus();

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

            _broadcastController.SendSerializableBroadcast(
                    MenuUpdateFinishedBroadcast,
                    MenuUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadMenuList();
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

            _broadcastController.SendSerializableBroadcast(
                    MenuClearFinishedBroadcast,
                    MenuClearFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadMenuList();
        }
    };

    private MenuService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static MenuService getInstance() {
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

        _databaseListedMenuList = new DatabaseListedMenuList(context);
        _databaseListedMenuList.Open();
        _databaseMenuList = new DatabaseMenuList(context);
        _databaseMenuList.Open();

        _receiverController.RegisterReceiver(_listedMenuDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuClearFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToListedMenuConverter = new JsonDataToListedMenuConverter();
        _jsonDataToMenuConverter = new JsonDataToMenuConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

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

    public SerializableList<LucaMenu> GetMenuList() {
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

    public SerializableList<LucaMenu> FoundMenus(@NonNull String searchKey) {
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
                + "&action=" + LucaServerAction.GET_LISTED_MENU.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ListedMenu, true);
    }

    public void LoadMenuList() {
        _logger.Debug("LoadMenuList");

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

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedMenuClearBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandClear());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MenuClear, true);
    }

    public void ControlMenus() {
        _logger.Debug("ControlMenus");

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
        _broadcastController.SendSerializableBroadcast(
                ListedMenuDownloadFinishedBroadcast,
                ListedMenuDownloadFinishedBundle,
                new ListedMenuDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedMenuDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MenuDownloadFinishedBroadcast,
                MenuDownloadFinishedBundle,
                new MenuDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedMenuUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MenuUpdateFinishedBroadcast,
                MenuUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedMenuClearBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MenuClearFinishedBroadcast,
                MenuClearFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private LucaMenu resetMenu(@NonNull LucaMenu menu) {
        _logger.Debug(String.format("Resetting menu %s", menu.toString()));

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

        if (menu.GetDate().Year() < year) {
            return calculateDate(menu, year, month, dayOfMonth, dayOfWeekDifference);
        }

        if (menu.GetDate().Month() < month) {
            return calculateDate(menu, year, month, dayOfMonth, dayOfWeekDifference);
        }

        if (menu.GetDate().DayOfMonth() < dayOfMonth) {
            return calculateDate(menu, year, month, dayOfMonth, dayOfWeekDifference);
        }

        menu.SetTitle("-");
        menu.SetDescription("-");

        _logger.Debug(String.format(Locale.getDefault(), "Menu was reset: %s", menu));

        return menu;
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
                return null;
        }

        menu.SetDate(new SerializableDate(year, month, dayOfMonth));

        return menu;
    }
}