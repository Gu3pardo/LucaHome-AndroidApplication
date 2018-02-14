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
import guepardoapps.lucahome.common.classes.SuggestedShoppingItem;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToSuggestedShoppingItemConverter;
import guepardoapps.lucahome.common.databases.DatabaseSuggestedShoppingItemList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.enums.ShoppingItemType;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class SuggestedShoppingItemService implements ISuggestedShoppingItemService {
    private static final String Tag = SuggestedShoppingItemService.class.getSimpleName();

    private static final SuggestedShoppingItemService Singleton = new SuggestedShoppingItemService();

    private static final int MinTimeoutMin = 60;
    private static final int MaxTimeoutMin = 24 * 60;

    private DatabaseSuggestedShoppingItemList _databaseSuggestedShoppingItemList;

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
                ArrayList<SuggestedShoppingItem> suggestedShoppingItemList = JsonDataToSuggestedShoppingItemConverter.getInstance().GetList(contentResponse);
                if (suggestedShoppingItemList == null) {
                    Logger.getInstance().Error(Tag, "Converted shoppingItemList is null!");
                    _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDownloadFinishedBroadcast, SuggestedShoppingItemDownloadFinishedBundle, false);
                    return "";
                }

                _databaseSuggestedShoppingItemList.ClearDatabase();
                saveSuggestedShoppingItemListToDatabase(suggestedShoppingItemList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDownloadFinishedBroadcast, SuggestedShoppingItemDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _suggestedShoppingItemDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedShoppingItem) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDownloadFinishedBroadcast, SuggestedShoppingItemDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDownloadFinishedBroadcast, SuggestedShoppingItemDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _suggestedShoppingItemAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedShoppingItemAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemAddFinishedBroadcast, SuggestedShoppingItemAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Adding was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemAddFinishedBroadcast, SuggestedShoppingItemAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemAddFinishedBroadcast, SuggestedShoppingItemAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _suggestedShoppingItemUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedShoppingItemUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemUpdateFinishedBroadcast, SuggestedShoppingItemUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Updating was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemUpdateFinishedBroadcast, SuggestedShoppingItemUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemUpdateFinishedBroadcast, SuggestedShoppingItemUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _suggestedShoppingItemDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedShoppingItemDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDeleteFinishedBroadcast, SuggestedShoppingItemDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Deleting was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDeleteFinishedBroadcast, SuggestedShoppingItemDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDeleteFinishedBroadcast, SuggestedShoppingItemDeleteFinishedBundle, true);
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

    private SuggestedShoppingItemService() {
    }

    public static SuggestedShoppingItemService getInstance() {
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

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);

        _databaseSuggestedShoppingItemList = new DatabaseSuggestedShoppingItemList(context);
        _databaseSuggestedShoppingItemList.Open();

        _receiverController.RegisterReceiver(_suggestedShoppingItemDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_suggestedShoppingItemAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_suggestedShoppingItemUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_suggestedShoppingItemDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseSuggestedShoppingItemList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<SuggestedShoppingItem> GetDataList() {
        try {
            return _databaseSuggestedShoppingItemList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public SuggestedShoppingItem GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseSuggestedShoppingItemList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseSuggestedShoppingItemList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new SuggestedShoppingItem(uuid, "NULL", ShoppingItemType.OTHER, 0, "NULL", false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<SuggestedShoppingItem> SearchDataList(@NonNull String searchKey) {
        ArrayList<SuggestedShoppingItem> suggestedShoppingItemList = GetDataList();
        ArrayList<SuggestedShoppingItem> foundSuggestedShoppingItemList = new ArrayList<>();
        for (int index = 0; index < suggestedShoppingItemList.size(); index++) {
            SuggestedShoppingItem entry = suggestedShoppingItemList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundSuggestedShoppingItemList.add(entry);
            }
        }
        return foundSuggestedShoppingItemList;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDownloadFinishedBroadcast, SuggestedShoppingItemDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDownloadFinishedBroadcast, SuggestedShoppingItemDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<SuggestedShoppingItem> notOnServerSuggestedShoppingItemList = notOnServerEntries();
            for (int index = 0; index < notOnServerSuggestedShoppingItemList.size(); index++) {
                SuggestedShoppingItem suggestedShoppingItem = notOnServerSuggestedShoppingItemList.get(index);

                switch (suggestedShoppingItem.GetServerDbAction()) {
                    case Add:
                        AddEntry(suggestedShoppingItem);
                        break;
                    case Update:
                        UpdateEntry(suggestedShoppingItem);
                        break;
                    case Delete:
                        DeleteEntry(suggestedShoppingItem);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", suggestedShoppingItem));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_SUGGESTED_SHOPPING_ITEMS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SuggestedShoppingItem, true);
    }

    @Override
    public void AddEntry(@NonNull SuggestedShoppingItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseSuggestedShoppingItemList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemAddFinishedBroadcast, SuggestedShoppingItemAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SuggestedShoppingItemAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull SuggestedShoppingItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseSuggestedShoppingItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemUpdateFinishedBroadcast, SuggestedShoppingItemUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SuggestedShoppingItemUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull SuggestedShoppingItem entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseSuggestedShoppingItemList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedShoppingItemDeleteFinishedBroadcast, SuggestedShoppingItemDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SuggestedShoppingItemDelete, true);
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

    private void saveSuggestedShoppingItemListToDatabase(@NonNull ArrayList<SuggestedShoppingItem> suggestedShoppingItemList) {
        for (int index = 0; index < suggestedShoppingItemList.size(); index++) {
            SuggestedShoppingItem suggestedShoppingItem = suggestedShoppingItemList.get(index);
            _databaseSuggestedShoppingItemList.AddEntry(suggestedShoppingItem);
        }
    }

    private ArrayList<SuggestedShoppingItem> notOnServerEntries() {
        try {
            return _databaseSuggestedShoppingItemList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseSuggestedShoppingItemList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
