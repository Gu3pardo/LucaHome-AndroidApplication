package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.MoneyMeter;
import guepardoapps.lucahome.common.classes.MoneyMeterData;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMoneyMeterDataConverter;
import guepardoapps.lucahome.common.database.DatabaseMoneyMeterDataList;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class MoneyMeterListService implements IDataService {
    public static class MoneyMeterListDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<MoneyMeter> MoneyMeterList;

        MoneyMeterListDownloadFinishedContent(SerializableList<MoneyMeter> moneyMeterList, boolean succcess) {
            super(succcess, new byte[]{});
            MoneyMeterList = moneyMeterList;
        }
    }

    public static class MoneyMeterDataListDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<MoneyMeterData> MoneyMeterDataList;

        MoneyMeterDataListDownloadFinishedContent(SerializableList<MoneyMeterData> moneyMeterDataList, boolean succcess) {
            super(succcess, new byte[]{});
            MoneyMeterDataList = moneyMeterDataList;
        }
    }

    public static final String MoneyMeterDataIntent = "MoneyMeterDataIntent";

    public static final String MoneyMeterListDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.moneymeterlist.download.finished";
    public static final String MoneyMeterListDownloadFinishedBundle = "MoneyMeterListDownloadFinishedBundle";

    public static final String MoneyMeterDataListDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.moneymeterdatalist.download.finished";
    public static final String MoneyMeterDataListDownloadFinishedBundle = "MoneyMeterDataListDownloadFinishedBundle";

    public static final String MoneyMeterDataListAddFinishedBroadcast = "guepardoapps.lucahome.data.service.moneymeterdatalist.add.finished";
    public static final String MoneyMeterDataListAddFinishedBundle = "MoneyMeterDataListAddFinishedBundle";

    public static final String MoneyMeterDataListUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.moneymeterdatalist.update.finished";
    public static final String MoneyMeterDataListUpdateFinishedBundle = "MoneyMeterDataListUpdateFinishedBundle";

    public static final String MoneyMeterDataListDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.moneymeterdatalist.delete.finished";
    public static final String MoneyMeterDataListDeleteFinishedBundle = "MoneyMeterDataListDeleteFinishedBundle";

    private static final MoneyMeterListService SINGLETON = new MoneyMeterListService();
    private boolean _isInitialized;

    private static final String TAG = MoneyMeterListService.class.getSimpleName();

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

    private DatabaseMoneyMeterDataList _databaseMoneyMeterDataList;

    private MoneyMeter _activeMoneyMeter;
    private SerializableList<MoneyMeter> _moneyMeterList = new SerializableList<>();
    private SerializableList<MoneyMeterData> _moneyMeterDataList = new SerializableList<>();

    private BroadcastReceiver _moneyMeterDataListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyMeterData) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                _moneyMeterDataList = _databaseMoneyMeterDataList.GetMoneyMeterDataList();
                sendFailedDownloadBroadcast();
                createMoneyMeterList();
                sendFailedDownloadMeterBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                _moneyMeterDataList = _databaseMoneyMeterDataList.GetMoneyMeterDataList();
                sendFailedDownloadBroadcast();
                createMoneyMeterList();
                sendFailedDownloadMeterBroadcast();
                return;
            }

            SerializableList<MoneyMeterData> moneyMeterDataList = JsonDataToMoneyMeterDataConverter.getInstance().GetList(contentResponse);
            if (moneyMeterDataList == null) {
                Logger.getInstance().Error(TAG, "Converted moneyMeterDataList is null!");
                _moneyMeterDataList = _databaseMoneyMeterDataList.GetMoneyMeterDataList();
                sendFailedDownloadBroadcast();
                createMoneyMeterList();
                sendFailedDownloadMeterBroadcast();
                return;
            }

            _lastUpdate = new Date();

            _moneyMeterDataList = moneyMeterDataList;
            createMoneyMeterList();

            clearMoneyMeterDataListFromDatabase();
            saveMoneyMeterDataListToDatabase();

            _broadcastController.SendSerializableBroadcast(
                    MoneyMeterDataListDownloadFinishedBroadcast,
                    MoneyMeterDataListDownloadFinishedBundle,
                    new MoneyMeterDataListDownloadFinishedContent(_moneyMeterDataList, true));
        }
    };

    private BroadcastReceiver _moneyMeterDataListAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyMeterDataAdd) {
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
                    MoneyMeterDataListAddFinishedBroadcast,
                    MoneyMeterDataListAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _moneyMeterDataListUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyMeterDataUpdate) {
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
                    MoneyMeterDataListUpdateFinishedBroadcast,
                    MoneyMeterDataListUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _moneyMeterDataListDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MoneyMeterDataDelete) {
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
                    MoneyMeterDataListDeleteFinishedBroadcast,
                    MoneyMeterDataListDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

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

    private MoneyMeterListService() {
    }

    public static MoneyMeterListService getInstance() {
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

        _databaseMoneyMeterDataList = new DatabaseMoneyMeterDataList(context);
        _databaseMoneyMeterDataList.Open();

        _receiverController.RegisterReceiver(_moneyMeterDataListDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyMeterDataListAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyMeterDataListUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyMeterDataListDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMoneyMeterDataList.Close();
        _isInitialized = false;
    }

    @Override
    public SerializableList<MoneyMeterData> GetDataList() {
        return _moneyMeterDataList;
    }

    public SerializableList<MoneyMeter> GetMoneyMeterList() {
        return _moneyMeterList;
    }

    public MoneyMeter GetActiveMoneyMeter() {
        return _activeMoneyMeter;
    }

    public void SetActiveMoneyMeter(@NonNull MoneyMeter moneyMeter) {
        _activeMoneyMeter = moneyMeter;
    }

    public void SetActiveMoneyMeter(int typeId) {
        for (int index = 0; index < _moneyMeterList.getSize(); index++) {
            if (_moneyMeterList.getValue(index).GetTypeId() == typeId) {
                _activeMoneyMeter = _moneyMeterList.getValue(index);
                break;
            }
        }
    }

    public ArrayList<String> GetMoneyMeterDescriptionList() {
        ArrayList<String> descriptionList = new ArrayList<>();
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            String description = String.format(Locale.getDefault(), "%s - %s", _moneyMeterDataList.getValue(index).GetBank(), _moneyMeterDataList.getValue(index).GetPlan());
            descriptionList.add(description);
        }
        return new ArrayList<>(descriptionList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetMoneyMeterBankList() {
        ArrayList<String> bankList = new ArrayList<>();
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            bankList.add(_moneyMeterDataList.getValue(index).GetBank());
        }
        return new ArrayList<>(bankList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetMoneyMeterPlanList() {
        ArrayList<String> planList = new ArrayList<>();
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            planList.add(_moneyMeterDataList.getValue(index).GetPlan());
        }
        return new ArrayList<>(planList.stream().distinct().collect(Collectors.toList()));
    }

    public ArrayList<String> GetMoneyMeterUnitList() {
        ArrayList<String> unitList = new ArrayList<>();
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            unitList.add(_moneyMeterDataList.getValue(index).GetUnit());
        }
        return new ArrayList<>(unitList.stream().distinct().collect(Collectors.toList()));
    }

    public MoneyMeterData GetById(int id) {
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            MoneyMeterData entry = _moneyMeterDataList.getValue(index);
            if (entry.GetId() == id) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int GetHighestId() {
        int highestId = -1;
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            int id = _moneyMeterDataList.getValue(index).GetId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    public int GetHighestTypeId(@NonNull String bank, @NonNull String plan) {
        int highestTypeId = -1;
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            MoneyMeterData moneyMeterData = _moneyMeterDataList.getValue(index);
            if (moneyMeterData.GetBank().equals(bank) && moneyMeterData.GetPlan().equals(plan)) {
                int typeId = moneyMeterData.GetTypeId();
                if (typeId > highestTypeId) {
                    highestTypeId = typeId;
                }
            }
        }
        return highestTypeId;
    }

    @Override
    public SerializableList<MoneyMeterData> SearchDataList(@NonNull String searchKey) {
        SerializableList<MoneyMeterData> foundMoneyMeterDataList = new SerializableList<>();
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            MoneyMeterData entry = _moneyMeterDataList.getValue(index);
            if (entry.toString().contains(searchKey)) {
                foundMoneyMeterDataList.addValue(entry);
            }
        }
        return foundMoneyMeterDataList;
    }

    @Override
    public void LoadData() {
        if (!_loadDataEnabled) {
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            _moneyMeterDataList = _databaseMoneyMeterDataList.GetMoneyMeterDataList();
            createMoneyMeterList();
            _broadcastController.SendSerializableBroadcast(
                    MoneyMeterDataListDownloadFinishedBroadcast,
                    MoneyMeterDataListDownloadFinishedBundle,
                    new MoneyMeterDataListDownloadFinishedContent(_moneyMeterDataList, true));
            return;
        }

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedDownloadBroadcast();
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_MONEY_METER_DATA_USER.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyMeterData, true);
    }

    public void AddMoneyMeterData(@NonNull MoneyMeterData entry) {
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

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyMeterDataAdd, true);
    }

    public void UpdateMoneyMeterData(@NonNull MoneyMeterData entry) {
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

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyMeterDataUpdate, true);
    }

    public void DeleteMoneyMeterData(@NonNull MoneyMeterData entry) {
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

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MoneyMeterDataDelete, true);
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

    private void clearMoneyMeterDataListFromDatabase() {
        SerializableList<MoneyMeterData> moneyMeterDataList = _databaseMoneyMeterDataList.GetMoneyMeterDataList();
        for (int index = 0; index < moneyMeterDataList.getSize(); index++) {
            MoneyMeterData meterData = moneyMeterDataList.getValue(index);
            _databaseMoneyMeterDataList.Delete(meterData);
        }
    }

    private void saveMoneyMeterDataListToDatabase() {
        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            MoneyMeterData moneyMeterData = _moneyMeterDataList.getValue(index);
            _databaseMoneyMeterDataList.CreateEntry(moneyMeterData);
        }
    }

    private void createMoneyMeterList() {
        SerializableList<MoneyMeter> moneyMeterList = new SerializableList<>();

        for (int index = 0; index < _moneyMeterDataList.getSize(); index++) {
            MoneyMeterData moneyMeterData = _moneyMeterDataList.getValue(index);
            int typeId = moneyMeterData.GetTypeId();

            boolean moneyMeterAlreadyExists = false;
            int moneyMeterIndex;
            for (moneyMeterIndex = 0; moneyMeterIndex < _moneyMeterDataList.getSize(); moneyMeterIndex++) {
                MoneyMeter moneyMeter = moneyMeterList.getValue(moneyMeterIndex);
                if (moneyMeter != null) {
                    if (moneyMeter.GetTypeId() == typeId) {
                        moneyMeterAlreadyExists = true;
                        break;
                    }
                }
            }

            if (moneyMeterAlreadyExists) {
                MoneyMeter moneyMeter = moneyMeterList.getValue(moneyMeterIndex);
                SerializableList<MoneyMeterData> moneyMeterDataList = moneyMeter.GetMoneyMeterDataList();
                moneyMeterDataList.addValue(moneyMeterData);
                moneyMeter.SetMoneyMeterDataList(moneyMeterDataList);
                moneyMeterList.setValue(moneyMeterIndex, moneyMeter);
            } else {
                SerializableList<MoneyMeterData> moneyMeterDataList = new SerializableList<>();
                moneyMeterDataList.addValue(moneyMeterData);
                MoneyMeter moneyMeter = new MoneyMeter(moneyMeterData.GetTypeId(), moneyMeterData.GetBank(), moneyMeterData.GetPlan(), moneyMeterData.GetUser(), moneyMeterDataList);
                moneyMeterList.addValue(moneyMeter);
            }
        }

        _moneyMeterList = moneyMeterList;
        _activeMoneyMeter = _moneyMeterList.getSize() > 0 ? _moneyMeterList.getValue(0) : null;

        _broadcastController.SendSerializableBroadcast(
                MoneyMeterListDownloadFinishedBroadcast,
                MoneyMeterListDownloadFinishedBundle,
                new MoneyMeterListDownloadFinishedContent(_moneyMeterList, true));
    }

    private void sendFailedDownloadMeterBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                MoneyMeterListDownloadFinishedBroadcast,
                MoneyMeterListDownloadFinishedBundle,
                new MoneyMeterListDownloadFinishedContent(_moneyMeterList, false));
    }

    private void sendFailedDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                MoneyMeterDataListDownloadFinishedBroadcast,
                MoneyMeterDataListDownloadFinishedBundle,
                new MoneyMeterDataListDownloadFinishedContent(_moneyMeterDataList, false));
    }

    private void sendFailedAddBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Add for money meter data failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MoneyMeterDataListAddFinishedBroadcast,
                MoneyMeterDataListAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedUpdateBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Update of money meter data failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MoneyMeterDataListUpdateFinishedBroadcast,
                MoneyMeterDataListUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedDeleteBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Delete for money meter data failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MoneyMeterDataListDeleteFinishedBroadcast,
                MoneyMeterDataListDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
