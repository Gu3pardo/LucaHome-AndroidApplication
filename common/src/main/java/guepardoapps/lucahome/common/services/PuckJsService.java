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
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToPuckJsConverter;
import guepardoapps.lucahome.common.databases.DatabasePuckJsList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class PuckJsService implements IPuckJsService {
    private static final String Tag = PuckJsService.class.getSimpleName();

    private static final PuckJsService Singleton = new PuckJsService();

    private static final int MinTimeoutMin = 15;
    private static final int MaxTimeoutMin = 24 * 60;

    private DatabasePuckJsList _databasePuckJsList;

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
                ArrayList<PuckJs> puckJsList = JsonDataToPuckJsConverter.getInstance().GetList(contentResponse);
                if (puckJsList == null) {
                    Logger.getInstance().Error(Tag, "Converted puckJsList is null!");
                    _broadcastController.SendBooleanBroadcast(PuckJsDownloadFinishedBroadcast, PuckJsDownloadFinishedBundle, false);
                    return "";
                }

                _databasePuckJsList.ClearDatabase();
                savePuckJsListToDatabase(puckJsList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(PuckJsDownloadFinishedBroadcast, PuckJsDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _puckJsDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.PuckJs) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(PuckJsDownloadFinishedBroadcast, PuckJsDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(PuckJsDownloadFinishedBroadcast, PuckJsDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _puckJsAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.PuckJsAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(PuckJsAddFinishedBroadcast, PuckJsAddFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Adding was not successful!");
                _broadcastController.SendBooleanBroadcast(PuckJsAddFinishedBroadcast, PuckJsAddFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(PuckJsAddFinishedBroadcast, PuckJsAddFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _puckJsUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.PuckJsUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(PuckJsUpdateFinishedBroadcast, PuckJsUpdateFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Update was not successful!");
                _broadcastController.SendBooleanBroadcast(PuckJsUpdateFinishedBroadcast, PuckJsUpdateFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(PuckJsUpdateFinishedBroadcast, PuckJsUpdateFinishedBundle, true);
            LoadData();
        }
    };

    private BroadcastReceiver _puckJsDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.PuckJsDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(PuckJsDeleteFinishedBroadcast, PuckJsDeleteFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Deleting was not successful!");
                _broadcastController.SendBooleanBroadcast(PuckJsDeleteFinishedBroadcast, PuckJsDeleteFinishedBundle, false);
                return;
            }

            _broadcastController.SendBooleanBroadcast(PuckJsDeleteFinishedBroadcast, PuckJsDeleteFinishedBundle, true);
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

    private PuckJsService() {
    }

    public static PuckJsService getInstance() {
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

        _databasePuckJsList = new DatabasePuckJsList(context);
        _databasePuckJsList.Open();

        _receiverController.RegisterReceiver(_puckJsDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_puckJsAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_puckJsUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_puckJsDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databasePuckJsList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<PuckJs> GetDataList() {
        try {
            return _databasePuckJsList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public PuckJs GetByUuid(@NonNull UUID uuid) {
        try {
            return _databasePuckJsList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabasePuckJsList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new PuckJs(uuid, UUID.randomUUID(), "NULL", "NULL", 0, 0, false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<PuckJs> SearchDataList(@NonNull String searchKey) {
        ArrayList<PuckJs> puckJsList = GetDataList();
        ArrayList<PuckJs> foundPuckJs = new ArrayList<>();
        for (int index = 0; index < puckJsList.size(); index++) {
            PuckJs entry = puckJsList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundPuckJs.add(entry);
            }
        }
        return foundPuckJs;
    }

    @Override
    public ArrayList<String> GetNameList() {
        try {
            return _databasePuckJsList.GetStringQueryList(true, DatabasePuckJsList.KeyName, null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<UUID> GetRoomUuidList() {
        try {
            ArrayList<String> roomUuidStringList = _databasePuckJsList.GetStringQueryList(true, DatabasePuckJsList.KeyUuid, null, null, null);
            ArrayList<UUID> roomUuidList = new ArrayList<>();
            for (String roomUuidString : roomUuidStringList) {
                roomUuidList.add(UUID.fromString(roomUuidString));
            }
            return roomUuidList;
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<String> GetMacList() {
        try {
            return _databasePuckJsList.GetStringQueryList(true, DatabasePuckJsList.KeyMac, null, null, null);
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
            _broadcastController.SendBooleanBroadcast(PuckJsDownloadFinishedBroadcast, PuckJsDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(PuckJsDownloadFinishedBroadcast, PuckJsDownloadFinishedBundle, false);
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            ArrayList<PuckJs> notOnServerPuckJsList = notOnServerEntries();
            for (int index = 0; index < notOnServerPuckJsList.size(); index++) {
                PuckJs puckJs = notOnServerPuckJsList.get(index);

                switch (puckJs.GetServerDbAction()) {
                    case Update:
                        UpdateEntry(puckJs);
                        break;
                    case Delete:
                        DeleteEntry(puckJs);
                        break;
                    case Add:
                    case Null:
                    default:
                        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Nothing todo with %s.", puckJs));
                        break;
                }
            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_PUCK_JS_LIST.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJs, true);
    }

    @Override
    public void AddEntry(@NonNull PuckJs entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);
            _databasePuckJsList.AddEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(PuckJsAddFinishedBroadcast, PuckJsAddFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJsAdd, true);
    }

    @Override
    public void UpdateEntry(@NonNull PuckJs entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);
            _databasePuckJsList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(PuckJsUpdateFinishedBroadcast, PuckJsUpdateFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJsUpdate, true);
    }

    @Override
    public void DeleteEntry(@NonNull PuckJs entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            if (!entry.GetIsOnServer()) {
                Logger.getInstance().Warning(Tag, "Already action performed which is not yet on server!");
                return;
            }

            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);
            _databasePuckJsList.UpdateEntry(entry);

            LoadData();
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(PuckJsDeleteFinishedBroadcast, PuckJsDeleteFinishedBundle, false);
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ActionPath,
                user.GetName(), user.GetPassphrase(),
                entry.GetCommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJsDelete, true);
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

    private void savePuckJsListToDatabase(@NonNull ArrayList<PuckJs> puckJsList) {
        for (int index = 0; index < puckJsList.size(); index++) {
            PuckJs puckJs = puckJsList.get(index);
            _databasePuckJsList.AddEntry(puckJs);
        }
    }

    private ArrayList<PuckJs> notOnServerEntries() {
        try {
            return _databasePuckJsList.GetList(String.format(Locale.getDefault(), "%s like %d", DatabasePuckJsList.KeyIsOnServer, 0), null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerEntries().size() > 0;
    }
}
