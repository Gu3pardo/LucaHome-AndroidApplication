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
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMapContentConverter;
import guepardoapps.lucahome.common.databases.DatabaseMapContentList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

@SuppressWarnings({"WeakerAccess"})
public class MapContentService implements IMapContentService {
    private static final String Tag = MapContentService.class.getSimpleName();

    private static final MapContentService Singleton = new MapContentService();

    private static final int MinTimeoutMin = 60;
    private static final int MaxTimeoutMin = 24 * 60;

    private DatabaseMapContentList _databaseMapContentList;

    private BroadcastController _broadcastController;
    private DownloadController _downloadController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private Calendar _lastUpdate;

    private boolean _isInitialized;

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
                ArrayList<MapContent> mapContentList = JsonDataToMapContentConverter.getInstance().GetList(contentResponse);
                if (mapContentList == null) {
                    Logger.getInstance().Error(Tag, "Converted mapContentList is null!");
                    _broadcastController.SendBooleanBroadcast(MapContentDownloadFinishedBroadcast, MapContentDownloadFinishedBundle, false);
                    return "";
                }

                _databaseMapContentList.ClearDatabase();
                saveMapContentListToDatabase(mapContentList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(MapContentDownloadFinishedBroadcast, MapContentDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _mapContentDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MapContent) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MapContentDownloadFinishedBroadcast, MapContentDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(MapContentDownloadFinishedBroadcast, MapContentDownloadFinishedBundle, false);
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

    private MapContentService() {
    }

    public static MapContentService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _lastUpdate = Calendar.getInstance();

        _reloadEnabled = reloadEnabled;

        _broadcastController = new BroadcastController(context);
        _downloadController = new DownloadController(context);
        _networkController = new NetworkController(context);
        _receiverController = new ReceiverController(context);

        _databaseMapContentList = new DatabaseMapContentList(context);
        _databaseMapContentList.Open();

        _receiverController.RegisterReceiver(_mapContentDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMapContentList.Close();
        _isInitialized = false;
    }

    @Override
    public ArrayList<MapContent> GetDataList() {
        try {
            return _databaseMapContentList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public MapContent GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseMapContentList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseMapContentList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new MapContent(uuid, UUID.randomUUID(), MapContent.DrawingType.Null, new int[]{0, 0}, "NULL", "", false, false, ILucaClass.LucaServerDbAction.Null);
        }
    }

    @Override
    public ArrayList<MapContent> SearchDataList(@NonNull String searchKey) {
        ArrayList<MapContent> list = GetDataList();
        ArrayList<MapContent> foundList = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            MapContent entry = list.get(index);
            if (entry.toString().contains(searchKey)) {
                foundList.add(entry);
            }
        }
        return foundList;
    }

    @Override
    public void LoadData() {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(MapContentDownloadFinishedBroadcast, MapContentDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MapContentDownloadFinishedBroadcast, MapContentDownloadFinishedBundle, false);
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_MAP_CONTENT.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MapContent, true);
    }

    @Override
    public void AddEntry(@NonNull MapContent entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for " + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull MapContent entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method UpdateEntry not implemented for " + Tag);
    }

    @Override
    public void DeleteEntry(@NonNull MapContent entry) throws NoSuchMethodException {
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

    private void saveMapContentListToDatabase(@NonNull ArrayList<MapContent> mapContentList) {
        for (int index = 0; index < mapContentList.size(); index++) {
            MapContent mapContent = mapContentList.get(index);
            _databaseMapContentList.AddEntry(mapContent);
        }
    }
}
