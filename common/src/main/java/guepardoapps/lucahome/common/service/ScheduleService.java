package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
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
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

public class ScheduleService {
    public static class ScheduleDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<Schedule> ScheduleList;

        public ScheduleDownloadFinishedContent(SerializableList<Schedule> scheduleList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            ScheduleList = scheduleList;
        }
    }

    public static class TimerDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<LucaTimer> TimerList;

        public TimerDownloadFinishedContent(SerializableList<LucaTimer> timerList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            TimerList = timerList;
        }
    }

    public static final String ScheduleIntent = "ScheduleIntent";
    public static final String TimerIntent = "TimerIntent";

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
    private Logger _logger;

    private static final int TIMEOUT_MS = 3 * 60 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadScheduleList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private WirelessSocketService _wirelessSocketService;

    private JsonDataToScheduleConverter _jsonDataToScheduleConverter;
    private JsonDataToTimerConverter _jsonDataToTimerConverter;

    private SerializableList<Schedule> _scheduleList = new SerializableList<>();
    private SerializableList<LucaTimer> _timerList = new SerializableList<>();

    private BroadcastReceiver _scheduleDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Schedule) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedTimerDownloadBroadcast(contentResponse);
                sendFailedScheduleDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedTimerDownloadBroadcast(contentResponse);
                sendFailedScheduleDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<Schedule> scheduleList = _jsonDataToScheduleConverter.GetList(contentResponse, _wirelessSocketService.GetWirelessSocketList());
            SerializableList<LucaTimer> timerList = _jsonDataToTimerConverter.GetList(contentResponse, _wirelessSocketService.GetWirelessSocketList());
            if (scheduleList == null) {
                _logger.Error("Converted scheduleList is null!");
                sendFailedTimerDownloadBroadcast(contentResponse);
                sendFailedScheduleDownloadBroadcast(contentResponse);
                return;
            }

            _scheduleList = scheduleList;
            _timerList = timerList;

            _broadcastController.SendSerializableBroadcast(
                    ScheduleDownloadFinishedBroadcast,
                    ScheduleDownloadFinishedBundle,
                    new ScheduleDownloadFinishedContent(_scheduleList, true, content.Response));

            _broadcastController.SendSerializableBroadcast(
                    TimerDownloadFinishedBroadcast,
                    TimerDownloadFinishedBundle,
                    new TimerDownloadFinishedContent(_timerList, true, content.Response));
        }
    };

    private BroadcastReceiver _scheduleSetFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleSetFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleSet) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedScheduleSetBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedScheduleSetBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    ScheduleSetFinishedBroadcast,
                    ScheduleSetFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadScheduleList();
        }
    };

    private BroadcastReceiver _scheduleAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedScheduleAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedScheduleAddBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    ScheduleAddFinishedBroadcast,
                    ScheduleAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadScheduleList();
        }
    };

    private BroadcastReceiver _scheduleUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedScheduleUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedScheduleUpdateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    ScheduleUpdateFinishedBroadcast,
                    ScheduleUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadScheduleList();
        }
    };

    private BroadcastReceiver _scheduleDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.ScheduleDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedScheduleDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedScheduleDeleteBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    ScheduleDeleteFinishedBroadcast,
                    ScheduleDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadScheduleList();
        }
    };

    private BroadcastReceiver _timerAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_timerAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.TimerAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedTimerAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedTimerAddBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    TimerAddFinishedBroadcast,
                    TimerAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadScheduleList();
        }
    };

    private BroadcastReceiver _timerUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_timerUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.TimerUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedTimerUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedTimerUpdateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    TimerUpdateFinishedBroadcast,
                    TimerUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadScheduleList();
        }
    };

    private BroadcastReceiver _timerDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_timerDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.TimerDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedTimerDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedTimerDeleteBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    TimerDeleteFinishedBroadcast,
                    TimerDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadScheduleList();
        }
    };

    private ScheduleService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static ScheduleService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _wirelessSocketService = WirelessSocketService.getInstance();

        _receiverController.RegisterReceiver(_scheduleDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleSetFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_timerAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_timerUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_timerDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToScheduleConverter = new JsonDataToScheduleConverter();
        _jsonDataToTimerConverter = new JsonDataToTimerConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public SerializableList<Schedule> GetScheduleList() {
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

    public Schedule GetScheduleByName(String name) {
        for (int index = 0; index < _scheduleList.getSize(); index++) {
            Schedule entry = _scheduleList.getValue(index);

            if (entry.GetName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    public SerializableList<Schedule> FoundSchedules(@NonNull String searchKey) {
        SerializableList<Schedule> foundSchedules = new SerializableList<>();

        for (int index = 0; index < _scheduleList.getSize(); index++) {
            Schedule entry = _scheduleList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetTime().toString().contains(searchKey)
                    || entry.GetInformation().contains(searchKey)
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
                    || entry.GetInformation().contains(searchKey)
                    || entry.GetWeekday().toString().contains(searchKey)
                    || entry.GetAction().toString().contains(searchKey)
                    || entry.GetIsActiveString().contains(searchKey)) {
                foundTimer.addValue(entry);
            }
        }

        return foundTimer;
    }

    public void LoadScheduleList() {
        _logger.Debug("LoadScheduleList");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedScheduleDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_SCHEDULES.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Schedule, true);
    }

    public void ChangeScheduleState(Schedule entry) {
        _logger.Debug(String.format(Locale.getDefault(), "ChangeScheduleState: Changing state of entry %s", entry));

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

    public void SetScheduleState(Schedule entry, boolean newState) {
        _logger.Debug(String.format(Locale.getDefault(), "SetScheduleState: Setting state of entry %s to %s", entry, newState));

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

    public void AddSchedule(Schedule entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddSchedule: Adding new entry %s", entry));

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

    public void UpdateSchedule(Schedule entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateSchedule: Updating entry %s", entry));

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

    public void DeleteSchedule(Schedule entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteSchedule: Deleting entry %s", entry));

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

    public void AddTimer(LucaTimer entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddTimer: Adding new entry %s", entry));

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

    public void UpdateTimer(LucaTimer entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateTimer: Updating entry %s", entry));

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

    public void DeleteTimer(LucaTimer entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteTimer: Deleting entry %s", entry));

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

    private void sendFailedScheduleDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                ScheduleDownloadFinishedBroadcast,
                ScheduleDownloadFinishedBundle,
                new ScheduleDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
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

    private void sendFailedTimerDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                TimerDownloadFinishedBroadcast,
                TimerDownloadFinishedBundle,
                new ScheduleDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
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
