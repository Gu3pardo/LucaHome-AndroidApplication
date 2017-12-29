package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.MeterData;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMeterDataConverter;
import guepardoapps.lucahome.common.database.DatabaseMeterDataList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class MeterListService implements IDataService {
    public static class MeterDataListDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<MeterData> MeterDataList;

        MeterDataListDownloadFinishedContent(SerializableList<MeterData> meterDataList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MeterDataList = meterDataList;
        }
    }

    public static final String MeterDataIntent = "MeterDataIntent";

    public static final String MeterDataListDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.meterdatalist.download.finished";
    public static final String MeterDataListDownloadFinishedBundle = "MeterDataListDownloadFinishedBundle";

    public static final String MeterDataListAddFinishedBroadcast = "guepardoapps.lucahome.data.service.meterdatalist.add.finished";
    public static final String MeterDataListAddFinishedBundle = "MeterDataListAddFinishedBundle";

    public static final String MeterDataListUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.meterdatalist.update.finished";
    public static final String MeterDataListUpdateFinishedBundle = "MeterDataListUpdateFinishedBundle";

    public static final String MeterDataListDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.meterdatalist.delete.finished";
    public static final String MeterDataListDeleteFinishedBundle = "MeterDataListDeleteFinishedBundle";

    private static final MeterListService SINGLETON = new MeterListService();
    private boolean _isInitialized;

    private static final String TAG = MeterListService.class.getSimpleName();

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
            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private DatabaseMeterDataList _databaseMeterDataList;

    private SerializableList<MeterData> _meterDataList = new SerializableList<>();

    private BroadcastReceiver _meterDataListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterData) {
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                _meterDataList = _databaseMeterDataList.GetMeterDataList();
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _meterDataList = _databaseMeterDataList.GetMeterDataList();
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<MeterData> meterDataList = JsonDataToMeterDataConverter.getInstance().GetList(contentResponse);
            if (meterDataList == null) {
                Logger.getInstance().Error(TAG, "Converted meterDataList is null!");
                _meterDataList = _databaseMeterDataList.GetMeterDataList();
                sendFailedDownloadBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _meterDataList = meterDataList;

            clearMeterDataListFromDatabase();
            saveMeterDataListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    MeterDataListDownloadFinishedBroadcast,
                    MeterDataListDownloadFinishedBundle,
                    new MeterDataListDownloadFinishedContent(_meterDataList, true, content.Response));
        }
    };

    private BroadcastReceiver _meterDataListAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterDataAdd) {
                return;
            }

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
                    MeterDataListAddFinishedBroadcast,
                    MeterDataListAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _meterDataListUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterDataUpdate) {
                return;
            }

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
                    MeterDataListUpdateFinishedBroadcast,
                    MeterDataListUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _meterDataListDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterDataDelete) {
                return;
            }

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
                    MeterDataListDeleteFinishedBroadcast,
                    MeterDataListDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadData();
        }
    };

    private BroadcastReceiver _homeNetworkAvailableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            if (_reloadEnabled && _networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
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

    private MeterListService() {
    }

    public static MeterListService getInstance() {
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
        _settingsController = SettingsController.getInstance();
        _settingsController.Initialize(context);

        _databaseMeterDataList = new DatabaseMeterDataList(context);
        _databaseMeterDataList.Open();

        _receiverController.RegisterReceiver(_meterDataListDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterDataListAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterDataListUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterDataListDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMeterDataList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<MeterData> GetDataList() {
        return _meterDataList;
    }

    public ArrayList<String> GetMeterIdList() {
        ArrayList<String> meterIdList = new ArrayList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            meterIdList.add(_meterDataList.getValue(index).GetMeterId());
        }
        return meterIdList;
    }

    public ArrayList<String> GetMeterTypeList() {
        ArrayList<String> meterTypeList = new ArrayList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            meterTypeList.add(_meterDataList.getValue(index).GetType());
        }
        return meterTypeList;
    }

    public MeterData GetById(int id) {
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            MeterData entry = _meterDataList.getValue(index);
            if (entry.GetId() == id) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public SerializableList<MeterData> SearchDataList(@NonNull String searchKey) {
        SerializableList<MeterData> foundMeterDataList = new SerializableList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            MeterData entry = _meterDataList.getValue(index);
            if (String.valueOf(entry.toString()).contains(searchKey)) {
                foundMeterDataList.addValue(entry);
            }
        }
        return foundMeterDataList;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _meterDataList = _databaseMeterDataList.GetMeterDataList();
            _broadcastController.SendSerializableBroadcast(
                    MeterDataListDownloadFinishedBroadcast,
                    MeterDataListDownloadFinishedBundle,
                    new MeterDataListDownloadFinishedContent(_meterDataList, true, Tools.CompressStringToByteArray("Loaded from database!")));
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
                + "&action=" + LucaServerAction.GET_METER_DATA.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterData, true);
    }

    public void AddMeterData(@NonNull MeterData entry) {
        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            sendFailedAddBroadcast("No home network");
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterDataAdd, true);
    }

    public void UpdateMeterData(@NonNull MeterData entry) {
        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            sendFailedUpdateBroadcast("No home network");
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterDataUpdate, true);
    }

    public void DeleteMeterData(@NonNull MeterData entry) {
        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            sendFailedDeleteBroadcast("No home network");
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterDataDelete, true);
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

    private void clearMeterDataListFromDatabase() {
        SerializableList<MeterData> meterDataList = _databaseMeterDataList.GetMeterDataList();
        for (int index = 0; index < meterDataList.getSize(); index++) {
            MeterData meterData = meterDataList.getValue(index);
            _databaseMeterDataList.Delete(meterData);
        }
    }

    private void saveMeterDataListToDatabase() {
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            MeterData meterData = _meterDataList.getValue(index);
            _databaseMeterDataList.CreateEntry(meterData);
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for meter data failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MeterDataListDownloadFinishedBroadcast,
                MeterDataListDownloadFinishedBundle,
                new MeterDataListDownloadFinishedContent(_meterDataList, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for meter data failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MeterDataListAddFinishedBroadcast,
                MeterDataListAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update of meter data failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MeterDataListUpdateFinishedBroadcast,
                MeterDataListUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for meter data failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MeterDataListDeleteFinishedBroadcast,
                MeterDataListDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
