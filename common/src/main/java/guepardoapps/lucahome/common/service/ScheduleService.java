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
import guepardoapps.lucahome.common.classes.LucaTimer;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.Schedule;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToScheduleConverter;
import guepardoapps.lucahome.common.converter.JsonDataToTimerConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.interfaces.services.IDataService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class ScheduleService implements IDataService {
    public static class ScheduleDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Schedule> ScheduleList;

        ScheduleDownloadFinishedContent(SerializableList<Schedule> scheduleList, boolean succcess) {
            super(succcess, new byte[]{});
            ScheduleList = scheduleList;
        }
    }

    public static class TimerDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<LucaTimer> TimerList;

        TimerDownloadFinishedContent(SerializableList<LucaTimer> timerList, boolean succcess) {
            super(succcess, new byte[]{});
            TimerList = timerList;
        }
    }

    public static final String ScheduleIntent = "ScheduleIntent";

    public static final String ScheduleDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.schedule.download.finished";
    public static final String ScheduleDownloadFinishedBundle = "ScheduleDownloadFinishedBundle";

    public static final String ScheduleSetFinishedBroadcast = "guepardoapps.lucahome.data.service.schedule.set.finished";
    public static final String ScheduleSetFinishedBundle = "ScheduleSetFinishedBundle";

    public static final String ScheduleAddFinishedBroadcast = "guepardoapps.lucahome.data.service.schedule.add.finished";
    public static final String ScheduleAddFinishedBundle = "ScheduleAddFinishedBundle";

    public static final String ScheduleUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.schedule.update.finished";
    public static final String ScheduleUpdateFinishedBundle = "ScheduleUpdateFinishedBundle";

    public static final String ScheduleDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.schedule.delete.finished";
    public static final String ScheduleDeleteFinishedBundle = "ScheduleDeleteFinishedBundle";

    public static final String TimerDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.download.finished";
    public static final String TimerDownloadFinishedBundle = "TimerDownloadFinishedBundle";

    public static final String TimerAddFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.add.finished";
    public static final String TimerAddFinishedBundle = "TimerAddFinishedBundle";

    public static final String TimerUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.update.finished";
    public static final String TimerUpdateFinishedBundle = "TimerUpdateFinishedBundle";

    public static final String TimerDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.timer.delete.finished";
    public static final String TimerDeleteFinishedBundle = "TimerDeleteFinishedBundle";

    private static final ScheduleService SINGLETON = new ScheduleService();
    private boolean _isInitialized;

    private static final String TAG = ScheduleService.class.getSimpleName();

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

    private SerializableList<Schedule> _scheduleList = new SerializableList<>();
    private SerializableList<LucaTimer> _timerList = new SerializableList<>();

    private BroadcastReceiver _scheduleDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Schedule) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedTimerDownloadBroadcast();
                sendFailedScheduleDownloadBroadcast();
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedTimerDownloadBroadcast();
                sendFailedScheduleDownloadBroadcast();
                return;
            }

            SerializableList<Schedule> scheduleList = JsonDataToScheduleConverter.getInstance().GetList(contentResponse, WirelessSocketService.getInstance().GetDataList(), WirelessSwitchService.getInstance().GetDataList());
            if (scheduleList == null) {
                Logger.getInstance().Error(TAG, "Converted scheduleList is null!");
                sendFailedScheduleDownloadBroadcast();
            } else {
                _lastUpdate = new Date();
                _scheduleList = scheduleList;
                _broadcastController.SendSerializableBroadcast(
                        ScheduleDownloadFinishedBroadcast,
                        ScheduleDownloadFinishedBundle,
                        new ScheduleDownloadFinishedContent(_scheduleList, true));
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

    private BroadcastReceiver _scheduleSetFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleSet) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedScheduleSetBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedScheduleSetBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    ScheduleSetFinishedBroadcast,
                    ScheduleSetFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _scheduleAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleAdd) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedScheduleAddBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedScheduleAddBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    ScheduleAddFinishedBroadcast,
                    ScheduleAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _scheduleUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleUpdate) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedScheduleUpdateBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedScheduleUpdateBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    ScheduleUpdateFinishedBroadcast,
                    ScheduleUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
        }
    };

    private BroadcastReceiver _scheduleDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleDelete) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(TAG, contentResponse);
                sendFailedScheduleDeleteBroadcast(contentResponse);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(TAG, "Download was not successful!");
                sendFailedScheduleDeleteBroadcast(contentResponse);
                return;
            }

            _lastUpdate = new Date();

            _broadcastController.SendSerializableBroadcast(
                    ScheduleDeleteFinishedBroadcast,
                    ScheduleDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, new byte[]{}));

            LoadData();
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

    private ScheduleService() {
    }

    public static ScheduleService getInstance() {
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

        _receiverController.RegisterReceiver(_scheduleDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleSetFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

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
    public SerializableList<Schedule> GetDataList() {
        return _scheduleList;
    }

    public ArrayList<String> GetScheduleNameList() {
        ArrayList<String> scheduleNameList = new ArrayList<>();

        for (int index = 0; index < _scheduleList.getSize(); index++) {
            scheduleNameList.add(_scheduleList.getValue(index).GetName());
        }

        return scheduleNameList;
    }

    public Schedule GetScheduleById(int id) {
        for (int index = 0; index < _scheduleList.getSize(); index++) {
            Schedule entry = _scheduleList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    public Schedule GetScheduleByName(@NonNull String name) {
        for (int index = 0; index < _scheduleList.getSize(); index++) {
            Schedule entry = _scheduleList.getValue(index);

            if (entry.GetName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public SerializableList<Schedule> SearchDataList(@NonNull String searchKey) {
        SerializableList<Schedule> foundSchedules = new SerializableList<>();

        for (int index = 0; index < _scheduleList.getSize(); index++) {
            Schedule entry = _scheduleList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetTime().toString().contains(searchKey)
                    || entry.GetWirelessSocket().toString().contains(searchKey)
                    || entry.GetWirelessSwitch().toString().contains(searchKey)
                    || entry.GetWeekday().toString().contains(searchKey)
                    || entry.GetAction().toString().contains(searchKey)
                    || entry.GetIsActiveString().contains(searchKey)) {
                foundSchedules.addValue(entry);
            }
        }

        return foundSchedules;
    }

    public SerializableList<LucaTimer> GetTimerList() {
        return _timerList;
    }

    public ArrayList<String> GetTimerNameList() {
        ArrayList<String> timerNameList = new ArrayList<>();

        for (int index = 0; index < _timerList.getSize(); index++) {
            timerNameList.add(_timerList.getValue(index).GetName());
        }

        return timerNameList;
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

    public LucaTimer GetTimerByName(String name) {
        for (int index = 0; index < _timerList.getSize(); index++) {
            LucaTimer entry = _timerList.getValue(index);

            if (entry.GetName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    public SerializableList<LucaTimer> FoundTimer(@NonNull String searchKey) {
        SerializableList<LucaTimer> foundTimer = new SerializableList<>();

        for (int index = 0; index < _timerList.getSize(); index++) {
            LucaTimer entry = _timerList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetTime().toString().contains(searchKey)
                    || entry.GetWirelessSocket().toString().contains(searchKey)
                    || entry.GetWirelessSwitch().toString().contains(searchKey)
                    || entry.GetWeekday().toString().contains(searchKey)
                    || entry.GetAction().toString().contains(searchKey)
                    || entry.GetIsActiveString().contains(searchKey)) {
                foundTimer.addValue(entry);
            }
        }

        return foundTimer;
    }

    @Override
    public void LoadData() {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleDownloadBroadcast();
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_SCHEDULES.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Schedule, true);
    }

    public void ChangeScheduleState(@NonNull Schedule entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleSetBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandChangeState());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ScheduleSet, true);
    }

    public void SetScheduleState(@NonNull Schedule entry, boolean newState) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleSetBroadcast("No user");
            return;
        }

        entry.SetActive(newState);
        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandSetState());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ScheduleSet, true);
    }

    public void AddSchedule(@NonNull Schedule entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ScheduleAdd, true);
    }

    public void UpdateSchedule(@NonNull Schedule entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ScheduleUpdate, true);
    }

    public void DeleteSchedule(@NonNull Schedule entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.ScheduleDelete, true);
    }

    public void AddTimer(@NonNull LucaTimer entry) {
        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleAddBroadcast("No user");
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
            sendFailedScheduleUpdateBroadcast("No user");
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
            sendFailedScheduleDeleteBroadcast("No user");
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

    private void sendFailedScheduleDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                ScheduleDownloadFinishedBroadcast,
                ScheduleDownloadFinishedBundle,
                new ScheduleDownloadFinishedContent(null, false));
    }

    private void sendFailedScheduleSetBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ScheduleSetFinishedBroadcast,
                ScheduleSetFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedScheduleAddBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ScheduleAddFinishedBroadcast,
                ScheduleAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedScheduleUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ScheduleUpdateFinishedBroadcast,
                ScheduleUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedScheduleDeleteBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ScheduleDeleteFinishedBroadcast,
                ScheduleDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedTimerDownloadBroadcast() {
        _broadcastController.SendSerializableBroadcast(
                TimerDownloadFinishedBroadcast,
                TimerDownloadFinishedBundle,
                new ScheduleDownloadFinishedContent(null, false));
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
