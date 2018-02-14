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

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.NotificationController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToTemperatureConverter;
import guepardoapps.lucahome.common.databases.DatabaseTemperatureList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class TemperatureService implements ITemperatureService {
    private static final String Tag = TemperatureService.class.getSimpleName();

    private static final TemperatureService Singleton = new TemperatureService();

    private static final int MinTimeoutMin = 15;
    private static final int MaxTimeoutMin = 24 * 60;

    private DatabaseTemperatureList _databaseTemperatureList;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private NotificationController _notificationController;
    private ReceiverController _receiverController;

    private Calendar _lastUpdate;

    private Class<?> _receiverActivity;
    private boolean _displayNotification;

    private boolean _isInitialized;

    private Temperature _activeTemperature;

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
                ArrayList<Temperature> temperatureList = JsonDataToTemperatureConverter.getInstance().GetList(contentResponse);
                if (temperatureList == null) {
                    Logger.getInstance().Error(Tag, "Converted temperatureList is null!");
                    _broadcastController.SendBooleanBroadcast(TemperatureDownloadFinishedBroadcast, TemperatureDownloadFinishedBundle, false);
                    return "";
                }

                _databaseTemperatureList.ClearDatabase();
                saveTemperatureListToDatabase(temperatureList);

                ShowNotification();

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(TemperatureDownloadFinishedBroadcast, TemperatureDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _temperatureDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.Temperature) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(TemperatureDownloadFinishedBroadcast, TemperatureDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(TemperatureDownloadFinishedBroadcast, TemperatureDownloadFinishedBundle, false);
                return;
            }

            new AsyncConverterTask().execute(contentResponse);
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

    private TemperatureService() {
    }

    public static TemperatureService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _lastUpdate = Calendar.getInstance();

        _receiverActivity = receiverActivity;
        _displayNotification = displayNotification;
        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _notificationController = new NotificationController(context);
        _receiverController = new ReceiverController(context);

        _databaseTemperatureList = new DatabaseTemperatureList(context);
        _databaseTemperatureList.Open();

        _receiverController.RegisterReceiver(_temperatureDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseTemperatureList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<Temperature> GetDataList() {
        try {
            return _databaseTemperatureList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Temperature GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseTemperatureList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseTemperatureList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new Temperature(uuid, UUID.randomUUID(), 0, Calendar.getInstance(), "", Temperature.TemperatureType.Dummy, "");
        }
    }

    @Override
    public ArrayList<Temperature> SearchDataList(@NonNull String searchKey) {
        ArrayList<Temperature> temperatureList = GetDataList();
        ArrayList<Temperature> foundTemperatureList = new ArrayList<>();
        for (int index = 0; index < temperatureList.size(); index++) {
            Temperature entry = temperatureList.get(index);
            if (entry.toString().contains(searchKey)) {
                foundTemperatureList.add(entry);
            }
        }
        return foundTemperatureList;
    }

    @Override
    public void LoadData() {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(TemperatureDownloadFinishedBroadcast, TemperatureDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(TemperatureDownloadFinishedBroadcast, TemperatureDownloadFinishedBundle, false);
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_TEMPERATURE.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.Temperature, true);
    }

    @Override
    public void AddEntry(@NonNull Temperature entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for " + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull Temperature entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method UpdateEntry not implemented for " + Tag);
    }

    @Override
    public void DeleteEntry(@NonNull Temperature entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method DeleteEntry not implemented for " + Tag);
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
    public void ShowNotification() {
        if (!_displayNotification) {
            Logger.getInstance().Warning(Tag, "_displayNotification is false!");
            return;
        }

        if (_receiverActivity == null) {
            Logger.getInstance().Error(Tag, "ReceiverActivity is null!");
            return;
        }

        if (_activeTemperature == null) {
            Logger.getInstance().Error(Tag, "_activeTemperature is null!");
            return;
        }

        String locationName = RoomService.getInstance().GetByUuid(_activeTemperature.GetRoomUuid()).GetName();
        String body = String.format(Locale.getDefault(), "%s: %s", locationName, _activeTemperature.GetTemperatureString());

        _notificationController.CreateTemperatureNotification(NotificationId, _receiverActivity, R.drawable.weather_dummy, "Temperature", body, true);
    }

    @Override
    public void CloseNotification() {
        _notificationController.CloseNotification(NotificationId);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) {
        _displayNotification = displayNotification;

        if (!_displayNotification) {
            CloseNotification();
        } else {
            ShowNotification();
        }
    }

    @Override
    public boolean GetDisplayNotification() {
        return _displayNotification;
    }

    @Override
    public void SetReceiverActivity(@NonNull Class<?> receiverActivity) {
        _receiverActivity = receiverActivity;
    }

    @Override
    public Class<?> GetReceiverActivity() {
        return _receiverActivity;
    }

    @Override
    public void SetActiveTemperature(@NonNull Temperature activeTemperature) {
        _activeTemperature = activeTemperature;
    }

    @Override
    public Temperature GetActiveTemperature() {
        if (_activeTemperature == null) {
            ArrayList<Temperature> temperatureList = GetDataList();
            _activeTemperature = temperatureList.size() > 0 ? temperatureList.get(0) : new Temperature(UUID.randomUUID(), UUID.randomUUID(), -273.15, Calendar.getInstance(), "", Temperature.TemperatureType.Dummy, "");
        }
        return _activeTemperature;
    }

    private void saveTemperatureListToDatabase(@NonNull ArrayList<Temperature> temperatureList) {
        for (int index = 0; index < temperatureList.size(); index++) {
            Temperature temperature = temperatureList.get(index);
            _databaseTemperatureList.AddEntry(temperature);
        }
    }
}
