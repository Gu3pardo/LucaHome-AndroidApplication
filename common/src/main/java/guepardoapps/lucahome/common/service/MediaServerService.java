package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.mediaserver.MediaNotificationData;
import guepardoapps.lucahome.common.classes.mediaserver.MediaServerData;
import guepardoapps.lucahome.common.classes.mediaserver.MediaServerInformationData;
import guepardoapps.lucahome.common.classes.mediaserver.RadioStreamData;
import guepardoapps.lucahome.common.classes.mediaserver.SleepTimerData;
import guepardoapps.lucahome.common.classes.mediaserver.YoutubeData;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.enums.RSSFeed;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.common.tasks.ClientTask;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MediaServerService {
    public static class MediaServerDownloadFinishedContent extends ObjectChangeFinishedContent {
        public MediaServerData MediaServer;

        MediaServerDownloadFinishedContent(@NonNull MediaServerData mediaServer, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MediaServer = mediaServer;
        }
    }

    public static final String MediaServerCommandResponseBroadcast = "guepardoapps.lucahome.data.service.mediaServer.command_response";
    public static final String MediaServerCommandResponseBundle = "MediaServerCommandResponseBundle";

    public static final String MediaServerYoutubeDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.youtube_data";
    public static final String MediaServerYoutubeDataBundle = "MediaServerYoutubeDataBundle";

    public static final String MediaServerYoutubeVideoDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.youtube_video_data";
    public static final String MediaServerYoutubeVideoDataBundle = "MediaServerYoutubeVideoDataBundle";

    public static final String MediaServerCenterTextDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.center_text_data";
    public static final String MediaServerCenterTextDataBundle = "MediaServerCenterTextDataBundle";

    public static final String MediaServerRadioStreamDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.radio_stream_data";
    public static final String MediaServerRadioStreamDataBundle = "MediaServerRadioStreamDataBundle";

    public static final String MediaServerMediaNotificationDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.media_notification_data";
    public static final String MediaServerMediaNotificationDataBundle = "MediaServerMediaNotificationDataBundle";

    public static final String MediaServerSleepTimerDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.sleep_timer_data";
    public static final String MediaServerSleepTimerDataBundle = "MediaServerSleepTimerDataBundle";

    public static final String MediaServerRssFeedDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.rss_feed_data";
    public static final String MediaServerRssFeedDataBundle = "MediaServerRssFeedDataBundle";

    public static final String MediaServerInformationDataBroadcast = "guepardoapps.lucahome.data.service.mediaServer.information_data";
    public static final String MediaServerInformationDataBundle = "MediaServerInformationDataBundle";

    private static final MediaServerService SINGLETON = new MediaServerService();
    private static final String TAG = MediaServerService.class.getSimpleName();

    public static final String COMMAND_SPLIT_CHAR = "&&";
    public static final int INDEX_COMMAND_ACTION = 0;
    public static final int INDEX_COMMAND_DATA = 1;
    public static final int COMMAND_DATA_SIZE = 2;

    public static final String RESPONSE_SPLIT_CHAR = "###";
    private static final int INDEX_RESPONSE_FLAG = 0;
    private static final int INDEX_RESPONSE_MESSAGE = 1;
    private static final int INDEX_RESPONSE_ACTION = 2;
    private static final int INDEX_RESPONSE_DATA = 3;
    private static final int RESPONSE_DATA_SIZE = 4;

    private boolean _isInitialized;

    private static final int MIN_TIMEOUT_MIN = 30;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            sendAllGetCommands();
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private Context _context;
    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private MediaServerData _activeMediaServer;

    private BroadcastReceiver _mediaServerDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(ClientTask.ClientTaskBundle);
            if (response != null) {
                try {
                    handleResponse(response);
                } catch (Exception exception) {
                    Logger.getInstance().Error(TAG, exception.getMessage());
                }
            } else {
                Logger.getInstance().Error(TAG, "Received null response!");
            }
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

    private MediaServerService() {
    }

    public static MediaServerService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout) {
        if (_isInitialized) {
            Logger.getInstance().Warning(TAG, "Already initialized!");
            return;
        }

        Logger.getInstance().Information(TAG, "Initialize");

        _reloadEnabled = reloadEnabled;

        _context = context;
        _broadcastController = new BroadcastController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);

        _activeMediaServer = new MediaServerData(new YoutubeData(), "", new RadioStreamData(), new MediaNotificationData(), new SleepTimerData(), RSSFeed.DEFAULT, new MediaServerInformationData(), false);

        _receiverController.RegisterReceiver(_mediaServerDownloadFinishedReceiver, new String[]{ClientTask.ClientTaskBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    public void Dispose() {
        Logger.getInstance().Information(TAG, "Dispose");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public MediaServerData GetActiveMediaServer() {
        return _activeMediaServer;
    }

    public void SetActiveMediaServer(@NonNull MediaServerSelection selectedMediaServerSelection) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "SetActiveMediaServer with MediaServerSelection %s", selectedMediaServerSelection));

        if (selectedMediaServerSelection.GetId() == _activeMediaServer.GetMediaServerInformationData().GetMediaServerSelection().GetId()) {
            Logger.getInstance().Warning(TAG, "MediaServer already active!");
            return;
        }

        MediaServerInformationData mediaServerInformationData = new MediaServerInformationData();
        mediaServerInformationData.SetMediaServerSelection(selectedMediaServerSelection);

        _activeMediaServer = new MediaServerData(new YoutubeData(), "", new RadioStreamData(), new MediaNotificationData(), new SleepTimerData(), RSSFeed.DEFAULT, mediaServerInformationData, true);
        sendAllGetCommands();
    }

    public ArrayList<String> GetServerLocations() {
        Logger.getInstance().Debug(TAG, "GetServerLocations");
        ArrayList<String> serverLocations = new ArrayList<>();
        for (MediaServerSelection entry : MediaServerSelection.values()) {
            serverLocations.add(entry.GetLocation());
        }
        return serverLocations;
    }

    public ArrayList<String> GetRadioStreamTitleList() {
        Logger.getInstance().Debug(TAG, "GetRadioStreamTitleList");
        ArrayList<String> radioStreams = new ArrayList<>();
        for (RadioStreams entry : RadioStreams.values()) {
            radioStreams.add(entry.GetTitle());
        }
        return radioStreams;
    }

    public ArrayList<String> GetRssFeedTitleList() {
        Logger.getInstance().Debug(TAG, "GetRssFeedTitleList");
        ArrayList<String> rssStreams = new ArrayList<>();
        for (RSSFeed entry : RSSFeed.values()) {
            rssStreams.add(entry.GetTitle());
        }
        return rssStreams;
    }

    public void SendCommand(@NonNull String command, @NonNull String data) {
        if (!_isInitialized) {
            sendFailedCommandBroadcast("Not initialized!");
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            sendFailedCommandBroadcast("No home network!");
            return;
        }

        if (_activeMediaServer == null) {
            sendFailedCommandBroadcast("No active media server!");
            return;
        }

        if (!_activeMediaServer.IsValidModel()) {
            sendFailedCommandBroadcast("Active media server is not valid!");
            return;
        }

        String serverIp = _activeMediaServer.GetMediaServerInformationData().GetMediaServerSelection().GetIp();
        String communication = String.format(Locale.getDefault(), "COMMAND:%s%sDATA:%s", command, COMMAND_SPLIT_CHAR, data);
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Sending communication %s to %s:%d", communication, serverIp, Constants.MEDIASERVER_SERVERPORT));

        ClientTask clientTask = new ClientTask(_context, serverIp, Constants.MEDIASERVER_SERVERPORT, communication);
        clientTask.execute();
    }

    public boolean GetReloadEnabled() {
        return _reloadEnabled;
    }

    public void SetReloadEnabled(boolean reloadEnabled) {
        _reloadEnabled = reloadEnabled;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    public int GetReloadTimeout() {
        return _reloadTimeout;
    }

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

    private void handleResponse(@NonNull String response) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleResponse: %s", response));

        String[] responseData = response.split(RESPONSE_SPLIT_CHAR);
        if (responseData.length == RESPONSE_DATA_SIZE) {

            MediaServerAction responseAction = MediaServerAction.GetByString(responseData[INDEX_RESPONSE_ACTION]);
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
                        handleMediaNotificationResponse(responseData);
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
                        handleUpdateResponse(responseData);
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

                    case GET_CENTER_TEXT:
                        handleGetCenterTextResponse(responseData);
                        break;
                    case GET_MEDIA_NOTIFICATION_DATA:
                        handleGetMediaNotificationDataResponse(responseData);
                        break;
                    case GET_MEDIA_SERVER_INFORMATION_DATA:
                        handleGetMediaServerInformationResponse(responseData);
                        break;
                    case GET_RADIO_DATA:
                        handleGetRadioDataResponse(responseData);
                        break;
                    case GET_RSS_FEED_DATA:
                        handleGetRssFeedDataResponse(responseData);
                        break;
                    case GET_SLEEP_TIMER_DATA:
                        handleGetSleepTimerDataResponse(responseData);
                        break;
                    case GET_YOUTUBE_DATA:
                        handleGetYoutubeDataResponse(responseData);
                        break;

                    case SYSTEM_REBOOT:
                    case SYSTEM_SHUTDOWN:
                        handleSystemResponse(responseData);
                        break;

                    case NULL:
                    default:
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "No handle for %s", responseAction));
                        sendFailedCommandBroadcast(String.format(Locale.getDefault(), "No handle for %s", responseAction));
                        return;
                }

                boolean commandSuccess = responseData[INDEX_RESPONSE_FLAG].contains("OK");
                if (commandSuccess) {
                    sendSucceededCommandBroadcast(String.format(Locale.getDefault(), "Action %s was successfully performed", responseAction));
                } else {
                    sendFailedCommandBroadcast(String.format(Locale.getDefault(), "Action %s failed!", responseAction));
                }
            } else {
                Logger.getInstance().Warning(TAG, "responseAction is null!");
                sendFailedCommandBroadcast("responseAction is null!");
            }
        } else {
            Logger.getInstance().Warning(TAG, String.format(Locale.getDefault(), "ResponseData has invalid size %d", responseData.length));
            sendFailedCommandBroadcast(String.format(Locale.getDefault(), "ResponseData has invalid size %d", responseData.length));
        }
    }

    private void handleYoutubeResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleYoutubeResponse");
        handleGetYoutubeDataResponse(responseData);
    }

    private void handleCenterTextResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleCenterTextResponse");
        handleGetCenterTextResponse(responseData);
    }

    private void handleRadioStreamResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleRadioStreamResponse");
        handleGetRadioDataResponse(responseData);
    }

    private void handleMediaNotificationResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleMediaNotificationResponse");
        handleGetMediaNotificationDataResponse(responseData);
    }

    private void handleSleepSoundResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleSleepSoundResponse");
        handleGetSleepTimerDataResponse(responseData);
    }

    private void handleRssFeedResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleRssFeedResponse");
        handleGetRssFeedDataResponse(responseData);
    }

    private void handleUpdateResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleUpdateResponse");
        // Nothing to do here
        Logger.getInstance().Information(TAG, "No action necessary for handleUpdateResponse");
    }

    private void handleVolumeResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleVolumeResponse");
        handleGetMediaServerInformationResponse(responseData);
    }

    private void handleScreenResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleScreenResponse");
        handleGetMediaServerInformationResponse(responseData);
    }

    private void handleGetCenterTextResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleGetCenterTextResponse");
        String centerText = responseData[INDEX_RESPONSE_DATA];
        _activeMediaServer.SetCenterText(centerText);
        _broadcastController.SendSimpleBroadcast(MediaServerCenterTextDataBroadcast);
    }

    private void handleGetMediaNotificationDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleGetMediaNotificationDataResponse");
        try {
            MediaNotificationData mediaNotificationData = new MediaNotificationData();
            mediaNotificationData.ParseCommunicationString(responseData[INDEX_RESPONSE_DATA]);
            _activeMediaServer.SetMediaNotificationData(mediaNotificationData);
            _broadcastController.SendSimpleBroadcast(MediaServerMediaNotificationDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            sendFailedCommandBroadcast(exception.toString());
        }
    }

    private void handleGetMediaServerInformationResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleGetMediaServerInformationResponse");
        try {
            MediaServerInformationData mediaServerInformationData = new MediaServerInformationData();
            mediaServerInformationData.ParseCommunicationString(responseData[INDEX_RESPONSE_DATA]);
            _activeMediaServer.SetMediaServerInformationData(mediaServerInformationData);
            _broadcastController.SendSimpleBroadcast(MediaServerInformationDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            sendFailedCommandBroadcast(exception.toString());
        }
    }

    private void handleGetRadioDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleGetRadioDataResponse");
        try {
            RadioStreamData radioStreamData = new RadioStreamData();
            radioStreamData.ParseCommunicationString(responseData[INDEX_RESPONSE_DATA]);
            _activeMediaServer.SetRadioStreamData(radioStreamData);
            _broadcastController.SendSimpleBroadcast(MediaServerRadioStreamDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            sendFailedCommandBroadcast(exception.toString());
        }
    }

    private void handleGetRssFeedDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleGetRssFeedDataResponse");
        try {
            RSSFeed rssFeed = RSSFeed.GetById(Integer.parseInt(responseData[INDEX_RESPONSE_DATA]));
            _activeMediaServer.SetRSSFeed(rssFeed);
            _broadcastController.SendSimpleBroadcast(MediaServerRssFeedDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            sendFailedCommandBroadcast(exception.toString());
        }
    }

    private void handleGetSleepTimerDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleGetSleepTimerDataResponse");
        try {
            SleepTimerData sleepTimerData = new SleepTimerData();
            sleepTimerData.ParseCommunicationString(responseData[INDEX_RESPONSE_DATA]);
            _activeMediaServer.SetSleepTimerData(sleepTimerData);
            _broadcastController.SendSimpleBroadcast(MediaServerSleepTimerDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            sendFailedCommandBroadcast(exception.toString());
        }
    }

    private void handleGetYoutubeDataResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleGetYoutubeDataResponse");
        try {
            YoutubeData youtubeData = new YoutubeData();
            youtubeData.ParseCommunicationString(responseData[INDEX_RESPONSE_DATA]);
            _activeMediaServer.SetYoutubeData(youtubeData);
            _broadcastController.SendSimpleBroadcast(MediaServerYoutubeDataBroadcast);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            sendFailedCommandBroadcast(exception.toString());
        }
    }

    private void handleSystemResponse(@NonNull String[] responseData) {
        Logger.getInstance().Debug(TAG, "handleSystemResponse");
        // Nothing to do here
        Logger.getInstance().Information(TAG, "No action necessary for handleSystemResponse");
    }

    private void sendSucceededCommandBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Sending command to media server was successful!";
        }
        _broadcastController.SendSerializableBroadcast(
                MediaServerCommandResponseBroadcast,
                MediaServerCommandResponseBundle,
                new MediaServerDownloadFinishedContent(_activeMediaServer, true, Tools.CompressStringToByteArray(response)));
    }

    private void sendFailedCommandBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Sending command to media server failed!";
        }
        _broadcastController.SendSerializableBroadcast(
                MediaServerCommandResponseBroadcast,
                MediaServerCommandResponseBundle,
                new MediaServerDownloadFinishedContent(_activeMediaServer, false, Tools.CompressStringToByteArray(response)));
    }

    private void sendAllGetCommands() {
        Logger.getInstance().Debug(TAG, "sendAllGetCommands");

        SendCommand(MediaServerAction.GET_MEDIA_SERVER_INFORMATION_DATA.toString(), "");
        SendCommand(MediaServerAction.GET_YOUTUBE_DATA.toString(), "");
        SendCommand(MediaServerAction.GET_CENTER_TEXT.toString(), "");
        SendCommand(MediaServerAction.GET_RADIO_DATA.toString(), "");
        SendCommand(MediaServerAction.GET_MEDIA_NOTIFICATION_DATA.toString(), "");
        SendCommand(MediaServerAction.GET_SLEEP_TIMER_DATA.toString(), "");
        SendCommand(MediaServerAction.GET_RSS_FEED_DATA.toString(), "");
    }
}
