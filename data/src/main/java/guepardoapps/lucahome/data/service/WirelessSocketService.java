package guepardoapps.lucahome.data.service;

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
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.converter.JsonDataToSocketConverter;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.data.controller.DownloadController;
import guepardoapps.lucahome.data.controller.NotificationController;
import guepardoapps.lucahome.data.controller.SettingsController;
import guepardoapps.lucahome.data.service.broadcasts.content.ObjectChangeFinishedContent;

public class WirelessSocketService {
    public static class WirelessSocketDownloadFinishedContent extends ObjectChangeFinishedContent {
        public SerializableList<WirelessSocket> WirelessSocketList;

        public WirelessSocketDownloadFinishedContent(SerializableList<WirelessSocket> wirelessSocketList, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            WirelessSocketList = wirelessSocketList;
        }
    }

    public static final int NOTIFICATION_ID = 211990;

    public static final String WirelessSocketIntent = "WirelessSocketIntent";

    public static final String WirelessSocketDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.download.finished";
    public static final String WirelessSocketDownloadFinishedBundle = "WirelessSocketDownloadFinishedBundle";

    public static final String WirelessSocketSetFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelessscket.set.finished";
    public static final String WirelessSocketSetFinishedBundle = "WirelessSocketSetFinishedBundle";

    public static final String WirelessSocketAddFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.add.finished";
    public static final String WirelessSocketAddFinishedBundle = "WirelessSocketAddFinishedBundle";

    public static final String WirelessSocketUpdateFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.update.finished";
    public static final String WirelessSocketUpdateFinishedBundle = "WirelessSocketUpdateFinishedBundle";

    public static final String WirelessSocketDeleteFinishedBroadcast = "guepardoapps.lucahome.data.service.wirelesssocket.delete.finished";
    public static final String WirelessSocketDeleteFinishedBundle = "WirelessSocketDeleteFinishedBundle";

    private static final WirelessSocketService SINGLETON = new WirelessSocketService();
    private boolean _isInitialized;

    private static final String TAG = WirelessSocketService.class.getSimpleName();
    private Logger _logger;

    private Class<?> _receiverActivity;

    private static final int TIMEOUT_MS = 5 * 60 * 1000;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            _logger.Debug("_reloadListRunnable run");
            LoadWirelessSocketList();
            _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);
        }
    };

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private JsonDataToSocketConverter _jsonDataToWirelessSocketConverter;

    private SerializableList<WirelessSocket> _wirelessSocketList = new SerializableList<>();

    private BroadcastReceiver _wirelessSocketDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketDownloadFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocket) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketDownloadBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketDownloadBroadcast(contentResponse);
                return;
            }

            SerializableList<WirelessSocket> wirelessSocketList = _jsonDataToWirelessSocketConverter.GetList(contentResponse);
            if (wirelessSocketList == null) {
                _logger.Error("Converted wirelessSocketList is null!");
                sendFailedSocketDownloadBroadcast(contentResponse);
                return;
            }

            _wirelessSocketList = wirelessSocketList;
            ShowNotification();

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketDownloadFinishedBroadcast,
                    WirelessSocketDownloadFinishedBundle,
                    new WirelessSocketDownloadFinishedContent(_wirelessSocketList, true, content.Response));
        }
    };

    private BroadcastReceiver _wirelessSocketSetFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketSetFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketSet) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketSetBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketSetBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketSetFinishedBroadcast,
                    WirelessSocketSetFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadWirelessSocketList();
        }
    };

    private BroadcastReceiver _wirelessSocketAddFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketAddFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketAdd) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketAddBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketAddBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketAddFinishedBroadcast,
                    WirelessSocketAddFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadWirelessSocketList();
        }
    };

    private BroadcastReceiver _wirelessSocketUpdateFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketUpdateFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketUpdate) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketUpdateBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketUpdateBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketUpdateFinishedBroadcast,
                    WirelessSocketUpdateFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadWirelessSocketList();
        }
    };

    private BroadcastReceiver _wirelessSocketDeleteFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketDeleteFinishedReceiver");
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);
            String contentResponse = Tools.DecompressByteArrayToString(content.Response);

            if (content.CurrentDownloadType != DownloadController.DownloadType.WirelessSocketDelete) {
                _logger.Debug(String.format(Locale.getDefault(), "Received download finished with downloadType %s", content.CurrentDownloadType));
                return;
            }

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                _logger.Error(contentResponse);
                sendFailedSocketDeleteBroadcast(contentResponse);
                return;
            }

            _logger.Debug(String.format(Locale.getDefault(), "Response is %s", contentResponse));

            if (!content.Success) {
                _logger.Error("Download was not successful!");
                sendFailedSocketDeleteBroadcast(contentResponse);
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    WirelessSocketDeleteFinishedBroadcast,
                    WirelessSocketDeleteFinishedBundle,
                    new ObjectChangeFinishedContent(true, content.Response));

            LoadWirelessSocketList();
        }
    };

    private WirelessSocketService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static WirelessSocketService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context, @NonNull Class<?> receiverActivity) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _receiverActivity = receiverActivity;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);
        _settingsController = SettingsController.getInstance();

        _receiverController.RegisterReceiver(_wirelessSocketDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketSetFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketAddFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketUpdateFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketDeleteFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _jsonDataToWirelessSocketConverter = new JsonDataToSocketConverter();

        _reloadHandler.postDelayed(_reloadListRunnable, TIMEOUT_MS);

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public SerializableList<WirelessSocket> GetWirelessSocketList() {
        return _wirelessSocketList;
    }

    public ArrayList<String> GetNameList() {
        ArrayList<String> nameList = new ArrayList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            nameList.add(_wirelessSocketList.getValue(index).GetName());
        }

        return nameList;
    }

    public ArrayList<String> GetAreaList() {
        ArrayList<String> areaList = new ArrayList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            String area = _wirelessSocketList.getValue(index).GetArea();
            if (!areaList.contains(area)) {
                areaList.add(area);
            }
        }

        return areaList;
    }

    public ArrayList<String> GetCodeList() {
        ArrayList<String> codeList = new ArrayList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            codeList.add(_wirelessSocketList.getValue(index).GetCode());
        }

        return codeList;
    }

    public WirelessSocket GetSocketById(int id) {
        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket entry = _wirelessSocketList.getValue(index);

            if (entry.GetId() == id) {
                return entry;
            }
        }

        return null;
    }

    public WirelessSocket GetSocketByName(String name) {
        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket entry = _wirelessSocketList.getValue(index);

            if (entry.GetName().contains(name)) {
                return entry;
            }
        }

        return null;
    }

    public SerializableList<WirelessSocket> FoundWirelessSockets(@NonNull String searchKey) {
        SerializableList<WirelessSocket> foundWirelessSockets = new SerializableList<>();

        for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
            WirelessSocket entry = _wirelessSocketList.getValue(index);

            if (String.valueOf(entry.GetId()).contains(searchKey)
                    || entry.GetName().contains(searchKey)
                    || entry.GetShortName().contains(searchKey)
                    || entry.GetArea().contains(searchKey)
                    || entry.GetCode().contains(searchKey)
                    || entry.GetActivationString().contains(searchKey)
                    || String.valueOf(entry.IsActivated()).contains(searchKey)) {
                foundWirelessSockets.addValue(entry);
            }
        }

        return foundWirelessSockets;
    }

    public void LoadWirelessSocketList() {
        _logger.Debug("LoadWirelessSocketList");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketDownloadBroadcast("No user");
            return;
        }

        String requestUrl = "http://"
                + _settingsController.GetServerIp()
                + Constants.ACTION_PATH
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerAction.GET_SOCKETS.toString();
        _logger.Debug(String.format(Locale.getDefault(), "RequestUrl is: %s", requestUrl));

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocket, true);
    }

    public void ChangeWirelessSocketState(WirelessSocket entry) {
        _logger.Debug(String.format(Locale.getDefault(), "ChangeWirelessSocketState: Changing state of entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketSetBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandChangeState());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketSet, true);
    }

    public void SetWirelessSocketState(WirelessSocket entry, boolean newState) {
        _logger.Debug(String.format(Locale.getDefault(), "SetWirelessSocketState: Setting state of entry %s to %s", entry, newState));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketSetBroadcast("No user");
            return;
        }

        entry.SetActivated(newState);
        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandSetState());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketSet, true);
    }

    public void DeactivateAllWirelessSockets() {
        _logger.Debug("DeactivateAllWirelessSockets");

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketSetBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                LucaServerAction.DEACTIVATE_ALL_SOCKETS);

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketSet, true);
    }

    public void AddWirelessSocket(WirelessSocket entry) {
        _logger.Debug(String.format(Locale.getDefault(), "AddWirelessSocket: Adding new entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketAddBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandAdd());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketAdd, true);
    }

    public void UpdateWirelessSocket(WirelessSocket entry) {
        _logger.Debug(String.format(Locale.getDefault(), "UpdateWirelessSocket: Updating entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketUpdateBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandUpdate());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketUpdate, true);
    }

    public void DeleteWirelessSocket(WirelessSocket entry) {
        _logger.Debug(String.format(Locale.getDefault(), "DeleteWirelessSocket: Deleting entry %s", entry));

        LucaUser user = _settingsController.GetUser();
        if (user == null) {
            sendFailedSocketDeleteBroadcast("No user");
            return;
        }

        String requestUrl = String.format(Locale.getDefault(), "http://%s%s%s&password=%s&action=%s",
                _settingsController.GetServerIp(), Constants.ACTION_PATH,
                user.GetName(), user.GetPassphrase(),
                entry.CommandDelete());

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.WirelessSocketDelete, true);
    }

    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) {
        _receiverActivity = receiverActivity;
    }

    public Class<?> GetReceiverActivity() {
        return _receiverActivity;
    }

    public void ShowNotification() {
        _logger.Debug("ShowNotification");
        _notificationController.CreateSocketNotification(NOTIFICATION_ID, _wirelessSocketList, _receiverActivity);
    }

    public void CloseNotification() {
        _logger.Debug("CloseNotification");
        _notificationController.CloseNotification(NOTIFICATION_ID);
    }

    private void sendFailedSocketDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                WirelessSocketDownloadFinishedBroadcast,
                WirelessSocketDownloadFinishedBundle,
                new WirelessSocketDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketSetBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                WirelessSocketSetFinishedBroadcast,
                WirelessSocketSetFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketAddBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                WirelessSocketAddFinishedBroadcast,
                WirelessSocketAddFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketUpdateBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                WirelessSocketUpdateFinishedBroadcast,
                WirelessSocketUpdateFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedSocketDeleteBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                WirelessSocketDeleteFinishedBroadcast,
                WirelessSocketDeleteFinishedBundle,
                new ObjectChangeFinishedContent(false, Tools.CompressStringToByteArray(response)));
    }
}