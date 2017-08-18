package guepardoapps.lucahome.common.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.basic.utils.Tools;
import guepardoapps.lucahome.common.classes.MediaMirrorData;
import guepardoapps.lucahome.common.classes.PlayedYoutubeVideo;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;
import guepardoapps.lucahome.common.tasks.ClientTask;

public class MediaMirrorService {
    public static class MediaMirrorDownloadFinishedContent extends ObjectChangeFinishedContent {
        public MediaMirrorData MediaMirror;

        public MediaMirrorDownloadFinishedContent(MediaMirrorData mediaMirror, boolean succcess, @NonNull byte[] response) {
            super(succcess, response);
            MediaMirror = mediaMirror;
        }
    }

    public static final String MediaMirrorIntent = "MediaMirrorIntent";

    public static final String MediaMirrorDownloadFinishedBroadcast = "guepardoapps.lucahome.data.service.mediamirror.download.finished";
    public static final String MediaMirrorDownloadFinishedBundle = "MediaMirrorDownloadFinishedBundle";

    public static final String MediaMirrorVolumeBroadcast = "guepardoapps.lucahome.data.service.mediamirror.volume";
    public static final String MediaMirrorVolumeBundle = "MediaMirrorVolumeBundle";

    public static final String MediaMirrorBrightnessBroadcast = "guepardoapps.lucahome.data.service.mediamirror.brightness";
    public static final String MediaMirrorBrightnessBundle = "MediaMirrorBrightnessBundle";

    public static final String MediaMirrorPlayedYoutubeBroadcast = "guepardoapps.lucahome.data.service.mediamirror.playedyoutube";
    public static final String MediaMirrorPlayedYoutubeBundle = "MediaMirrorPlayedYoutubeBundle";

    public static final String MediaMirrorBatteryBroadcast = "guepardoapps.lucahome.data.service.mediamirror.battery";
    public static final String MediaMirrorBatteryBundle = "MediaMirrorBatteryBundle";

    public static final String MediaMirrorServerVersionBroadcast = "guepardoapps.lucahome.data.service.mediamirror.serverversion";
    public static final String MediaMirrorServerVersionBundle = "MediaMirrorServerVersionBundle";

    private static final MediaMirrorService SINGLETON = new MediaMirrorService();
    private boolean _isInitialized;

    private static final String TAG = MediaMirrorService.class.getSimpleName();
    private Logger _logger;

    private BroadcastController _broadcastController;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private Context _context;
    private MediaMirrorData _mediaMirrorData;

    private BroadcastReceiver _mediaMirrorDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mediaMirrorDownloadFinishedReceiver");
            String response = intent.getStringExtra(ClientTask.ClientTaskBundle);
            if (response != null) {
                try {
                    handleResponse(response);
                } catch (Exception exception) {
                    _logger.Error(exception.getMessage());
                }
            } else {
                _logger.Error("Received null response!");
            }
        }
    };

    private MediaMirrorService() {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");
    }

    public static MediaMirrorService getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("initialize");

        if (_isInitialized) {
            _logger.Warning("Already initialized!");
            return;
        }

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _networkController = new NetworkController(_context);
        _receiverController = new ReceiverController(_context);
        _settingsController = SettingsController.getInstance();

        _receiverController.RegisterReceiver(_mediaMirrorDownloadFinishedReceiver, new String[]{ClientTask.ClientTaskBroadcast});

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public MediaMirrorData GetMediaMirrorData() {
        return _mediaMirrorData;
    }

    public void SendCommand(
            @NonNull String serverIp,
            @NonNull String command,
            @NonNull String data) {
        _logger.Debug(String.format(Locale.getDefault(), "SendCommand with ServerIp %s, command %s and data %s", serverIp, command, data));

        if (!_isInitialized) {
            sendFailedDownloadBroadcast("Not initialized!");
            return;
        }

        if (!_networkController.IsHomeNetwork(_settingsController.GetHomeSsid())) {
            sendFailedDownloadBroadcast("No home network!");
            return;
        }

        String communication = "ACTION:" + command + "&DATA:" + data;

        ClientTask clientTask = new ClientTask(
                _context,
                serverIp,
                Constants.MEDIAMIRROR_SERVERPORT,
                communication);
        clientTask.execute();
    }

    private void handleResponse(@NonNull String response) {
        _logger.Debug("handleResponse");

        String[] responseData = response.split("\\:");

        if (responseData.length > 1) {
            MediaServerAction responseAction = MediaServerAction.GetByString(responseData[0]);
            if (responseAction != null) {
                _logger.Information("ResponseAction: " + responseAction.toString());

                switch (responseAction) {

                    case INCREASE_VOLUME:
                    case DECREASE_VOLUME:
                    case UNMUTE_VOLUME:
                    case GET_CURRENT_VOLUME:
                        String currentVolume = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaMirrorVolumeBroadcast,
                                MediaMirrorVolumeBundle,
                                currentVolume);
                        break;

                    case INCREASE_SCREEN_BRIGHTNESS:
                    case DECREASE_SCREEN_BRIGHTNESS:
                    case GET_SCREEN_BRIGHTNESS:
                        String currentBrightness = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaMirrorBrightnessBroadcast,
                                MediaMirrorBrightnessBundle,
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
                                _logger.Debug("Created new entry: " + newData.toString());
                                playedYoutubeVideos.add(newData);
                            } else {
                                _logger.Warning("Wrong Size (" + data.length + ") for " + entry);
                            }
                        }

                        _broadcastController.SendSerializableBroadcast(
                                MediaMirrorPlayedYoutubeBroadcast,
                                MediaMirrorPlayedYoutubeBundle,
                                playedYoutubeVideos);
                        break;

                    case GET_BATTERY_LEVEL:
                        String currentBatteryLevel = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaMirrorBatteryBroadcast,
                                MediaMirrorBatteryBundle,
                                currentBatteryLevel);
                        break;

                    case GET_SERVER_VERSION:
                        String currentServerVersion = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                MediaMirrorServerVersionBroadcast,
                                MediaMirrorServerVersionBundle,
                                currentServerVersion);
                        break;

                    case GET_MEDIAMIRROR_DTO:
                        String mediaMirrorDataString = responseData[responseData.length - 1];
                        String[] mediaMirrorData = mediaMirrorDataString.split("\\|");
                        if (mediaMirrorData.length == 16) {
                            String serverIp = mediaMirrorData[0];
                            MediaServerSelection mediaServerSelection = MediaServerSelection.GetByIp(serverIp);

                            String batteryLevelString = mediaMirrorData[1];
                            int batteryLevel = -1;
                            try {
                                batteryLevel = Integer.parseInt(batteryLevelString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            String socketName = mediaMirrorData[2];
                            String socketStateString = mediaMirrorData[3];
                            boolean socketState = socketStateString.contains("1");

                            String volumeString = mediaMirrorData[4];
                            int volume = -1;
                            try {
                                volume = Integer.parseInt(volumeString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            String youtubeId = mediaMirrorData[5];

                            String youtubeIsPlayingString = mediaMirrorData[6];
                            boolean youtubeIsPlaying = youtubeIsPlayingString.contains("1");

                            String youtubeVideoCurrentPlayTimeString = mediaMirrorData[7];
                            int youtubeVideoCurrentPlayTime = -1;
                            try {
                                youtubeVideoCurrentPlayTime = Integer.parseInt(youtubeVideoCurrentPlayTimeString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            String youtubeVideoDurationString = mediaMirrorData[8];
                            int youtubeVideoDuration = -1;
                            try {
                                youtubeVideoDuration = Integer.parseInt(youtubeVideoDurationString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            String alreadyPlayedYoutubeData = mediaMirrorData[9];
                            String[] rawPlayedYoutubeIds = alreadyPlayedYoutubeData.split("\\;");
                            ArrayList<PlayedYoutubeVideo> alreadyPlayedYoutubeVideos = new ArrayList<>();
                            for (String entry : rawPlayedYoutubeIds) {
                                String[] data = entry.split("\\.");
                                if (data.length == 3) {
                                    PlayedYoutubeVideo newData = new PlayedYoutubeVideo(
                                            Integer.parseInt(data[0]),
                                            data[1],
                                            Integer.parseInt(data[2]));
                                    _logger.Debug("Created new entry: " + newData.toString());
                                    alreadyPlayedYoutubeVideos.add(newData);
                                } else {
                                    _logger.Warning("Wrong Size (" + data.length + ") for " + entry);
                                }
                            }

                            String radioStreamIdString = mediaMirrorData[10];
                            int radioStreamId = -1;
                            try {
                                radioStreamId = Integer.parseInt(radioStreamIdString);
                            } catch (Exception exception) {
                                _logger.Error(exception.toString());
                            }

                            String isRadioStreamPlayingString = mediaMirrorData[11];
                            boolean isRadioStreamPlaying = isRadioStreamPlayingString.contains("1");

                            String isSeaSSoundPlayingString = mediaMirrorData[12];
                            boolean isSeaSSoundPlaying = isSeaSSoundPlayingString.contains("1");
                            String seaSoundCountdownString = mediaMirrorData[13];
                            int seaSoundCountdownSec = -1;
                            try {
                                seaSoundCountdownSec = Integer.parseInt(seaSoundCountdownString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            String serverVersion = mediaMirrorData[14];

                            String screenBrightnessString = mediaMirrorData[15];
                            int screenBrightness = -1;
                            try {
                                screenBrightness = Integer.parseInt(screenBrightnessString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            _mediaMirrorData = new MediaMirrorData(
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
                                    MediaMirrorDownloadFinishedBroadcast,
                                    MediaMirrorDownloadFinishedBundle,
                                    new MediaMirrorDownloadFinishedContent(_mediaMirrorData, true, Tools.CompressStringToByteArray(response)));

                        } else {
                            _logger.Warning(String.format("Length %s for MediaMirrorData is invalid!", mediaMirrorData.length));
                        }
                        break;

                    default:
                        break;
                }
            } else {
                _logger.Warning("responseAction is null!");
            }
        }
    }

    private void sendFailedDownloadBroadcast(@NonNull String response) {
        _broadcastController.SendSerializableBroadcast(
                MediaMirrorDownloadFinishedBroadcast,
                MediaMirrorDownloadFinishedBundle,
                new MediaMirrorDownloadFinishedContent(null, false, Tools.CompressStringToByteArray(response)));
    }
}
