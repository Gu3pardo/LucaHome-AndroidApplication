package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToPuckJsConverter;
import guepardoapps.lucahome.common.database.DatabasePuckJsList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PuckJsListService implements IDataService {
    public static class PuckJsListDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<PuckJs> PuckJsList;

        PuckJsListDownloadFinishedContent(@NonNull SerializableList<PuckJs> puckJsList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            PuckJsList = puckJsList;
        }
    }

    public static final String PuckJsIntent = "PuckJsIntent";

    public static final String PuckJsListDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.puckjs.download.finished";
    public static final String PuckJsListDownloadFinishedBundle = "PuckJsListDownloadFinishedBundle";

    public static final String PuckJsListAddFinishedBroadcast = "guepardoapps.lucahome.data.service.puckjs.add.finished";
    public static final String PuckJsListAddFinishedBundle = "PuckJsListAddFinishedBundle";

    public static final String PuckJsListUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.puckjs.update.finished";
    public static final String PuckJsListUpdateFinishedBundle = "PuckJsListUpdateFinishedBundle";

    public static final String PuckJsListDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.puckjs.delete.finished";
    public static final String PuckJsListDeleteFinishedBundle = "PuckJsListDeleteFinishedBundle";

    private static final PuckJsListService SINGLETON = new PuckJsListService();
    private boolean _isInitialized;

    private static final String TAG = PuckJsListService.class.getSimpleName();

    private static final int MIN_TIMEOUT_MIN = 60;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private boolean _loadDataEnabled;

    private Date _lastUpdate;

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
                SerializableList<PuckJs> puckJsList = JsonDataToPuckJsConverter.getInstance().GetList(contentResponse);
                if (puckJsList == null) {
                    Logger.getInstance().Error(TAG, "Converted puckJsList is null!");
                    _puckJsList = _databasePuckJsList.GetPuckJsList();
                    sendFailedDownloadBroadcast("Converted puckJsList is null!");
                    return "";
                }

                _lastUpdate = new Date();

                _puckJsList = puckJsList;

                clearPuckJsListFromDatabase();
                savePuckJsListToDatabase();

                _broadcastController.SendSerializableBroadcast(
                        PuckJsListDownloadFinishedBroadcast,
                        PuckJsListDownloadFinishedBundle,
                        new PuckJsListDownloadFinishedContent(_puckJsList, true, Tools.CompressStringToByteArray("Download finished")));
            }
            return "Success";
        }
    }

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private DatabasePuckJsList _databasePuckJsList;

    private SerializableList<PuckJs> _puckJsList = new SerializableList<>();

    private BroadcastReceiver _puckJsListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.PuckJsList) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                _puckJsList = _databasePuckJsList.GetPuckJsList();
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _puckJsList = _databasePuckJsList.GetPuckJsList();
                sendFailedDownloadBroadcast("Download was not successful!");
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _puckJsListAddFinishedReceiver = new BroadcastReceiver() {
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
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedAddBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    PuckJsListAddFinishedBroadcast,
                    PuckJsListAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _puckJsListUpdateFinishedReceiver = new BroadcastReceiver() {
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
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    PuckJsListUpdateFinishedBroadcast,
                    PuckJsListUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _puckJsListDeleteFinishedReceiver = new BroadcastReceiver() {
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
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedDeleteBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    PuckJsListDeleteFinishedBroadcast,
                    PuckJsListDeleteFinishedBundle,
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

    private PuckJsListService() {
    }

    public static PuckJsListService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _lastUpdate = new Date();

        _loadDataEnabled = true;
        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);

        _databasePuckJsList = new DatabasePuckJsList(context);
        _databasePuckJsList.Open();

        _receiverController.RegisterReceiver(_puckJsListDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_puckJsListAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_puckJsListUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_puckJsListDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

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
    public SerializableList<PuckJs> GetDataList() {
        return _puckJsList;
    }

    public ArrayList<String> GetPuckJsNameList() {
        ArrayList<String> nameList = new ArrayList<>();
        for (int index = 0; index < _puckJsList.getSize(); index++) {
            nameList.add(_puckJsList.getValue(index).GetName());
        }
        return new ArrayList<>(nameList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetPuckJsAreaList() {
        ArrayList<String> areaList = new ArrayList<>();
        for (int index = 0; index < _puckJsList.getSize(); index++) {
            areaList.add(_puckJsList.getValue(index).GetArea());
        }
        return new ArrayList<>(areaList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetPuckJsMacList() {
        ArrayList<String> macList = new ArrayList<>();
        for (int index = 0; index < _puckJsList.getSize(); index++) {
            macList.add(_puckJsList.getValue(index).GetMac());
        }
        return new ArrayList<>(macList.stream().distinct().collect(Collectors.toList()));
    }

    public PuckJs GetById(int id) {
        for (int index = 0; index < _puckJsList.getSize(); index++) {
            PuckJs entry = _puckJsList.getValue(index);
            if (entry.GetId() == id) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int GetHighestId() {
        int highestId = -1;
        for (int index = 0; index < _puckJsList.getSize(); index++) {
            int id = _puckJsList.getValue(index).GetId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    @Override
    public SerializableList<PuckJs> SearchDataList(@NonNull String searchKey) {
        SerializableList<PuckJs> foundPuckJsList = new SerializableList<>();
        for (int index = 0; index < _puckJsList.getSize(); index++) {
            PuckJs entry = _puckJsList.getValue(index);
            if (entry.toString().contains(searchKey)) {
                foundPuckJsList.addValue(entry);
            }
        }
        return foundPuckJsList;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _puckJsList = _databasePuckJsList.GetPuckJsList();
            _broadcastController.SendSerializableBroadcast(
                    PuckJsListDownloadFinishedBroadcast,
                    PuckJsListDownloadFinishedBundle,
                    new PuckJsListDownloadFinishedContent(_puckJsList, true, Tools.CompressStringToByteArray("Loaded from database")));
            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user");
            return;
        }

        if (hasEntryNotOnServer()) {
            _loadDataEnabled = false;

            for (int index = 0; index < notOnServerPuckJs().getSize(); index++) {
                PuckJs puckJs = notOnServerPuckJs().getValue(index);

                switch (puckJs.GetServerDbAction()) {
                    case Add:
                        AddPuckJs(puckJs);
                        break;
                    case Update:
                        UpdatePuckJs(puckJs);
                        break;
                    case Delete:
                        DeletePuckJs(puckJs);
                        break;
                    case Null:
                    default:
                        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Nothing todo with %s.", puckJs));
                        break;
                }

            }

            _loadDataEnabled = true;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_PUCKJS_LIST.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJsList, true);
    }

    public void AddPuckJs(@NonNull PuckJs entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Add);

            _databasePuckJsList.CreateEntry(entry);

            LoadData();

            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJsAdd, true);
    }

    public void UpdatePuckJs(@NonNull PuckJs entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Update);

            _databasePuckJsList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJsUpdate, true);
    }

    public void DeletePuckJs(@NonNull PuckJs entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            entry.SetIsOnServer(false);
            entry.SetServerDbAction(ILucaClass.LucaServerDbAction.Delete);

            _databasePuckJsList.Update(entry);

            LoadData();

            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                SettingsController.getInstance().GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.PuckJsDelete, true);
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

    private void clearPuckJsListFromDatabase() {
        SerializableList<PuckJs> puckJsList = _databasePuckJsList.GetPuckJsList();
        for (int index = 0; index < puckJsList.getSize(); index++) {
            PuckJs puckJs = puckJsList.getValue(index);
            _databasePuckJsList.Delete(puckJs);
        }
    }

    private void savePuckJsListToDatabase() {
        for (int index = 0; index < _puckJsList.getSize(); index++) {
            PuckJs puckJs = _puckJsList.getValue(index);
            _databasePuckJsList.CreateEntry(puckJs);
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for puck js failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                PuckJsListDownloadFinishedBroadcast,
                PuckJsListDownloadFinishedBundle,
                new PuckJsListDownloadFinishedContent(_puckJsList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for puck js failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                PuckJsListAddFinishedBroadcast,
                PuckJsListAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update of puck js failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                PuckJsListUpdateFinishedBroadcast,
                PuckJsListUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for puck js failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                PuckJsListDeleteFinishedBroadcast,
                PuckJsListDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private SerializableList<PuckJs> notOnServerPuckJs() {
        SerializableList<PuckJs> notOnServerPuckJsList = new SerializableList<>();

        for (int index = 0; index < _puckJsList.getSize(); index++) {
            if (!_puckJsList.getValue(index).GetIsOnServer()) {
                notOnServerPuckJsList.addValue(_puckJsList.getValue(index));
            }
        }

        return notOnServerPuckJsList;
    }

    private boolean hasEntryNotOnServer() {
        return notOnServerPuckJs().getSize() > 0;
    }
}
