package guepardoapps.mediamirror.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.controller.UserInformationController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.constants.Timeouts;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.enums.RSSFeed;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.enums.YoutubeId;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.CenterModel;
import guepardoapps.mediamirror.common.models.RSSModel;
import guepardoapps.mediamirror.common.models.YoutubeDatabaseModel;
import guepardoapps.mediamirror.controller.CenterViewController;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.controller.ScreenController;

public class DataHandler {
    private static final String TAG = DataHandler.class.getSimpleName();

    private Context _context;

    private BroadcastController _broadcastController;
    private CenterViewController _centerViewController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;
    private ScreenController _screenController;
    private UserInformationController _userInformationController;

    private int _batteryLevel = -1;
    private SerializableList<WirelessSocket> _socketList;
    private String _lastYoutubeId = YoutubeId.THE_GOOD_LIFE_STREAM.GetYoutubeId();

    private boolean _seaSoundIsRunning;
    private long _seaSoundStartTime = -1;
    private Handler _seaSoundHandler = new Handler();
    private Runnable _seaSoundRunnable = new Runnable() {
        @Override
        public void run() {
            CenterModel goodNightModel = new CenterModel(
                    true, "Sleep well!",
                    false, "",
                    false, "",
                    false, RadioStreams.BAYERN_3);

            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.SHOW_CENTER_MODEL,
                    Bundles.CENTER_MODEL,
                    goodNightModel);

            _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
            _seaSoundIsRunning = false;
            _seaSoundStartTime = -1;
        }
    };

    public DataHandler(@NonNull Context context) {
        _context = context;

        _broadcastController = new BroadcastController(_context);
        _centerViewController = CenterViewController.getInstance();
        _mediaVolumeController = MediaVolumeController.getInstance();
        _mediaVolumeController.Initialize(_context);
        _receiverController = new ReceiverController(_context);
        _screenController = new ScreenController(_context);
        _userInformationController = new UserInformationController(_context);

        _receiverController.RegisterReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            }
        }, new String[]{Intent.ACTION_BATTERY_CHANGED});
        _receiverController.RegisterReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                @SuppressWarnings("unchecked")
                SerializableList<WirelessSocket> socketList = (SerializableList<WirelessSocket>) intent.getSerializableExtra(Bundles.SOCKET_LIST);
                if (socketList != null) {
                    _socketList = socketList;
                }
            }
        }, new String[]{Broadcasts.SOCKET_LIST});
    }

    public String PerformAction(@NonNull String command) {
        if (command.startsWith("ACTION:")) {
            MediaServerAction action = convertCommandToAction(command);

            if (action != null) {
                String data = convertCommandToData(command);

                switch (action) {
                    case SHOW_YOUTUBE_VIDEO:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        if (data.length() < 4) {
                            int youtubeIdInt = -1;
                            try {
                                youtubeIdInt = Integer.parseInt(data);

                            } catch (Exception exception) {
                                Logger.getInstance().Error(TAG, exception.toString());
                                Logger.getInstance().Warning(TAG, "Setting youtubeId to 0!");
                                youtubeIdInt = 0;

                            } finally {
                                YoutubeId youtubeId = YoutubeId.GetById(youtubeIdInt);
                                if (youtubeId == null) {
                                    Logger.getInstance().Warning(TAG, "youtubeId is null! Setting to default");
                                    youtubeId = YoutubeId.THE_GOOD_LIFE_STREAM;
                                }

                                CenterModel youtubeModel = new CenterModel(
                                        false, "",
                                        true, youtubeId.GetYoutubeId(),
                                        false, "",
                                        false, RadioStreams.BAYERN_3);

                                _broadcastController.SendSerializableBroadcast(
                                        Broadcasts.SHOW_CENTER_MODEL,
                                        Bundles.CENTER_MODEL,
                                        youtubeModel);

                                _lastYoutubeId = youtubeId.GetYoutubeId();
                            }
                        } else if (data.length() == 11) {
                            CenterModel youtubeModel = new CenterModel(
                                    false, "",
                                    true, data,
                                    false, "",
                                    false, RadioStreams.BAYERN_3);
                            _broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, youtubeModel);
                            _lastYoutubeId = data;
                        } else {
                            Logger.getInstance().Warning(TAG, "Wrong size for data of youtube id!");
                        }
                        break;

                    case PLAY_YOUTUBE_VIDEO:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        if (data != null) {
                            if (data.length() > 0) {
                                _lastYoutubeId = data;
                                _broadcastController.SendStringBroadcast(Broadcasts.PLAY_VIDEO, Bundles.YOUTUBE_ID, data);
                            } else {
                                _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_VIDEO);
                            }
                        } else {
                            _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_VIDEO);
                        }
                        break;

                    case PAUSE_YOUTUBE_VIDEO:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PAUSE_VIDEO);
                        break;

                    case STOP_YOUTUBE_VIDEO:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.STOP_VIDEO);
                        break;

                    case GET_SAVED_YOUTUBE_IDS:
                        ArrayList<YoutubeDatabaseModel> loadedList = _centerViewController.GetYoutubeIds();
                        loadedList.sort((elementOne, elementTwo) -> Integer.valueOf(elementTwo.GetPlayCount()).compareTo(elementOne.GetPlayCount()));

                        StringBuilder answer = new StringBuilder();
                        for (YoutubeDatabaseModel entry : loadedList) {
                            answer.append(entry.GetCommunicationString());
                        }

                        return action.toString() + ":" + answer;

                    case SET_YOUTUBE_PLAY_POSITION:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        int positionPercent = -1;
                        try {
                            positionPercent = Integer.parseInt(data);
                        } catch (Exception ex) {
                            Logger.getInstance().Error(TAG, ex.getMessage());
                        }

                        _broadcastController.SendIntBroadcast(
                                Broadcasts.SET_VIDEO_POSITION,
                                Bundles.VIDEO_POSITION_PERCENT,
                                positionPercent);

                        break;

                    case SHOW_RADIO_STREAM:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        RadioStreams radioStream = RadioStreams.BAYERN_3;
                        try {
                            int radioStreamId = Integer.parseInt(data);
                            radioStream = RadioStreams.GetById(radioStreamId);
                        } catch (Exception exception) {
                            Logger.getInstance().Error(TAG, exception.getMessage());
                        }

                        CenterModel radioStreamModel = new CenterModel(
                                false, "",
                                false, "",
                                false, "",
                                true, radioStream);

                        _broadcastController.SendSerializableBroadcast(
                                Broadcasts.SHOW_CENTER_MODEL,
                                Bundles.CENTER_MODEL,
                                radioStreamModel);
                        break;

                    case IS_RADIO_STREAM_PLAYING:
                        return action.toString() + ":" + String.valueOf(_centerViewController.IsRadioStreamPlaying());

                    case PLAY_RADIO_STREAM:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        if (data != null) {
                            if (data.length() > 0) {
                                _lastYoutubeId = data;
                                _broadcastController.SendStringBroadcast(
                                        Broadcasts.PLAY_RADIO_STREAM,
                                        Bundles.RADIO_STREAM_ID,
                                        data);
                            } else {
                                _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_RADIO_STREAM);
                            }
                        } else {
                            _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_RADIO_STREAM);
                        }
                        break;

                    case STOP_RADIO_STREAM:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.STOP_RADIO_STREAM);
                        break;

                    case PLAY_SEA_SOUND:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        int timeOut;
                        try {
                            timeOut = Integer.parseInt(data) * 60 * 1000;
                        } catch (Exception exception) {
                            Logger.getInstance().Error(TAG, exception.getMessage());
                            Toasty.error(_context, exception.toString(), Toast.LENGTH_LONG).show();
                            timeOut = Timeouts.SEA_SOUND_STOP;
                        }

                        CenterModel playSeaSoundModel = new CenterModel(
                                false, "",
                                true, YoutubeId.SEA_SOUND.GetYoutubeId(),
                                false, "",
                                false, RadioStreams.BAYERN_3);
                        _broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, playSeaSoundModel);

                        _seaSoundHandler.postDelayed(_seaSoundRunnable, timeOut);
                        _seaSoundIsRunning = true;
                        _seaSoundStartTime = System.currentTimeMillis();
                        break;

                    case STOP_SEA_SOUND:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        CenterModel stopSeaSoundModel = new CenterModel(
                                true, "",
                                false, "",
                                false, "",
                                false, RadioStreams.BAYERN_3);
                        _broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, stopSeaSoundModel);

                        _seaSoundHandler.removeCallbacks(_seaSoundRunnable);
                        _seaSoundIsRunning = false;
                        _seaSoundStartTime = -1;
                        break;

                    case IS_SEA_SOUND_PLAYING:
                        String seaSSoundIsPlaying;

                        if (_seaSoundIsRunning) {
                            seaSSoundIsPlaying = "1";
                        } else {
                            seaSSoundIsPlaying = "0";
                        }

                        return action.toString() + ":" + seaSSoundIsPlaying;

                    case GET_SEA_SOUND_COUNTDOWN:
                        if (_seaSoundStartTime == -1) {
                            return action.toString() + ":-1";
                        }
                        long currentTime = System.currentTimeMillis();
                        long differenceTimeSec = (Timeouts.SEA_SOUND_STOP - (currentTime - _seaSoundStartTime)) / 1000;
                        while (differenceTimeSec < 0) {
                            differenceTimeSec += 24 * 60 * 60;
                        }
                        return action.toString() + ":" + differenceTimeSec;

                    case SHOW_CENTER_TEXT:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        CenterModel centerTextModel = new CenterModel(
                                true, data,
                                false, "",
                                false, "",
                                false, RadioStreams.BAYERN_3);
                        _broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, centerTextModel);
                        break;

                    case SET_RSS_FEED:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        int feedIdInt = -1;
                        try {
                            feedIdInt = Integer.parseInt(data);
                        } catch (Exception e) {
                            Logger.getInstance().Error(TAG, e.getMessage());
                            Logger.getInstance().Warning(TAG, "Setting feedIdInt to 0!");
                            feedIdInt = 0;
                        } finally {
                            RSSFeed rssFeed = RSSFeed.GetById(feedIdInt);

                            if (rssFeed == null) {
                                Logger.getInstance().Warning(TAG, "rssFeed is null! Setting to default");
                                rssFeed = RSSFeed.DEFAULT;
                            }

                            RSSModel rSSFeedModel = new RSSModel(rssFeed, true);
                            _broadcastController.SendSerializableBroadcast(Broadcasts.PERFORM_RSS_UPDATE, Bundles.RSS_MODEL, rSSFeedModel);
                        }
                        break;

                    case RESET_RSS_FEED:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.RESET_RSS_FEED);
                        break;

                    case UPDATE_CURRENT_WEATHER:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CURRENT_WEATHER_UPDATE);
                        break;

                    case UPDATE_FORECAST_WEATHER:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_FORECAST_WEATHER_UPDATE);
                        break;

                    case UPDATE_RASPBERRY_TEMPERATURE:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_TEMPERATURE_UPDATE);
                        break;

                    case UPDATE_IP_ADDRESS:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_IP_ADDRESS_UPDATE);
                        break;

                    case UPDATE_BIRTHDAY_ALARM:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
                        break;

                    case UPDATE_CALENDAR_ALARM:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
                        break;

                    case INCREASE_VOLUME:
                        _mediaVolumeController.IncreaseVolume();
                        return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();

                    case DECREASE_VOLUME:
                        _mediaVolumeController.DecreaseVolume();
                        return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();

                    case MUTE_VOLUME:
                        _mediaVolumeController.MuteVolume();
                        return action.toString() + ":Muted";

                    case UNMUTE_VOLUME:
                        _mediaVolumeController.UnMuteVolume();
                        return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();

                    case GET_CURRENT_VOLUME:
                        return action.toString() + ":" + _mediaVolumeController.GetCurrentVolume();

                    case PLAY_ALARM:
                        // TODO implement
                        break;

                    case STOP_ALARM:
                        // TODO implement
                        break;

                    case INCREASE_SCREEN_BRIGHTNESS:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendIntBroadcast(
                                Broadcasts.ACTION_SCREEN_BRIGHTNESS,
                                Bundles.SCREEN_BRIGHTNESS,
                                ScreenController.INCREASE);

                        return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());

                    case DECREASE_SCREEN_BRIGHTNESS:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendIntBroadcast(Broadcasts.ACTION_SCREEN_BRIGHTNESS, Bundles.SCREEN_BRIGHTNESS, ScreenController.DECREASE);
                        return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());

                    case GET_SCREEN_BRIGHTNESS:
                        return action.toString() + ":" + String.valueOf(_screenController.GetCurrentBrightness());

                    case SCREEN_ON:
                        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_ON);
                        break;

                    case SCREEN_OFF:
                        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
                        break;

                    case SCREEN_NORMAL:
                        if (!_screenController.IsScreenOn()) {
                            Logger.getInstance().Error(TAG, "Screen is not enabled!");
                            return "Error:Screen is not enabled!";
                        }

                        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_NORMAL);
                        break;

                    case GET_BATTERY_LEVEL:
                        return action.toString() + ":" + String.valueOf(_batteryLevel);

                    case GET_SERVER_VERSION:
                        return action.toString() + ":" + _context.getString(R.string.serverVersion);

                    case GET_MEDIA_SERVER_DTO:
                        String serverIp = _userInformationController.GetIp();
                        String batteryLevel = String.valueOf(_batteryLevel);
                        String socketName = MediaServerSelection.GetByIp(serverIp).GetSocket();
                        String socketState = "0";

                        if (_socketList != null) {
                            for (int index = 0; index < _socketList.getSize(); index++) {
                                WirelessSocket socket = _socketList.getValue(index);
                                if (socket.GetName().contains(socketName)) {
                                    socketState = socket.IsActivated() ? "1" : "0";
                                    break;
                                }
                            }
                        } else {
                            Logger.getInstance().Warning(TAG, "Cannot search socket state! _socketList is null!");
                        }

                        String volume = String.valueOf(_mediaVolumeController.GetCurrentVolume());

                        String youtubeId = _lastYoutubeId;
                        String isYoutubePlaying = _centerViewController.IsYoutubePlaying() ? "1" : "0";
                        String youtubeCurrentPlayPosition = String.valueOf(_centerViewController.GetCurrentPlayPosition());
                        String youtubeCurrentVideoDuration = String.valueOf(_centerViewController.GetYoutubeDuration());

                        StringBuilder playedYoutubeIds = new StringBuilder();

                        ArrayList<YoutubeDatabaseModel> loadedListFromDb = _centerViewController.GetYoutubeIds();
                        loadedListFromDb.sort((elementOne, elementTwo) -> Integer.valueOf(elementTwo.GetPlayCount()).compareTo(elementOne.GetPlayCount()));

                        for (YoutubeDatabaseModel entry : loadedListFromDb) {
                            playedYoutubeIds.append(entry.GetCommunicationString());
                        }

                        String radioStreamId = String.valueOf(_centerViewController.GetRadioStreamId());
                        String isRadioStreamPlaying = _centerViewController.IsRadioStreamPlaying() ? "1" : "0";

                        String isSeaSSoundPlaying = _seaSoundIsRunning ? "1" : "0";
                        String seaSoundCountdown;
                        if (!_seaSoundIsRunning) {
                            seaSoundCountdown = "-1";
                        } else {
                            if (_seaSoundStartTime == -1) {
                                seaSoundCountdown = "-1";
                            } else {
                                long currentTimeMSec = System.currentTimeMillis();
                                long differenceTimeInSec = (currentTimeMSec - _seaSoundStartTime) / 1000;
                                while (differenceTimeInSec < 0) {
                                    differenceTimeInSec += 24 * 60 * 60;
                                }
                                seaSoundCountdown = String.valueOf(differenceTimeInSec);
                            }
                        }

                        String serverVersion = _context.getString(R.string.serverVersion);
                        String screenBrightness = String.valueOf(_screenController.GetCurrentBrightness());

                        String mediaMirrorDto = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                                serverIp, batteryLevel, socketName,
                                socketState, volume, youtubeId,
                                isYoutubePlaying, youtubeCurrentPlayPosition, youtubeCurrentVideoDuration,
                                playedYoutubeIds.toString(), radioStreamId, isRadioStreamPlaying,
                                isSeaSSoundPlaying, seaSoundCountdown, serverVersion,
                                screenBrightness);

                        return action.toString() + ":" + mediaMirrorDto;

                    default:
                        Logger.getInstance().Warning(TAG, "Action not handled!\n" + action.toString());
                        return "Action not handled!\n" + action.toString();
                }
                return "OK:Command performed:" + action.toString();
            } else {
                Logger.getInstance().Warning(TAG, "Action failed to be converted! Is null!\n" + command);
                return "Action failed to be converted! Is null!\n" + command;
            }
        } else {
            Logger.getInstance().Warning(TAG, "Command has wrong format!\n" + command);
            return "Command has wrong format!\n" + command;
        }
    }

    public void Dispose() {
        _mediaVolumeController.Dispose();
        _receiverController.Dispose();
    }

    private MediaServerAction convertCommandToAction(@NonNull String command) {
        String[] entries = command.split("\\&");
        if (entries.length == 2) {
            String action = entries[0];
            action = action.replace("ACTION:", "");
            return MediaServerAction.GetByString(action);
        }

        Logger.getInstance().Warning(TAG, "Wrong size of entries: " + String.valueOf(entries.length));
        return MediaServerAction.NULL;
    }

    private String convertCommandToData(@NonNull String command) {
        String[] entries = command.split("\\&");
        if (entries.length == 2) {
            String data = entries[1];
            data = data.replace("DATA:", "");
            return data;
        }
        Logger.getInstance().Warning(TAG, "Wrong size of entries: " + String.valueOf(entries.length));
        return "";
    }
}
