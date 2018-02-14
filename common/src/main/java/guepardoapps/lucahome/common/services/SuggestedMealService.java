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
import guepardoapps.lucahome.common.classes.SuggestedMeal;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToSuggestedMealConverter;
import guepardoapps.lucahome.common.databases.DatabaseSuggestedMealList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class SuggestedMealService implements ISuggestedMealService {
    private static final String Tag = SuggestedMealService.class.getSimpleName();

    private static final SuggestedMealService Singleton = new SuggestedMealService();

    private static final int MinTimeoutMin = 15;
    private static final int MaxTimeoutMin = 24 * 60;

    private Context _context;

    private DatabaseSuggestedMealList _databaseSuggestedMealList;

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
                ArrayList<SuggestedMeal> suggestedMealList = JsonDataToSuggestedMealConverter.getInstance().GetList(contentResponse);
                if (suggestedMealList == null) {
                    Logger.getInstance().Error(Tag, "Converted suggestedMealList is null!");
                    _broadcastController.SendBooleanBroadcast(SuggestedMealDownloadFinishedBroadcast, SuggestedMealDownloadFinishedBundle, false);
                    return "";
                }

                _databaseSuggestedMealList.ClearDatabase();
                saveSuggestedMealListToDatabase(suggestedMealList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(SuggestedMealDownloadFinishedBroadcast, SuggestedMealDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _suggestedMealDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedMeal) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedMealDownloadFinishedBroadcast, SuggestedMealDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedMealDownloadFinishedBroadcast, SuggestedMealDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _suggestedMealAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedMealAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedMealAddFinishedBroadcast, SuggestedMealAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Adding was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedMealAddFinishedBroadcast, SuggestedMealAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(SuggestedMealAddFinishedBroadcast, SuggestedMealAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _suggestedMealUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedMealUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedMealUpdateFinishedBroadcast, SuggestedMealUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Update was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedMealUpdateFinishedBroadcast, SuggestedMealUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(SuggestedMealUpdateFinishedBroadcast, SuggestedMealUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _suggestedMealDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.SuggestedMealDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(SuggestedMealDeleteFinishedBroadcast, SuggestedMealDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Deleting was not successful!");
                _broadcastController.SendBooleanBroadcast(SuggestedMealDeleteFinishedBroadcast, SuggestedMealDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(SuggestedMealDeleteFinishedBroadcast, SuggestedMealDeleteFinishedBundle, true);
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

    private SuggestedMealService() {
    }

    public static SuggestedMealService getInstance() {
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

        _databaseSuggestedMealList = new DatabaseSuggestedMealList(_context);
        _databaseSuggestedMealList.Open();

        _receiverController.RegisterReceiver(_suggestedMealDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_suggestedMealAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_suggestedMealUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_suggestedMealDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseSuggestedMealList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<SuggestedMeal> GetDataList() {
        try {
            return _databaseSuggestedMealList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public SuggestedMeal GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseSuggestedMealList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseSuggestedMealList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new SuggestedMeal(uuid, "NULL", "NULL", 0, 0, new ArrayList<>(), false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<SuggestedMeal> SearchDataList(@NonNull String searchKey) {
        ArrayList<SuggestedMeal> suggestedMealList = GetDataList();
        ArrayList<SuggestedMeal> foundSuggestedMeals = new ArrayList<>();
        for (int index = 0; index < suggestedMealList.size(); index++) {
            SuggestedMeal entry = suggestedMealList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundSuggestedMeals.add(entry);
            }
        }
        return foundSuggestedMeals;
    }

    @Override
    public ArrayList<String> GetTitleList() {
        try {
            return _databaseSuggestedMealList.GetStringQueryList(true, DatabaseSuggestedMealList.KeyTitle, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<String> GetDescriptionList() {
        try {
            return _databaseSuggestedMealList.GetStringQueryList(true, DatabaseSuggestedMealList.KeyDescription, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(SuggestedMealDownloadFinishedBroadcast, SuggestedMealDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedMealDownloadFinishedBroadcast, SuggestedMealDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<SuggestedMeal> notOnServerSuggestedMealList = notOnServerEntries();
            for (int index = 0; index < notOnServerSuggestedMealList.size(); index++) {
                SuggestedMeal suggestedMeal = notOnServerSuggestedMealList.get(index);

                switch (suggestedMeal.GetServerDbAction()) {
                    case Update:
                        UpdateEntry(suggestedMeal);
                        break;
                    case Delete:
                        DeleteEntry(suggestedMeal);
                        break;
                    case Add:
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", suggestedMeal));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_SUGGESTED_MEALS.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Meal, true);
    }

    @Override
    public void AddEntry(@NonNull SuggestedMeal entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databaseSuggestedMealList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedMealAddFinishedBroadcast, SuggestedMealAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SuggestedMealAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull SuggestedMeal entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databaseSuggestedMealList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedMealUpdateFinishedBroadcast, SuggestedMealUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SuggestedMealUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull SuggestedMeal entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databaseSuggestedMealList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(SuggestedMealDeleteFinishedBroadcast, SuggestedMealDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.SuggestedMealDelete, true);
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
    public void ShareSuggestedMealList() {
        ArrayList<SuggestedMeal> suggestedMealList = GetDataList();

        StringBuilder shareText = new StringBuilder("Meal:\n");
        for (int index = 0; index < suggestedMealList.size(); index++) {
            SuggestedMeal entry = suggestedMealList.get(index);
            shareText.append(entry.GetTitle()).append("\n").append(entry.GetDescription()).append("\n\n");
        }

        Intent sendIntent = new Intent();

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        sendIntent.setType("text/plain");

        _context.startActivity(sendIntent);
    }

    private void saveSuggestedMealListToDatabase(@NonNull ArrayList<SuggestedMeal> suggestedMealList) {
        for (int index = 0; index < suggestedMealList.size(); index++) {
            SuggestedMeal suggestedMeal = suggestedMealList.get(index);
            _databaseSuggestedMealList.AddEntry(suggestedMeal);
        }
    }

    private ArrayList<SuggestedMeal> notOnServerEntries() {
        try {
            return _databaseSuggestedMealList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabaseSuggestedMealList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
