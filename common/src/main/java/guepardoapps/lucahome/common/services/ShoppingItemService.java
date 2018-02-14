package guepardoapps.lucahome.common.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.ShoppingItem;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToShoppingItemConverter;
import guepardoapps.lucahome.common.databases.DatabaseShoppingItemList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.enums.ShoppingItemType;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class ShoppingItemService implements IShoppingItemService {
    private static final String Tag = ShoppingItemService.class.getSimpleName();

    private static final ShoppingItemService Singleton = new ShoppingItemService();

    private static final int MinTimeoutMin = 60;
    private static final int MaxTimeoutMin = 24 * 60;

    private Context _context;

    private DatabaseShoppingItemList _databaseShoppingItemList;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private Calendar _lastUpdate;

    private boolean _isInitialized;

    private boolean _loadDataEnabled;

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

    private class AsyncConverterTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            for (String contentResponse : strings) {
                ArrayList<ShoppingItem> shoppingItemList = JsonDataToShoppingItemConverter.getInstance().GetList(contentResponse);
                if (shoppingItemList == null) {
                    Logger.getInstance().Error(Tag, "Converted shoppingItemList is null!");
                    _broadcastController.SendBooleanBroadcast(ShoppingItemDownloadFinishedBroadcast, ShoppingItemDownloadFinishedBundle, false);
                    return "";
                }

                _databaseShoppingItemList.ClearDatabase();
                saveShoppingItemListToDatabase(shoppingItemList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(ShoppingItemDownloadFinishedBroadcast, ShoppingItemDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _shoppingItemDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingItem) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(ShoppingItemDownloadFinishedBroadcast, ShoppingItemDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(ShoppingItemDownloadFinishedBroadcast, ShoppingItemDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _shoppingItemAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingItemAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(ShoppingItemAddFinishedBroadcast, ShoppingItemAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Adding was not successful!");
                _broadcastController.SendBooleanBroadcast(ShoppingItemAddFinishedBroadcast, ShoppingItemAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(ShoppingItemAddFinishedBroadcast, ShoppingItemAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _shoppingItemUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingItemUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(ShoppingItemUpdateFinishedBroadcast, ShoppingItemUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Updating was not successful!");
                _broadcastController.SendBooleanBroadcast(ShoppingItemUpdateFinishedBroadcast, ShoppingItemUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(ShoppingItemUpdateFinishedBroadcast, ShoppingItemUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _shoppingItemDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ShoppingItemDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(ShoppingItemDeleteFinishedBroadcast, ShoppingItemDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Deleting was not successful!");
                _broadcastController.SendBooleanBroadcast(ShoppingItemDeleteFinishedBroadcast, ShoppingItemDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(ShoppingItemDeleteFinishedBroadcast, ShoppingItemDeleteFinishedBundle, true);
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

    private ShoppingItemService() {
    }

    public static ShoppingItemService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _loadDataEnabled = true;

        _lastUpdate = Calendar.getInstance();

        _reloadEnabled = reloadEnabled;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _downloadController = new DownloadController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);

        _databaseShoppingItemList = new DatabaseShoppingItemList(_context);
        _databaseShoppingItemList.Open();

        _receiverController.RegisterReceiver(_shoppingItemDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingItemAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingItemUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingItemDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseShoppingItemList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<ShoppingItem> GetDataList() {
        try {
            return _databaseShoppingItemList.GetList(null, null, DatabaseShoppingItemList.KeyBought + " ASC");
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<String> GetDetailList() {
        ArrayList<String> detailList = new ArrayList<>();
        for (ShoppingItem shoppingItem : GetDataList()) {
            detailList.add(String.format(Locale.getDefault(), "%dx %s", shoppingItem.GetQuantity(), shoppingItem.GetName()));
        }
        return detailList;
    }

    @Override
    public ShoppingItem GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseShoppingItemList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseShoppingItemList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new ShoppingItem(uuid, "NULL", ShoppingItemType.OTHER, 0, "NULL", false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<ShoppingItem> SearchDataList(@NonNull String searchKey) {
        ArrayList<ShoppingItem> shoppingItemList = GetDataList();
        ArrayList<ShoppingItem> foundShoppingItemList = new ArrayList<>();
        for (int index = 0; index < shoppingItemList.size(); index++) {
            ShoppingItem entry = shoppingItemList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundShoppingItemList.add(entry);
            }
        }
        return foundShoppingItemList;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(ShoppingItemDownloadFinishedBroadcast, ShoppingItemDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(ShoppingItemDownloadFinishedBroadcast, ShoppingItemDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<ShoppingItem> notOnServerShoppingItemList = notOnServerEntries();
            for (int index = 0; index < notOnServerShoppingItemList.size(); index++) {
                ShoppingItem shoppingItem = notOnServerShoppingItemList.get(index);

                switch (shoppingItem.GetServerDbAction()) {
                    case Add:
                        AddEntry(shoppingItem);
                        break;
                    case Update:
                        UpdateEntry(shoppingItem);
                        break;
                    case Delete:
                        DeleteEntry(shoppingItem);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", shoppingItem));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_SHOPPING_LIST.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingItem, true);
    }

    @Override
    public void AddEntry(@NonNull ShoppingItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseShoppingItemList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(ShoppingItemAddFinishedBroadcast, ShoppingItemAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingItemAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull ShoppingItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseShoppingItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(ShoppingItemUpdateFinishedBroadcast, ShoppingItemUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingItemUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull ShoppingItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseShoppingItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(ShoppingItemDeleteFinishedBroadcast, ShoppingItemDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingItemDelete, true);
    }

    @Override
    public void ClearShoppingList() {
        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(ShoppingItemDeleteFinishedBroadcast, ShoppingItemDeleteFinishedBundle, false);
            return;
        }

        ArrayList<ShoppingItem> shoppingItemList = GetDataList();
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            for (int index = 0; index < shoppingItemList.size(); index++) {
                ShoppingItem deleteEntry = shoppingItemList.get(index);
                deleteEntry.SetIsOnServer(false);
                deleteEntry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
                _databaseShoppingItemList.UpdateEntry(deleteEntry);
            }

            _broadcastController.SendBooleanBroadcast(ShoppingItemDeleteFinishedBroadcast, ShoppingItemDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.CLEAR_SHOPPING_LIST.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ShoppingItemDelete, true);
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
    public boolean GetReloadEnabled() {
        return _reloadEnabled;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        if (reloadTimeout < MinTimeoutMin) {
            reloadTimeout = MinTimeoutMin;
        }
        if (reloadTimeout > MaxTimeoutMin) {
            reloadTimeout = MaxTimeoutMin;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
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
    public Calendar GetLastUpdate() {
        return _lastUpdate;
    }

    @Override
    public void ShowNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method ShowNotification not implemented for " + Tag);
    }

    @Override
    public void CloseNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method CloseNotification not implemented for " + Tag);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public boolean GetDisplayNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReceiverActivity not implemented for " + Tag);
    }

    @Override
    public Class<?> GetReceiverActivity() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReceiverActivity not implemented for " + Tag);
    }

    @Override
    public void ShareShoppingItemList() {
        ArrayList<ShoppingItem> shoppingItemList = GetDataList();

        StringBuilder shareText = new StringBuilder("ShoppingItemList:\n");
        for (int index = 0; index < shoppingItemList.size(); index++) {
            ShoppingItem entry = shoppingItemList.get(index);
            shareText.append(String.valueOf(entry.GetQuantity())).append("x ").append(entry.GetName()).append("\n");
        }

        Intent sendIntent = new Intent();

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        sendIntent.setType("text/plain");

        _context.startActivity(sendIntent);
    }

    private void saveShoppingItemListToDatabase(@NonNull ArrayList<ShoppingItem> shoppingItemList) {
        for (int index = 0; index < shoppingItemList.size(); index++) {
            ShoppingItem shoppingItem = shoppingItemList.get(index);
            _databaseShoppingItemList.AddEntry(shoppingItem);
        }
    }

    private ArrayList<ShoppingItem> notOnServerEntries() {
        try {
            return _databaseShoppingItemList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseShoppingItemList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
