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
import guepardoapps.lucahome.common.classes.Meter;
import guepardoapps.lucahome.common.classes.MeterData;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMeterDataConverter;
import guepardoapps.lucahome.common.database.DatabaseMeterDataList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MeterListService implements IDataService {
    public static class MeterDataListDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<MeterData> MeterDataList;

        MeterDataListDownloadFinishedContent(@NonNull SerializableList<MeterData> meterDataList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MeterDataList = meterDataList;
        }
    }

    public static class MeterListDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Meter> MeterList;

        MeterListDownloadFinishedContent(@NonNull SerializableList<Meter> meterList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MeterList = meterList;
        }
    }

    public static final String MeterDataIntent = "MeterDataIntent";

    public static final String MeterListDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.meterlist.download.finished";
    public static final String MeterListDownloadFinishedBundle = "MeterListDownloadFinishedBundle";

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
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private class AsyncConverterTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            for (String contentResponse : strings) {
                SerializableList<MeterData> meterDataList = JsonDataToMeterDataConverter.getInstance().GetList(contentResponse);
                if (meterDataList == null) {
                    Logger.getInstance().Error(TAG, "Converted meterDataList is null!");
                    _meterDataList = _databaseMeterDataList.GetMeterDataList();
                    sendFailedDownloadBroadcast("Converted meterDataList is null!");
                    createMeterList();
                    sendFailedDownloadMeterBroadcast("Converted meterDataList is null!");
                    return "";
                }

                _lastUpdate = new Date();

                _meterDataList = meterDataList;
                createMeterList();

                clearMeterDataListFromDatabase();
                saveMeterDataListToDatabase();

                _broadcastController.SendSerializableBroadcast(
                        MeterDataListDownloadFinishedBroadcast,
                        MeterDataListDownloadFinishedBundle,
                        new MeterDataListDownloadFinishedContent(_meterDataList, true, Tools.CompressStringToByteArray("Download finished")));
            }
            return "Success";
        }
    }

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private DatabaseMeterDataList _databaseMeterDataList;

    private Meter _activeMeter;
    private SerializableList<Meter> _meterList = new SerializableList<>();
    private SerializableList<MeterData> _meterDataList = new SerializableList<>();

    private BroadcastReceiver _meterDataListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterData) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                _meterDataList = _databaseMeterDataList.GetMeterDataList();
                sendFailedDownloadBroadcast(contentResponse);
                createMeterList();
                sendFailedDownloadMeterBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _meterDataList = _databaseMeterDataList.GetMeterDataList();
                sendFailedDownloadBroadcast("Download was not successful!");
                createMeterList();
                sendFailedDownloadMeterBroadcast("Download was not successful!");
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
        }
    };

    private BroadcastReceiver _meterDataListAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterDataAdd) {
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
                    MeterDataListAddFinishedBroadcast,
                    MeterDataListAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _meterDataListUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterDataUpdate) {
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
                    MeterDataListUpdateFinishedBroadcast,
                    MeterDataListUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _meterDataListDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MeterDataDelete) {
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
                    MeterDataListDeleteFinishedBroadcast,
                    MeterDataListDeleteFinishedBundle,
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

    public SerializableList<Meter> GetMeterList() {
        return _meterList;
    }

    public Meter GetActiveMeter() {
        if (_activeMeter == null) {
            _activeMeter = _meterList.getSize() > 0 ? _meterList.getValue(0) : new Meter(-1, "Error", "Error", "Error", new SerializableList<>());
        }
        return _activeMeter;
    }

    public void SetActiveMeter(@NonNull Meter meter) {
        _activeMeter = meter;
    }

    public void SetActiveMeter(@NonNull String meterId) {
        for (int index = 0; index < _meterList.getSize(); index++) {
            if (_meterList.getValue(index).GetMeterId().equals(meterId)) {
                _activeMeter = _meterList.getValue(index);
                break;
            }
        }
    }

    public ArrayList<String> GetMeterIdList() {
        ArrayList<String> meterIdList = new ArrayList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            meterIdList.add(_meterDataList.getValue(index).GetMeterId());
        }
        return new ArrayList<>(meterIdList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetMeterTypeList() {
        ArrayList<String> meterTypeList = new ArrayList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            meterTypeList.add(_meterDataList.getValue(index).GetType());
        }
        return new ArrayList<>(meterTypeList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetAreaList() {
        ArrayList<String> areaList = new ArrayList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            areaList.add(_meterDataList.getValue(index).GetArea());
        }
        return new ArrayList<>(areaList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetImageNameList() {
        ArrayList<String> imageNameList = new ArrayList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            imageNameList.add(_meterDataList.getValue(index).GetImageName());
        }
        return new ArrayList<>(imageNameList.stream().distinct().collect(Collectors.toList()));
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

    public Meter GetByTypeId(int typeId) {
        for (int index = 0; index < _meterList.getSize(); index++) {
            Meter entry = _meterList.getValue(index);
            if (entry.GetTypeId() == typeId) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int GetHighestId() {
        int highestId = -1;
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            int id = _meterDataList.getValue(index).GetId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    public int GetHighestTypeId(@NonNull String type) {
        int highestTypeId = -1;
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            MeterData meterData = _meterDataList.getValue(index);
            if (meterData.GetType().equals(type)) {
                int typeId = meterData.GetTypeId();
                if (typeId > highestTypeId) {
                    highestTypeId = typeId;
                }
            }
        }
        return highestTypeId;
    }

    @Override
    public SerializableList<MeterData> SearchDataList(@NonNull String searchKey) {
        SerializableList<MeterData> foundMeterDataList = new SerializableList<>();
        for (int index = 0; index < _meterDataList.getSize(); index++) {
            MeterData entry = _meterDataList.getValue(index);
            if (entry.toString().contains(searchKey)) {
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

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _meterDataList = _databaseMeterDataList.GetMeterDataList();
            createMeterList();
            _broadcastController.SendSerializableBroadcast(
                    MeterDataListDownloadFinishedBroadcast,
                    MeterDataListDownloadFinishedBundle,
                    new MeterDataListDownloadFinishedContent(_meterDataList, true, Tools.CompressStringToByteArray("Loaded from database")));
            return;
        }

        LucaUser user = SettingsController.getInstance().GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast("No user!");
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_METER_DATA.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterData, true);
    }

    public void AddMeterData(@NonNull MeterData entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            sendFailedAddBroadcast("No home network");
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

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterDataAdd, true);
    }

    public void UpdateMeterData(@NonNull MeterData entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            sendFailedUpdateBroadcast("No home network");
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

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MeterDataUpdate, true);
    }

    public void DeleteMeterData(@NonNull MeterData entry) {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            sendFailedDeleteBroadcast("No home network");
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

    private void createMeterList() {
        SerializableList<Meter> meterList = new SerializableList<>();

        for (int index = 0; index < _meterDataList.getSize(); index++) {
            MeterData meterData = _meterDataList.getValue(index);
            int typeId = meterData.GetTypeId();

            boolean meterAlreadyExists = false;
            int meterIndex;
            for (meterIndex = 0; meterIndex < meterList.getSize(); meterIndex++) {
                Meter meter = meterList.getValue(meterIndex);
                if (meter != null) {
                    if (meter.GetTypeId() == typeId) {
                        meterAlreadyExists = true;
                        break;
                    }
                }
            }

            if (meterAlreadyExists) {
                Meter meter = meterList.getValue(meterIndex);
                SerializableList<MeterData> meterDataList = meter.GetMeterDataList();
                meterDataList.addValue(meterData);
                meter.SetMeterDataList(meterDataList);
                meterList.setValue(meterIndex, meter);
            } else {
                SerializableList<MeterData> meterDataList = new SerializableList<>();
                meterDataList.addValue(meterData);
                Meter meter = new Meter(meterData.GetTypeId(), meterData.GetType(), meterData.GetMeterId(), meterData.GetArea(), meterDataList);
                meterList.addValue(meter);
            }
        }

        _meterList = meterList;
        _activeMeter = _meterList.getSize() > 0 ? _meterList.getValue(0) : new Meter(-1, "Error", "Error", "Error", new SerializableList<>());

        _broadcastController.SendSerializableBroadcast(
                MeterListDownloadFinishedBroadcast,
                MeterListDownloadFinishedBundle,
                new MeterListDownloadFinishedContent(_meterList, true, Tools.CompressStringToByteArray("Conversion succeeded")));
    }

    private void sendFailedDownloadMeterBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for meter failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MeterListDownloadFinishedBroadcast,
                MeterListDownloadFinishedBundle,
                new MeterListDownloadFinishedContent(_meterList, false, Tools.CompressStringToByteArray(response)));
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
