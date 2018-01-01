package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import guepardoapps.lucahome.common.classes.LucaTimer;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToTimerConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class TimerService implements IDataService {
    public static class TimerDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<LucaTimer> TimerList;

        TimerDownloadFinishedContent(SerializableList<LucaTimer> timerList, boolean succcess) {
            super(succcess, new byte[]{});
            TimerList = timerList;
        }
    }

    public static final String TimerIntent = "TimerIntent";

    public static final String TimerDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.download.finished";
    public static final String TimerDownloadFinishedBundle = "TimerDownloadFinishedBundle";

    public static final String TimerAddFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.add.finished";
    public static final String TimerAddFinishedBundle = "TimerAddFinishedBundle";

    public static final String TimerUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.update.finished";
    public static final String TimerUpdateFinishedBundle = "TimerUpdateFinishedBundle";

    public static final String TimerDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.delete.finished";
    public static final String TimerDeleteFinishedBundle = "TimerDeleteFinishedBundle";

    private static final TimerService SINGLETON = new TimerService();
    private boolean _isInitialized;

    private static final String TAG = TimerService.class.getSimpleName();

    private static final int MIN_TIMEOUT_MIN = 60;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

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

    private SerializableList<LucaTimer> _timerList = new SerializableList<>();

    private BroadcastReceiver _timerDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Timer) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedTimerDownloadBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedTimerDownloadBroadcast();
                return;
            }

            SerializableList<LucaTimer> timerList = JsonDataToTimerConverter.getInstance().GetList(contentResponse, WirelessSocketService.getInstance().GetDataList(), WirelessSwitchService.getInstance().GetDataList());
            if (timerList == null) {
                Logger.getInstance().Error(TAG, "Converted timerList is null!");
                sendFailedTimerDownloadBroadcast();
            } else {
                _lastUpdate = new Date();
                _timerList = timerList;
                _broadcastController.SendSerializableBroadcast(
                        TimerDownloadFinishedBroadcast,
                        TimerDownloadFinishedBundle,
                        new TimerDownloadFinishedContent(_timerList, true));
            }

        }
    };

    private BroadcastReceiver _timerAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.TimerAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedTimerAddBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedTimerAddBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    TimerAddFinishedBroadcast,
                    TimerAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _timerUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.TimerUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedTimerUpdateBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedTimerUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    TimerUpdateFinishedBroadcast,
                    TimerUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _timerDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.TimerDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedTimerDeleteBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedTimerDeleteBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    TimerDeleteFinishedBroadcast,
                    TimerDeleteFinishedBundle,
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

    private TimerService() {
    }

    public static TimerService getInstance() {
        return SINGLETON;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        _lastUpdate = new Date();

        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _receiverController.RegisterReceiver(_timerDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_timerAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_timerUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_timerDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    @Override
    public SerializableList<LucaTimer> GetDataList() {
        return _timerList;
    }

    public ArrayList<String> GetTimerNameList() {
        ArrayList<String> timerNameList = new ArrayList<>();
        for (int index = 0; index < _timerList.getSize(); index++) {
            timerNameList.add(_timerList.getValue(index).GetName());
        }
        return new ArrayList<>(timerNameList.stream().distinct().collect(Collectors.toList()));
    }

    public LucaTimer GetTimerByName(String name) {
        for (int index = 0; index < _timerList.getSize(); index++) {
            LucaTimer entry = _timerList.getValue(index);
            if (entry.GetName().contains(name)) {
                return entry;
            }
        }
        return null;
    }

    public LucaTimer GetTimerById(int id) {
        for (int index = 0; index < _timerList.getSize(); index++) {
            LucaTimer entry = _timerList.getValue(index);
            if (entry.GetId() == id) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int GetHighestId() {
        int highestId = -1;
        for (int index = 0; index < _timerList.getSize(); index++) {
            int id = _timerList.getValue(index).GetId();
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId;
    }

    @Override
    public SerializableList<LucaTimer> SearchDataList(@NonNull String searchKey) {
        SerializableList<LucaTimer> foundTimer = new SerializableList<>();
        for (int index = 0; index < _timerList.getSize(); index++) {
            LucaTimer entry = _timerList.getValue(index);
            if (entry.toString().contains(searchKey)) {
                foundTimer.addValue(entry);
            }
        }
        return foundTimer;
    }

    @Override
    public void LoadData() {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedTimerDownloadBroadcast();
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_SCHEDULES.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Timer, true);
    }

    public void AddTimer(@NonNull LucaTimer entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedTimerAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.TimerAdd, true);
    }

    public void UpdateTimer(@NonNull LucaTimer entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedTimerUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.TimerUpdate, true);
    }

    public void DeleteTimer(@NonNull LucaTimer entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedTimerDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.TimerDelete, true);
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

    private void sendFailedTimerDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                TimerDownloadFinishedBroadcast,
                TimerDownloadFinishedBundle,
                new TimerDownloadFinishedContent(null, false));
    }

    private void sendFailedTimerAddBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                TimerAddFinishedBroadcast,
                TimerAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedTimerUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                TimerUpdateFinishedBroadcast,
                TimerUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedTimerDeleteBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                TimerDeleteFinishedBroadcast,
                TimerDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}
