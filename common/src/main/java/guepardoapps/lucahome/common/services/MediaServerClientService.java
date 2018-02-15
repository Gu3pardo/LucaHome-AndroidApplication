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

import guepardoapps.lucahome.common.classes.MediaServer;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.classes.mediaserver.InformationData;
import guepardoapps.lucahome.common.classes.mediaserver.NotificationData;
import guepardoapps.lucahome.common.classes.mediaserver.RadioStreamData;
import guepardoapps.lucahome.common.classes.mediaserver.SleepTimerData;
import guepardoapps.lucahome.common.classes.mediaserver.YoutubeData;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.DownloadController;
import guepardoapps.lucahome.common.controller.NetworkController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.converter.JsonDataToMediaServerConverter;
import guepardoapps.lucahome.common.databases.DatabaseMediaServerList;
import guepardoapps.lucahome.common.enums.LucaServerActionTypes;
import guepardoapps.lucahome.common.enums.MediaServerActionType;
import guepardoapps.lucahome.common.enums.RSSFeedType;
import guepardoapps.lucahome.common.enums.RadioStreamType;
import guepardoapps.lucahome.common.tasks.mediaserver.MediaServerClientTask;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.common.utils.Tools;

import static guepardoapps.lucahome.common.server.handler.IDataHandler.CommandSplitChar;
import static guepardoapps.lucahome.common.server.handler.IDataHandler.ResponseSplitChar;

@SuppressWarnings({"WeakerAccess"})
public class MediaServerClientService implements IMediaServerClientService {
    private static final String Tag = MediaServerClientService.class.getSimpleName();

    private static final MediaServerClientService Singleton = new MediaServerClientService();

    private static final int MinTimeoutMin = 5;
    private static final int MaxTimeoutMin = 24 * 60;

    private MediaServer _activeMediaServer;

    private DatabaseMediaServerList _databaseMediaServerList;

    private Context _context;

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
                ArrayList<MediaServer> mediaServerDataList = JsonDataToMediaServerConverter.getInstance().GetList(contentResponse);
                if (mediaServerDataList == null) {
                    Logger.getInstance().Error(Tag, "Converted mediaServerDataList is null!");
                    _broadcastController.SendBooleanBroadcast(MediaServerDataDownloadFinishedBroadcast, MediaServerDataDownloadFinishedBundle, false);
                    return "";
                }

                _databaseMediaServerList.ClearDatabase();
                saveMediaServerListToDatabase(mediaServerDataList);

                _lastUpdate = Calendar.getInstance();
                _broadcastController.SendBooleanBroadcast(MediaServerDataDownloadFinishedBroadcast, MediaServerDataDownloadFinishedBundle, true);
            }
            return "Success";
        }
    }

    private BroadcastReceiver _mediaServerActionDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(MediaServerClientTask.MediaServerClientTaskBundle);
            if (response != null) {
                try {
                    handleResponse(response);
                    _lastUpdate = Calendar.getInstance();
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.toString());
                }
            } else {
                Logger.getInstance().Error(Tag, "Received null response!");
            }
        }
    };

    private BroadcastReceiver _mediaServerDataDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadController.DownloadFinishedBroadcastContent content = (DownloadController.DownloadFinishedBroadcastContent) intent.getSerializableExtra(DownloadController.DownloadFinishedBundle);

            if (content.CurrentDownloadType != DownloadController.DownloadType.MediaServerData) {
                return;
            }

            String contentResponse = Tools.DecompressByteArrayToString(DownloadStorageService.getInstance().GetDownloadResult(content.CurrentDownloadType));

            if (contentResponse.contains("Error") || contentResponse.contains("ERROR")
                    || contentResponse.contains("Canceled") || contentResponse.contains("CANCELED")
                    || content.FinalDownloadState != DownloadController.DownloadState.Success) {
                Logger.getInstance().Error(Tag, contentResponse);
                _broadcastController.SendBooleanBroadcast(MediaServerDataDownloadFinishedBroadcast, MediaServerDataDownloadFinishedBundle, false);
                return;
            }

            if (!content.Success) {
                Logger.getInstance().Error(Tag, "Download was not successful!");
                _broadcastController.SendBooleanBroadcast(MediaServerDataDownloadFinishedBroadcast, MediaServerDataDownloadFinishedBundle, false);
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

    private MediaServerClientService() {
    }

    public static MediaServerClientService getInstance() {
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

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _downloadController = new DownloadController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);

        _activeMediaServer = new MediaServer();

        _databaseMediaServerList = new DatabaseMediaServerList(_context);
        _databaseMediaServerList.Open();

        _receiverController.RegisterReceiver(_mediaServerActionDownloadFinishedReceiver, new String[]{MediaServerClientTask.MediaServerClientTaskBroadcast});
        _receiverController.RegisterReceiver(_mediaServerDataDownloadFinishedReceiver, new String[]{DownloadController.DownloadFinishedBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _databaseMediaServerList.Close();
        _isInitialized = false;
    }

    @Override
    public void SetActiveMediaServer(@NonNull MediaServer mediaServer) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "SetActiveMediaServer with MediaServer %s", mediaServer));

        if (mediaServer.GetUuid() == _activeMediaServer.GetUuid()) {
            Logger.getInstance().Warning(Tag, "MediaServer already active!");
            return;
        }

        _activeMediaServer = mediaServer;
        sendAllGetCommands();
    }

    @Override
    public MediaServer GetActiveMediaServer() {
        return _activeMediaServer;
    }

    @Override
    public ArrayList<MediaServer> GetDataList() {
        try {
            return _databaseMediaServerList.GetList(null, null, null);
        } catch (SQLException sqlException) {
            Logger.getInstance().Error(Tag, sqlException.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public MediaServer GetByUuid(@NonNull UUID uuid) {
        try {
            return _databaseMediaServerList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseMediaServerList.KeyUuid, uuid), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new MediaServer();
        }
    }

    @Override
    public MediaServer GetMediaServerByIp(@NonNull String ip) {
        try {
            return _databaseMediaServerList.GetList(String.format(Locale.getDefault(), "%s like %s", DatabaseMediaServerList.KeyIp, ip), null, null).get(0);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            return new MediaServer();
        }
    }

    @Override
    public ArrayList<UUID> GetRoomUuidList() {
        ArrayList<UUID> roomUuidList = new ArrayList<>();
        for (MediaServer entry : GetDataList()) {
            roomUuidList.add(entry.GetRoomUuid());
        }
        return roomUuidList;
    }

    @Override
    public ArrayList<String> GetRadioStreamTitleList() {
        ArrayList<String> radioStreams = new ArrayList<>();
        for (RadioStreamType entry : RadioStreamType.values()) {
            radioStreams.add(entry.GetTitle());
        }
        return radioStreams;
    }

    @Override
    public ArrayList<String> GetRssFeedTitleList() {
        ArrayList<String> rssStreams = new ArrayList<>();
        for (RSSFeedType entry : RSSFeedType.values()) {
            rssStreams.add(entry.GetTitle());
        }
        return rssStreams;
    }

    @Override
    public ArrayList<MediaServer> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SearchDataList not implemented for " + Tag);
    }

    @Override
    public void SendCommand(@NonNull String command, @NonNull String data) {
        if (!_isInitialized) {
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
            return;
        }

        if (_activeMediaServer == null) {
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
            return;
        }

        String serverIp = _activeMediaServer.GetIp();
        if (serverIp.length() == 0) {
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
            return;
        }

        String communication = String.format(Locale.getDefault(), "COMMAND:%s%sDATA:%s", command, CommandSplitChar, data);
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Sending communication %s to %s:%d", communication, serverIp, Constants.MediaServerPort));

        MediaServerClientTask mediaServerClientTask = new MediaServerClientTask(_context, serverIp, Constants.MediaServerPort, communication);
        mediaServerClientTask.execute();
    }

    @Override
    public void LoadData() {
        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            _broadcastController.SendBooleanBroadcast(MediaServerDataDownloadFinishedBroadcast, MediaServerDataDownloadFinishedBundle, true);
            return;
        }

        User user = SettingsController.getInstance().GetUser();
        if (user == null) {
            _broadcastController.SendBooleanBroadcast(MediaServerDataDownloadFinishedBroadcast, MediaServerDataDownloadFinishedBundle, false);
            return;
        }

        String requestUrl = "http://"
                + SettingsController.getInstance().GetServerIp()
                + Constants.ActionPath
                + user.GetName() + "&password=" + user.GetPassphrase()
                + "&action=" + LucaServerActionTypes.GET_MEDIA_SERVER.toString();

        _downloadController.SendCommandToWebsiteAsync(requestUrl, DownloadController.DownloadType.MediaServerData, true);
    }

    @Override
    public void AddEntry(@NonNull MediaServer entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for " + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull MediaServer entry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method UpdateEntry not implemented for " + Tag);
    }

    @Override
    public void DeleteEntry(@NonNull MediaServer entry) throws NoSuchMethodException {
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

    private void saveMediaServerListToDatabase(@NonNull ArrayList<MediaServer> list) {
        for (int index = 0; index < list.size(); index++) {
            MediaServer entry = list.get(index);
            _databaseMediaServerList.AddEntry(entry);
        }
    }

    private void handleResponse(@NonNull String response) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleResponse: %s", response));

        String[] responseData = response.split(ResponseSplitChar);
        if (responseData.length == ResponseDataSize) {

            MediaServerActionType responseAction = MediaServerActionType.GetByString(responseData[IndexResponseAction]);
            if (responseAction != null) {
                switch (responseAction) {
                    case YOUTUBE_PLAY:
                    case YOUTUBE_PAUSE:
                    case YOUTUBE_STOP:
                    case YOUTUBE_SET_POSITION:
                        handleYoutubeResponse(responseData);
                        break;

                    case CENTER_TEXT_SET:
                        handleCenterTextResponse(responseData);
                        break;

                    case RADIO_STREAM_PLAY:
                    case RADIO_STREAM_STOP:
                        handleRadioStreamResponse(responseData);
                        break;

                    case MEDIA_NOTIFICATION_PLAY:
                    case MEDIA_NOTIFICATION_STOP:
                        handleNotificationResponse(responseData);
                        break;

                    case SLEEP_SOUND_PLAY:
                    case SLEEP_SOUND_STOP:
                        handleSleepSoundResponse(responseData);
                        break;

                    case RSS_FEED_SET:
                    case RSS_FEED_RESET:
                        handleRssFeedResponse(responseData);
                        break;

                    case UPDATE_BIRTHDAY_ALARM:
                    case UPDATE_CALENDAR_ALARM:
                    case UPDATE_CURRENT_WEATHER:
                    case UPDATE_FORECAST_WEATHER:
                    case UPDATE_IP_ADDRESS:
                    case UPDATE_RASPBERRY_TEMPERATURE:
                        Logger.getInstance().Information(Tag, String.format(Locale.getDefault(), "No handle necessary for CMD %s", responseAction));
                        break;

                    case VOLUME_INCREASE:
                    case VOLUME_DECREASE:
                    case VOLUME_MUTE:
                    case VOLUME_UN_MUTE:
                        handleVolumeResponse(responseData);
                        break;

                    case SCREEN_BRIGHTNESS_INCREASE:
                    case SCREEN_BRIGHTNESS_DECREASE:
                    case SCREEN_NORMAL:
                    case SCREEN_ON:
                    case SCREEN_OFF:
                        handleScreenResponse(responseData);
                        break;

                    case GET_INFORMATION_DATA:
                        handleGetInformationResponse(responseData);
                        break;
                    case GET_NOTIFICATION_DATA:
                        handleGetNotificationDataResponse(responseData);
                        break;
                    case GET_RADIO_STREAM_DATA:
                        handleGetRadioDataResponse(responseData);
                        break;
                    case GET_SLEEP_TIMER_DATA:
                        handleGetSleepTimerDataResponse(responseData);
                        break;
                    case GET_YOUTUBE_DATA:
                        handleGetYoutubeDataResponse(responseData);
                        break;

                    case SYSTEM_REBOOT:
                    case SYSTEM_SHUTDOWN:
                        Logger.getInstance().Information(Tag, String.format(Locale.getDefault(), "No handle necessary for CMD %s", responseAction));
                        break;

                    case NULL:
                    default:
                        Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "No handle for %s", responseAction));
                        _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
                        return;
                }

                if (responseData[IndexResponseFlag].contains("OK")) {
                    _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, true);
                    return;
                }
            }
        }

        _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
    }

    private void handleYoutubeResponse(@NonNull String[] responseData) {
        handleGetYoutubeDataResponse(responseData);
    }

    private void handleCenterTextResponse(@NonNull String[] responseData) {
        handleGetInformationResponse(responseData);
    }

    private void handleRadioStreamResponse(@NonNull String[] responseData) {
        handleGetRadioDataResponse(responseData);
    }

    private void handleNotificationResponse(@NonNull String[] responseData) {
        handleGetNotificationDataResponse(responseData);
    }

    private void handleSleepSoundResponse(@NonNull String[] responseData) {
        handleGetSleepTimerDataResponse(responseData);
    }

    private void handleRssFeedResponse(@NonNull String[] responseData) {
        handleGetInformationResponse(responseData);
    }

    private void handleVolumeResponse(@NonNull String[] responseData) {
        handleGetInformationResponse(responseData);
    }

    private void handleScreenResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(Tag, "handleScreenResponse");
        handleGetInformationResponse(responseData);
    }

    private void handleGetInformationResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(Tag, "handleGetInformationResponse");
        try {
            InformationData informationData = new InformationData();
            informationData.ParseCommunicationString(responseData[IndexResponseData]);
            _activeMediaServer.GetBundledData().SetInformationData(informationData);
            _broadcastController.SendSimpleBroadcast(MediaServerInformationDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
        }
    }

    private void handleGetNotificationDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(Tag, "handleGetNotificationDataResponse");
        try {
            NotificationData notificationData = new NotificationData();
            notificationData.ParseCommunicationString(responseData[IndexResponseData]);
            _activeMediaServer.GetBundledData().SetNotificationData(notificationData);
            _broadcastController.SendSimpleBroadcast(MediaServerNotificationDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
        }
    }

    private void handleGetRadioDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(Tag, "handleGetRadioDataResponse");
        try {
            RadioStreamData radioStreamData = new RadioStreamData();
            radioStreamData.ParseCommunicationString(responseData[IndexResponseData]);
            _activeMediaServer.GetBundledData().SetRadioStreamData(radioStreamData);
            _broadcastController.SendSimpleBroadcast(MediaServerRadioStreamDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
        }
    }

    private void handleGetSleepTimerDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(Tag, "handleGetSleepTimerDataResponse");
        try {
            SleepTimerData sleepTimerData = new SleepTimerData();
            sleepTimerData.ParseCommunicationString(responseData[IndexResponseData]);
            _activeMediaServer.GetBundledData().SetSleepTimerData(sleepTimerData);
            _broadcastController.SendSimpleBroadcast(MediaServerSleepTimerDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
        }
    }

    private void handleGetYoutubeDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(Tag, "handleGetYoutubeDataResponse");
        try {
            YoutubeData youtubeData = new YoutubeData();
            youtubeData.ParseCommunicationString(responseData[IndexResponseData]);
            _activeMediaServer.GetBundledData().SetYoutubeData(youtubeData);
            _broadcastController.SendSimpleBroadcast(MediaServerYoutubeDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            _broadcastController.SendBooleanBroadcast(MediaServerCommandResponseBroadcast, MediaServerCommandResponseBundle, false);
        }
    }

    private void sendAllGetCommands() {
        SendCommand(MediaServerActionType.GET_INFORMATION_DATA.toString(), "");
        SendCommand(MediaServerActionType.GET_NOTIFICATION_DATA.toString(), "");
        SendCommand(MediaServerActionType.GET_RADIO_STREAM_DATA.toString(), "");
        SendCommand(MediaServerActionType.GET_SLEEP_TIMER_DATA.toString(), "");
        SendCommand(MediaServerActionType.GET_YOUTUBE_DATA.toString(), "");
    }
}
