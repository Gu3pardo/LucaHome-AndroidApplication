package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.MediaServerData;
import guepardoapps.lucahome.common.classes.PlayedYoutubeVideo;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.common.tasks.ClientTask;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MediaServerService {
    public static class MediaServerDownloadFinishedContent extends ObjectChangeFinishedContent {
        public MediaServerData MediaServer;

        MediaServerDownloadFinishedContent(MediaServerData mediaServer, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MediaServer = mediaServer;
        }
    }

    public static final String MediaServerDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.mediaServer.download.finished";
    public static final String MediaServerDownloadFinishedBundle = "MediaServerDownloadFinishedBundle";

    public static final String MediaServerVolumeBroadcast = "guepardoapps.lucahome.data.service.mediaServer.volume";
    public static final String MediaServerVolumeBundle = "MediaServerVolumeBundle";

    public static final String MediaServerBrightnessBroadcast = "guepardoapps.lucahome.data.service.mediaServer.brightness";
    public static final String MediaServerBrightnessBundle = "MediaServerBrightnessBundle";

    public static final String MediaServerPlayedYoutubeBroadcast = "guepardoapps.lucahome.data.service.mediaServer.played_youtube";
    public static final String MediaServerPlayedYoutubeBundle = "MediaServerPlayedYoutubeBundle";

    public static final String MediaServerBatteryBroadcast = "guepardoapps.lucahome.data.service.mediaServer.battery";
    public static final String MediaServerBatteryBundle = "MediaServerBatteryBundle";

    public static final String MediaServerServerVersionBroadcast = "guepardoapps.lucahome.data.service.mediaServer.server_version";
    public static final String MediaServerServerVersionBundle = "MediaServerServerVersionBundle";

    public static final String MediaServerYoutubeVideoBroadcast = "guepardoapps.lucahome.data.service.mediaServer.youtube_video";
    public static final String MediaServerYoutubeVideoBundle = "MediaServerYoutubeVideoBundle";

    private static final MediaServerService SINGLETON = new MediaServerService();
    private boolean _isInitialized;

    private static final String TAG = MediaServerService.class.getSimpleName();

    private static final int MIN_TIMEOUT_MIN = 30;
    private static final int MAX_TIMEOUT_MIN = 24 * 60;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            //TODO reload method
            if (_reloadEnabled && _networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;

    private Context _context;
    private MediaServerData _mediaServerData;

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

        _reloadEnabled = reloadEnabled;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);

        _receiverController.RegisterReceiver(_mediaServerDownloadFinishedReceiver, new String[]{ClientTask.ClientTaskBroadcast});

        _receiverController.RegisterReceiver(_homeNetworkAvailableReceiver, new String[]{NetworkController.WIFIReceiverInHomeNetworkBroadcast});
        _receiverController.RegisterReceiver(_homeNetworkNotAvailableReceiver, new String[]{NetworkController.WIFIReceiverNoHomeNetworkBroadcast});

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    public void Dispose() {
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public MediaServerData GetMediaServerData() {
        return _mediaServerData;
    }

    public void SendCommand(
            @NonNull String serverIp,
            @NonNull String command,
            @NonNull String data) {
        if (!_isInitialized) {
            sendFailedDownloadBroadcast("Not initialized!");
            return;
        }

        if (!_networkController.IsHomeNetwork(SettingsController.getInstance().GetHomeSsid())) {
            sendFailedDownloadBroadcast("No home network!");
            return;
        }

        String communication = "ACTION:" + command + "&DATA:" + data;

        ClientTask clientTask = new ClientTask(
                _context,
                serverIp,
                Constants.MEDIASERVER_SERVERPORT,
                communication);
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
        String[] responseData = response.split("\\:");

        if (responseData.length > 1) {
            MediaServerAction responseAction = MediaServerAction.GetByString(responseData[0]);
            if (responseAction != null) {
                switch (responseAction) {
                    case INCREASE_VOLUME:
                    case DECREASE_VOLUME:
                    case UN_MUTE_VOLUME:
                    case GET_CURRENT_VOLUME:
                        String currentVolume = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaServerVolumeBroadcast,
                                MediaServerVolumeBundle,
                                currentVolume);
                        break;

                    case INCREASE_SCREEN_BRIGHTNESS:
                    case DECREASE_SCREEN_BRIGHTNESS:
                    case GET_SCREEN_BRIGHTNESS:
                        String currentBrightness = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaServerBrightnessBroadcast,
                                MediaServerBrightnessBundle,
                                currentBrightness);
                        break;

                    case GET_SAVED_YOUTUBE_IDS:
                        String playedYoutubeData = responseData[responseData.length - 1];
                        String[] rawYoutubeIds = playedYoutubeData.split("\\;");

                        ArrayList<PlayedYoutubeVideo> playedYoutubeVideos = new ArrayList<>();
                        for (String entry : rawYoutubeIds) {
                            String[] data = entry.split("\\.");
                            if (data.length == 3) {
                                PlayedYoutubeVideo newData = new PlayedYoutubeVideo(
                                        Integer.parseInt(data[0]),
                                        data[1],
                                        Integer.parseInt(data[2]));
                                playedYoutubeVideos.add(newData);
                            } else {
                                Logger.getInstance().Warning(TAG, "Wrong Size (" + data.length + ") for " + entry);
                            }
                        }

                        _broadcastController.SendSerializableBroadcast(
                                MediaServerPlayedYoutubeBroadcast,
                                MediaServerPlayedYoutubeBundle,
                                playedYoutubeVideos);
                        break;

                    case GET_BATTERY_LEVEL:
                        String currentBatteryLevel = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaServerBatteryBroadcast,
                                MediaServerBatteryBundle,
                                currentBatteryLevel);
                        break;

                    case GET_SERVER_VERSION:
                        String currentServerVersion = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaServerServerVersionBroadcast,
                                MediaServerServerVersionBundle,
                                currentServerVersion);
                        break;

                    case GET_MEDIA_SERVER_DTO:
                        String mediaServerDataString = responseData[responseData.length - 1];
                        String[] mediaServerData = mediaServerDataString.split("\\|");
                        if (mediaServerData.length == 16) {
                            String serverIp = mediaServerData[0];
                            MediaServerSelection mediaServerSelection = MediaServerSelection.GetByIp(serverIp);

                            String batteryLevelString = mediaServerData[1];
                            int batteryLevel = -1;
                            try {
                                batteryLevel = Integer.parseInt(batteryLevelString);
                            } catch (Exception ex) {
                                Logger.getInstance().Error(TAG, ex.getMessage());
                            }

                            String socketName = mediaServerData[2];
                            String socketStateString = mediaServerData[3];
                            boolean socketState = socketStateString.contains("1");

                            String volumeString = mediaServerData[4];
                            int volume = -1;
                            try {
                                volume = Integer.parseInt(volumeString);
                            } catch (Exception ex) {
                                Logger.getInstance().Error(TAG, ex.getMessage());
                            }

                            String youtubeId = mediaServerData[5];

                            String youtubeIsPlayingString = mediaServerData[6];
                            boolean youtubeIsPlaying = youtubeIsPlayingString.contains("1");

                            String youtubeVideoCurrentPlayTimeString = mediaServerData[7];
                            int youtubeVideoCurrentPlayTime = -1;
                            try {
                                youtubeVideoCurrentPlayTime = Integer.parseInt(youtubeVideoCurrentPlayTimeString);
                            } catch (Exception ex) {
                                Logger.getInstance().Error(TAG, ex.getMessage());
                            }

                            String youtubeVideoDurationString = mediaServerData[8];
                            int youtubeVideoDuration = -1;
                            try {
                                youtubeVideoDuration = Integer.parseInt(youtubeVideoDurationString);
                            } catch (Exception ex) {
                                Logger.getInstance().Error(TAG, ex.getMessage());
                            }

                            String alreadyPlayedYoutubeData = mediaServerData[9];
                            String[] rawPlayedYoutubeIds = alreadyPlayedYoutubeData.split("\\;");
                            ArrayList<PlayedYoutubeVideo> alreadyPlayedYoutubeVideos = new ArrayList<>();
                            for (String entry : rawPlayedYoutubeIds) {
                                String[] data = entry.split("\\.");
                                if (data.length == 3) {
                                    PlayedYoutubeVideo newData = new PlayedYoutubeVideo(
                                            Integer.parseInt(data[0]),
                                            data[1],
                                            Integer.parseInt(data[2]));
                                    alreadyPlayedYoutubeVideos.add(newData);
                                } else {
                                    Logger.getInstance().Warning(TAG, "Wrong Size (" + data.length + ") for " + entry);
                                }
                            }

                            String radioStreamIdString = mediaServerData[10];
                            int radioStreamId = -1;
                            try {
                                radioStreamId = Integer.parseInt(radioStreamIdString);
                            } catch (Exception exception) {
                                Logger.getInstance().Error(TAG, exception.getMessage());
                            }

                            String isRadioStreamPlayingString = mediaServerData[11];
                            boolean isRadioStreamPlaying = isRadioStreamPlayingString.contains("1");

                            String isSeaSSoundPlayingString = mediaServerData[12];
                            boolean isSeaSSoundPlaying = isSeaSSoundPlayingString.contains("1");
                            String seaSoundCountdownString = mediaServerData[13];
                            int seaSoundCountdownSec = -1;
                            try {
                                seaSoundCountdownSec = Integer.parseInt(seaSoundCountdownString);
                            } catch (Exception exception) {
                                Logger.getInstance().Error(TAG, exception.getMessage());
                            }

                            String serverVersion = mediaServerData[14];

                            String screenBrightnessString = mediaServerData[15];
                            int screenBrightness = -1;
                            try {
                                screenBrightness = Integer.parseInt(screenBrightnessString);
                            } catch (Exception exception) {
                                Logger.getInstance().Error(TAG, exception.getMessage());
                            }

                            _mediaServerData = new MediaServerData(
                                    mediaServerSelection,
                                    batteryLevel,
                                    socketName,
                                    socketState,
                                    volume,
                                    youtubeId,
                                    youtubeIsPlaying,
                                    youtubeVideoCurrentPlayTime,
                                    youtubeVideoDuration,
                                    alreadyPlayedYoutubeVideos,
                                    isRadioStreamPlaying,
                                    radioStreamId,
                                    isSeaSSoundPlaying,
                                    seaSoundCountdownSec,
                                    serverVersion,
                                    screenBrightness);

                            _broadcastController.SendSerializableBroadcast(
                                    MediaServerDownloadFinishedBroadcast,
                                    MediaServerDownloadFinishedBundle,
                                    new MediaServerDownloadFinishedContent(_mediaServerData, true, Tools.CompressStringToByteArray("Download finished")));

                        } else {
                            Logger.getInstance().Warning(TAG, String.format("Length %s for MediaServerData is invalid!", mediaServerData.length));
                        }
                        break;

                    default:
                        break;
                }
            } else {
                Logger.getInstance().Warning(TAG, "responseAction is null!");
            }
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        if (response.length() == 0) {
            response = "Download for mediaserver failed!";
        }

        _broadcastController.SendSerializableBroadcast(
                MediaServerDownloadFinishedBroadcast,
                MediaServerDownloadFinishedBundle,
                new MediaServerDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }
}
