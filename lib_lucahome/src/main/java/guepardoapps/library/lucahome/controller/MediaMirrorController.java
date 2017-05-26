package guepardoapps.library.lucahome.controller;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.dto.PlayedYoutubeVideoDto;
import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.tasks.ClientTask;

public class MediaMirrorController {

    private static final String TAG = MediaMirrorController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _initialized;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private BroadcastReceiver _clientTaskResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_clientTaskResponseReceiver onReceive");

            String response = intent.getStringExtra(guepardoapps.library.toolset.common.Bundles.CLIENT_TASK_RESPONSE);
            if (response != null) {
                try {
                    handleResponse(response);
                } catch (Exception ex) {
                    _logger.Error(ex.toString());
                }
            } else {
                _logger.Error("Received null response!");
            }
        }
    };

    public MediaMirrorController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
    }

    public void Initialize() {
        _logger.Debug("Initialize");
        _receiverController.RegisterReceiver(_clientTaskResponseReceiver, new String[]{guepardoapps.library.toolset.common.Broadcasts.CLIENT_TASK_RESPONSE});
        _initialized = true;
    }

    public boolean SendCommand(
            @NonNull String serverIp,
            @NonNull String command,
            @NonNull String data) {
        _logger.Debug("SendServerCommand: " + command + " with data " + data);

        if (!_initialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        String communication = "ACTION:" + command + "&DATA:" + data;
        _logger.Debug("Communication is: " + communication);

        ClientTask clientTask = new ClientTask(
                _context,
                serverIp,
                Constants.MEDIAMIRROR_SERVERPORT,
                communication);
        clientTask.execute();

        return true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");
        _receiverController.Dispose();
        _initialized = false;
    }

    private void handleResponse(@NonNull String response) {
        _logger.Debug("handleResponse");

        String[] responseData = response.split("\\:");

        if (responseData.length > 1) {
            ServerAction responseAction = ServerAction.GetByString(responseData[0]);
            if (responseAction != null) {
                _logger.Info("ResponseAction: " + responseAction.toString());

                switch (responseAction) {

                    case INCREASE_VOLUME:
                    case DECREASE_VOLUME:
                    case UNMUTE_VOLUME:
                    case GET_CURRENT_VOLUME:
                        String currentVolume = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_VOLUME,
                                Bundles.CURRENT_RECEIVED_VOLUME,
                                currentVolume);
                        break;

                    case INCREASE_SCREEN_BRIGHTNESS:
                    case DECREASE_SCREEN_BRIGHTNESS:
                    case GET_SCREEN_BRIGHTNESS:
                        String currentBrightness = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_BRIGHTNESS,
                                Bundles.CURRENT_RECEIVED_BRIGHTNESS,
                                currentBrightness);
                        break;

                    case GET_SAVED_YOUTUBE_IDS:
                        String playedYoutubeData = responseData[responseData.length - 1];
                        String[] rawYoutubeIds = playedYoutubeData.split("\\;");

                        ArrayList<PlayedYoutubeVideoDto> playedYoutubeVideos = new ArrayList<>();
                        for (String entry : rawYoutubeIds) {
                            String[] data = entry.split("\\.");
                            if (data.length == 3) {
                                PlayedYoutubeVideoDto newData = new PlayedYoutubeVideoDto(
                                        Integer.parseInt(data[0]),
                                        data[1],
                                        Integer.parseInt(data[2]));
                                _logger.Debug("Created new entry: " + newData.toString());
                                playedYoutubeVideos.add(newData);
                            } else {
                                _logger.Warn("Wrong Size (" + data.length + ") for " + entry);
                            }
                        }

                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.PLAYED_YOUTUBE_VIDEOS,
                                Bundles.PLAYED_YOUTUBE_VIDEOS,
                                playedYoutubeVideos);
                        break;

                    case GET_BATTERY_LEVEL:
                        String currentBatteryLevel = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_BATTERY_LEVEL,
                                Bundles.CURRENT_BATTERY_LEVEL,
                                currentBatteryLevel);
                        break;

                    case GET_SERVER_VERSION:
                        String currentServerVersion = responseData[responseData.length - 1];
                        _broadcastController.SendStringBroadcast(
                                Broadcasts.MEDIAMIRROR_SERVER_VERSION,
                                Bundles.CURRENT_SERVER_VERSION,
                                currentServerVersion);
                        break;

                    case GET_MEDIAMIRROR_DTO:
                        String mediaMirrorDataString = responseData[responseData.length - 1];
                        String[] mediaMirrorData = mediaMirrorDataString.split("\\|");
                        if (mediaMirrorData.length == 14) {
                            String serverIp = mediaMirrorData[0];
                            MediaMirrorSelection mediaMirrorSelection = MediaMirrorSelection.GetByIp(serverIp);

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
                            ArrayList<PlayedYoutubeVideoDto> alreadyPlayedYoutubeVideos = new ArrayList<>();
                            for (String entry : rawPlayedYoutubeIds) {
                                String[] data = entry.split("\\.");
                                if (data.length == 3) {
                                    PlayedYoutubeVideoDto newData = new PlayedYoutubeVideoDto(
                                            Integer.parseInt(data[0]),
                                            data[1],
                                            Integer.parseInt(data[2]));
                                    _logger.Debug("Created new entry: " + newData.toString());
                                    alreadyPlayedYoutubeVideos.add(newData);
                                } else {
                                    _logger.Warn("Wrong Size (" + data.length + ") for " + entry);
                                }
                            }

                            String isSeaSSoundPlayingString = mediaMirrorData[10];
                            boolean isSeaSSoundPlaying = isSeaSSoundPlayingString.contains("1");
                            String seaSoundCountdownString = mediaMirrorData[11];
                            int seaSoundCountdownSec = -1;
                            try {
                                seaSoundCountdownSec = Integer.parseInt(seaSoundCountdownString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            String serverVersion = mediaMirrorData[12];

                            String screenBrightnessString = mediaMirrorData[13];
                            int screenBrightness = -1;
                            try {
                                screenBrightness = Integer.parseInt(screenBrightnessString);
                            } catch (Exception ex) {
                                _logger.Error(ex.toString());
                            }

                            MediaMirrorViewDto mediaMirrorDto = new MediaMirrorViewDto(mediaMirrorSelection,
                                    batteryLevel, socketName, socketState, volume, youtubeId, youtubeIsPlaying,
                                    youtubeVideoCurrentPlayTime, youtubeVideoDuration, alreadyPlayedYoutubeVideos,
                                    isSeaSSoundPlaying, seaSoundCountdownSec, serverVersion, screenBrightness);

                            _broadcastController.SendSerializableBroadcast(
                                    Broadcasts.MEDIAMIRROR_VIEW_DTO,
                                    Bundles.MEDIAMIRROR_VIEW_DTO,
                                    mediaMirrorDto);
                        } else {
                            _logger.Warn(String.format("Length %s for MediaMirrorData is invalid!", mediaMirrorData.length));
                        }
                        break;

                    default:
                        break;
                }
            } else {
                _logger.Warn("responseAction is null!");
            }
        }
    }
}
