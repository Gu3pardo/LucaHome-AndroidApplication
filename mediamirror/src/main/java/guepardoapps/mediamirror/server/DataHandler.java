package guepardoapps.mediamirror.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.CommandController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.controller.UserInformationController;
import guepardoapps.lucahome.basic.utils.Logger;

import guepardoapps.lucahome.common.classes.mediaserver.MediaServerInformationData;
import guepardoapps.lucahome.common.classes.mediaserver.RadioStreamData;
import guepardoapps.lucahome.common.classes.mediaserver.SleepTimerData;
import guepardoapps.lucahome.common.classes.mediaserver.YoutubeData;
import guepardoapps.lucahome.common.constants.Timeouts;
import guepardoapps.lucahome.common.enums.MediaServerAction;
import guepardoapps.lucahome.common.enums.MediaServerSelection;
import guepardoapps.lucahome.common.enums.RSSFeed;
import guepardoapps.lucahome.common.enums.RadioStreams;
import guepardoapps.lucahome.common.enums.YoutubeId;
import guepardoapps.lucahome.common.service.MediaServerService;

import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.CenterModel;
import guepardoapps.mediamirror.common.models.RSSModel;
import guepardoapps.mediamirror.controller.CenterViewController;
import guepardoapps.mediamirror.controller.MediaVolumeController;
import guepardoapps.mediamirror.controller.RssViewController;
import guepardoapps.mediamirror.controller.ScreenController;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class DataHandler {
    private static final String TAG = DataHandler.class.getSimpleName();

    private Context _context;

    private BroadcastController _broadcastController;
    private CommandController _commandController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;
    private ScreenController _screenController;
    private UserInformationController _userInformationController;

    private int _batteryLevel = -1;

    private boolean _sleepTimerEnabled;
    private long _sleepTimerStartTime = -1;
    private Handler _sleepTimerHandler = new Handler();
    private Runnable _sleepTimerRunnable = new Runnable() {
        @Override
        public void run() {
            CenterModel goodNightModel = new CenterModel(
                    true, "Sleep well!",
                    false, YoutubeId.NULL,
                    false, "",
                    false, RadioStreams.BAYERN_3);

            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.SHOW_CENTER_MODEL,
                    Bundles.CENTER_MODEL,
                    goodNightModel);

            _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
            _sleepTimerEnabled = false;
            _sleepTimerStartTime = -1;
        }
    };

    public DataHandler(@NonNull Context context) {
        _context = context;

        _broadcastController = new BroadcastController(_context);
        _commandController = new CommandController(_context);
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
    }

    public String PerformAction(@NonNull String communication) {
        if (communication.startsWith("COMMAND:")) {
            MediaServerAction commandAction = convertCommandToAction(communication);

            if (commandAction != null) {
                String commandData = convertCommandToData(communication);

                switch (commandAction) {
                    case YOUTUBE_PLAY:
                        return handleYoutubePlay(commandData);
                    case YOUTUBE_PAUSE:
                        return handleYoutubePause(commandData);
                    case YOUTUBE_STOP:
                        return handleYoutubeStop(commandData);
                    case YOUTUBE_SET_POSITION:
                        return handleYoutubeSetPosition(commandData);

                    case CENTER_TEXT_SET:
                        return handleCenterTextSet(commandData);

                    case RADIO_STREAM_PLAY:
                        return handleRadioStreamPlay(commandData);
                    case RADIO_STREAM_STOP:
                        return handleRadioStreamStop(commandData);

                    case MEDIA_NOTIFICATION_PLAY:
                        return handleMediaNotificationPlay(commandData);
                    case MEDIA_NOTIFICATION_STOP:
                        return handleMediaNotificationStop(commandData);

                    case SLEEP_SOUND_PLAY:
                        return handleSleepSoundPlay(commandData);
                    case SLEEP_SOUND_STOP:
                        return handleSleepSoundStop(commandData);

                    case RSS_FEED_SET:
                        return handleRssFeedSet(commandData);
                    case RSS_FEED_RESET:
                        return handleRssFeedReset(commandData);

                    case UPDATE_BIRTHDAY_ALARM:
                        return handleUpdateBirthdayAlarm(commandData);
                    case UPDATE_CALENDAR_ALARM:
                        return handleUpdateCalendarAlarm(commandData);
                    case UPDATE_CURRENT_WEATHER:
                        return handleUpdateCurrentWeather(commandData);
                    case UPDATE_FORECAST_WEATHER:
                        return handleUpdateForecastWeather(commandData);
                    case UPDATE_IP_ADDRESS:
                        return handleUpdateIpAddress(commandData);
                    case UPDATE_RASPBERRY_TEMPERATURE:
                        return handleUpdateRaspberryTemperature(commandData);

                    case VOLUME_INCREASE:
                        return handleVolumeIncrease(commandData);
                    case VOLUME_DECREASE:
                        return handleVolumeDecrease(commandData);
                    case VOLUME_MUTE:
                        return handleVolumeMute(commandData);
                    case VOLUME_UN_MUTE:
                        return handleVolumeUnMute(commandData);

                    case SCREEN_BRIGHTNESS_INCREASE:
                        return handleScreenBrightnessIncrease(commandData);
                    case SCREEN_BRIGHTNESS_DECREASE:
                        return handleScreenBrightnessDecrease(commandData);
                    case SCREEN_NORMAL:
                        return handleScreenNormal(commandData);
                    case SCREEN_ON:
                        return handleScreenOn(commandData);
                    case SCREEN_OFF:
                        return handleScreenOff(commandData);

                    case GET_CENTER_TEXT:
                        return handleGetCenterText(commandData, commandAction);
                    case GET_MEDIA_NOTIFICATION_DATA:
                        return handleGetMediaNotificationData(commandData, commandAction);
                    case GET_MEDIA_SERVER_INFORMATION_DATA:
                        return handleGetMediaServerInformation(commandData, commandAction);
                    case GET_RADIO_DATA:
                        return handleGetRadioData(commandData, commandAction);
                    case GET_RSS_FEED_DATA:
                        return handleGetRssFeedData(commandData, commandAction);
                    case GET_SLEEP_TIMER_DATA:
                        return handleGetSleepTimerData(commandData, commandAction);
                    case GET_YOUTUBE_DATA:
                        return handleGetYoutubeData(commandData, commandAction);

                    case SYSTEM_REBOOT:
                        return handleSystemReboot(commandData);
                    case SYSTEM_SHUTDOWN:
                        return handleSystemShutdown(commandData);

                    case NULL:
                    default:
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Error: No handle for %s", commandData));
                        return String.format(Locale.getDefault(), "Error: No handle for %s", commandData);
                }
            } else {
                Logger.getInstance().Warning(TAG, "Action failed to be converted! Is null!\n" + communication);
                return "Action failed to be converted! Is null!\n" + communication;
            }
        } else {
            Logger.getInstance().Warning(TAG, "Communication has wrong format!\n" + communication);
            return "Communication has wrong format!\n" + communication;
        }
    }

    public void Dispose() {
        Logger.getInstance().Debug(TAG, "Dispose");
        _mediaVolumeController.Dispose();
        _receiverController.Dispose();
    }

    private MediaServerAction convertCommandToAction(@NonNull String communication) {
        String[] entries = communication.split(MediaServerService.COMMAND_SPLIT_CHAR);
        if (entries.length == MediaServerService.COMMAND_DATA_SIZE) {
            String command = entries[MediaServerService.INDEX_COMMAND_ACTION];
            command = command.replace("COMMAND:", "");
            return MediaServerAction.GetByString(command);
        }

        Logger.getInstance().Warning(TAG, "Wrong size of entries: " + String.valueOf(entries.length));
        return MediaServerAction.NULL;
    }

    private String convertCommandToData(@NonNull String communication) {
        String[] entries = communication.split(MediaServerService.COMMAND_SPLIT_CHAR);
        if (entries.length == MediaServerService.COMMAND_DATA_SIZE) {
            String data = entries[MediaServerService.INDEX_COMMAND_DATA];
            data = data.replace("DATA:", "");
            return data;
        }
        Logger.getInstance().Warning(TAG, "Wrong size of entries: " + String.valueOf(entries.length));
        return "";
    }

    private String handleYoutubePlay(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleYoutubePlay with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.YOUTUBE_PLAY);
        }

        if (commandData.length() == 0) {
            _broadcastController.SendSimpleBroadcast(Broadcasts.PLAY_VIDEO);

        } else if (commandData.length() > 0 && commandData.length() < 4) {
            int youtubeIdInt;
            try {
                youtubeIdInt = Integer.parseInt(commandData);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
                Logger.getInstance().Warning(TAG, "Setting youtubeId to 0!");
                youtubeIdInt = 0;
            }

            YoutubeId youtubeId = YoutubeId.GetById(youtubeIdInt);
            if (youtubeId == null) {
                Logger.getInstance().Warning(TAG, "youtubeId is null! Setting to default");
                youtubeId = YoutubeId.THE_GOOD_LIFE_STREAM;
            }

            CenterModel youtubeModel = new CenterModel(
                    false, "",
                    true, youtubeId,
                    false, "",
                    false, RadioStreams.BAYERN_3);

            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.SHOW_CENTER_MODEL,
                    Bundles.CENTER_MODEL,
                    youtubeModel);

        } else if (commandData.length() == 11) {
            YoutubeId youtubeId = YoutubeId.NULL;
            youtubeId.SetYoutubeId(commandData);

            CenterModel youtubeModel = new CenterModel(
                    false, "",
                    true, youtubeId,
                    false, "",
                    false, RadioStreams.BAYERN_3);

            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.SHOW_CENTER_MODEL,
                    Bundles.CENTER_MODEL,
                    youtubeModel);

        } else {
            Logger.getInstance().Warning(TAG, "Wrong size for data of youtube id!");
            return errorResponse("Wrong size for data of youtube id", MediaServerAction.YOUTUBE_PLAY);
        }

        return handleGetYoutubeData(commandData, MediaServerAction.YOUTUBE_PLAY);
    }

    private String handleYoutubePause(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleYoutubePause with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.YOUTUBE_PAUSE);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.PAUSE_VIDEO);
        return handleGetYoutubeData(commandData, MediaServerAction.YOUTUBE_PAUSE);
    }

    private String handleYoutubeStop(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleYoutubeStop with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.YOUTUBE_STOP);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.STOP_VIDEO);
        return handleGetYoutubeData(commandData, MediaServerAction.YOUTUBE_STOP);
    }

    private String handleYoutubeSetPosition(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleYoutubeSetPosition with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.YOUTUBE_SET_POSITION);
        }

        int positionPercent = -1;
        try {
            positionPercent = Integer.parseInt(commandData);
        } catch (Exception ex) {
            Logger.getInstance().Error(TAG, ex.getMessage());
        }

        _broadcastController.SendIntBroadcast(
                Broadcasts.SET_VIDEO_POSITION,
                Bundles.VIDEO_POSITION_PERCENT,
                positionPercent);

        return handleGetYoutubeData(commandData, MediaServerAction.YOUTUBE_SET_POSITION);
    }

    private String handleCenterTextSet(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleCenterTextSet with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.CENTER_TEXT_SET);
        }

        CenterModel centerTextModel = new CenterModel(
                true, commandData,
                false, YoutubeId.NULL,
                false, "",
                false, RadioStreams.BAYERN_3);
        _broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, centerTextModel);

        return handleGetCenterText(commandData, MediaServerAction.CENTER_TEXT_SET);
    }

    private String handleRadioStreamPlay(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleRadioStreamPlay with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.RADIO_STREAM_PLAY);
        }

        RadioStreams radioStream = RadioStreams.BAYERN_3;
        try {
            int radioStreamId = Integer.parseInt(commandData);
            radioStream = RadioStreams.GetById(radioStreamId);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.getMessage());
        }

        CenterModel radioStreamModel = new CenterModel(
                false, "",
                false, YoutubeId.NULL,
                false, "",
                true, radioStream);

        _broadcastController.SendSerializableBroadcast(
                Broadcasts.SHOW_CENTER_MODEL,
                Bundles.CENTER_MODEL,
                radioStreamModel);

        return handleGetRadioData(commandData, MediaServerAction.RADIO_STREAM_PLAY);
    }

    private String handleRadioStreamStop(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleRadioStreamStop with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.RADIO_STREAM_STOP);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.STOP_RADIO_STREAM);
        return handleGetRadioData(commandData, MediaServerAction.RADIO_STREAM_STOP);
    }

    private String handleMediaNotificationPlay(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleMediaNotificationPlay with CommandData %s", commandData));
        //TODO
        return handleGetMediaNotificationData(commandData, MediaServerAction.MEDIA_NOTIFICATION_PLAY);
    }

    private String handleMediaNotificationStop(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleMediaNotificationStop with CommandData %s", commandData));
        //TODO
        return handleGetMediaNotificationData(commandData, MediaServerAction.MEDIA_NOTIFICATION_STOP);
    }

    private String handleSleepSoundPlay(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleSleepSoundPlay with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.SLEEP_SOUND_PLAY);
        }

        int timeOut;
        try {
            timeOut = Integer.parseInt(commandData) * 60 * 1000;
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            Toasty.error(_context, exception.toString(), Toast.LENGTH_LONG).show();
            timeOut = Timeouts.SEA_SOUND_STOP;
        }

        CenterModel playSleepSoundModel = new CenterModel(
                false, "",
                true, YoutubeId.SEA_SOUND,
                false, "",
                false, RadioStreams.BAYERN_3);
        _broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, playSleepSoundModel);

        _sleepTimerHandler.postDelayed(_sleepTimerRunnable, timeOut);
        _sleepTimerEnabled = true;
        _sleepTimerStartTime = System.currentTimeMillis();

        return handleGetSleepTimerData(commandData, MediaServerAction.SLEEP_SOUND_PLAY);
    }

    private String handleSleepSoundStop(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleSleepSoundStop with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.SLEEP_SOUND_STOP);
        }

        CenterModel stopSleepSoundModel = new CenterModel(
                true, "",
                false, YoutubeId.NULL,
                false, "",
                false, RadioStreams.BAYERN_3);
        _broadcastController.SendSerializableBroadcast(Broadcasts.SHOW_CENTER_MODEL, Bundles.CENTER_MODEL, stopSleepSoundModel);

        _sleepTimerHandler.removeCallbacks(_sleepTimerRunnable);
        _sleepTimerEnabled = false;
        _sleepTimerStartTime = -1;

        return handleGetSleepTimerData(commandData, MediaServerAction.SLEEP_SOUND_STOP);
    }

    private String handleRssFeedSet(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleRssFeedSet with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.RSS_FEED_SET);
        }

        int feedIdInt;
        try {
            feedIdInt = Integer.parseInt(commandData);
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.toString());
            Logger.getInstance().Warning(TAG, "Setting feedIdInt to 0!");
            feedIdInt = 0;
        }

        RSSFeed rssFeed = RSSFeed.GetById(feedIdInt);
        if (rssFeed == null) {
            Logger.getInstance().Warning(TAG, "rssFeed is null! Setting to default");
            rssFeed = RSSFeed.DEFAULT;
        }

        RSSModel rSSFeedModel = new RSSModel(rssFeed, true);
        _broadcastController.SendSerializableBroadcast(Broadcasts.PERFORM_RSS_UPDATE, Bundles.RSS_MODEL, rSSFeedModel);

        return handleGetRssFeedData(commandData, MediaServerAction.RSS_FEED_SET);
    }

    private String handleRssFeedReset(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleRssFeedReset with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.RSS_FEED_RESET);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.RESET_RSS_FEED);
        return handleGetRssFeedData(commandData, MediaServerAction.RSS_FEED_RESET);
    }

    private String handleUpdateBirthdayAlarm(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleUpdateBirthdayAlarm with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.UPDATE_BIRTHDAY_ALARM);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_BIRTHDAY_UPDATE);
        return successResponse(MediaServerAction.UPDATE_BIRTHDAY_ALARM, "-");
    }

    private String handleUpdateCalendarAlarm(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleUpdateCalendarAlarm with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.UPDATE_CALENDAR_ALARM);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
        return successResponse(MediaServerAction.UPDATE_CALENDAR_ALARM, "-");
    }

    private String handleUpdateCurrentWeather(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleUpdateCurrentWeather with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.UPDATE_CURRENT_WEATHER);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CURRENT_WEATHER_UPDATE);
        return successResponse(MediaServerAction.UPDATE_CURRENT_WEATHER, "-");
    }

    private String handleUpdateForecastWeather(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleUpdateForecastWeather with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.UPDATE_FORECAST_WEATHER);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_FORECAST_WEATHER_UPDATE);
        return successResponse(MediaServerAction.UPDATE_FORECAST_WEATHER, "-");
    }

    private String handleUpdateIpAddress(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleUpdateIpAddress with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.UPDATE_IP_ADDRESS);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_IP_ADDRESS_UPDATE);
        return successResponse(MediaServerAction.UPDATE_IP_ADDRESS, "-");
    }

    private String handleUpdateRaspberryTemperature(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleUpdateRaspberryTemperature with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.UPDATE_RASPBERRY_TEMPERATURE);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_TEMPERATURE_UPDATE);
        return successResponse(MediaServerAction.UPDATE_RASPBERRY_TEMPERATURE, "-");
    }

    private String handleVolumeIncrease(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleVolumeIncrease with CommandData %s", commandData));
        _mediaVolumeController.IncreaseVolume();
        return handleGetMediaServerInformation(commandData, MediaServerAction.VOLUME_INCREASE);
    }

    private String handleVolumeDecrease(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleVolumeDecrease with CommandData %s", commandData));
        _mediaVolumeController.DecreaseVolume();
        return handleGetMediaServerInformation(commandData, MediaServerAction.VOLUME_DECREASE);
    }

    private String handleVolumeMute(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleVolumeMute with CommandData %s", commandData));
        _mediaVolumeController.MuteVolume();
        return handleGetMediaServerInformation(commandData, MediaServerAction.VOLUME_MUTE);
    }

    private String handleVolumeUnMute(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleVolumeUnMute with CommandData %s", commandData));
        _mediaVolumeController.UnMuteVolume();
        return handleGetMediaServerInformation(commandData, MediaServerAction.VOLUME_UN_MUTE);
    }

    private String handleScreenBrightnessIncrease(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleScreenBrightnessIncrease with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.SCREEN_BRIGHTNESS_INCREASE);
        }
        _broadcastController.SendIntBroadcast(Broadcasts.ACTION_SCREEN_BRIGHTNESS, Bundles.SCREEN_BRIGHTNESS, ScreenController.INCREASE);
        return handleGetMediaServerInformation(commandData, MediaServerAction.SCREEN_BRIGHTNESS_INCREASE);
    }

    private String handleScreenBrightnessDecrease(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleScreenBrightnessDecrease with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerAction.SCREEN_BRIGHTNESS_DECREASE);
        }
        _broadcastController.SendIntBroadcast(Broadcasts.ACTION_SCREEN_BRIGHTNESS, Bundles.SCREEN_BRIGHTNESS, ScreenController.DECREASE);
        return handleGetMediaServerInformation(commandData, MediaServerAction.SCREEN_BRIGHTNESS_DECREASE);

    }

    private String handleScreenNormal(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleScreenNormal with CommandData %s", commandData));
        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_NORMAL);
        return handleGetMediaServerInformation(commandData, MediaServerAction.SCREEN_NORMAL);
    }

    private String handleScreenOn(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleScreenOn with CommandData %s", commandData));
        if (_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is already on!");
            return errorResponse("Screen is already on", MediaServerAction.SCREEN_ON);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_ON);
        return handleGetMediaServerInformation(commandData, MediaServerAction.SCREEN_ON);
    }

    private String handleScreenOff(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleScreenOff with CommandData %s", commandData));
        if (!_screenController.IsScreenOn()) {
            Logger.getInstance().Error(TAG, "Screen is already off!");
            return errorResponse("Screen is already off", MediaServerAction.SCREEN_OFF);
        }
        _broadcastController.SendSimpleBroadcast(Broadcasts.SCREEN_OFF);
        return handleGetMediaServerInformation(commandData, MediaServerAction.SCREEN_OFF);
    }

    private String handleGetCenterText(@NonNull String commandData, MediaServerAction mediaServerAction) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleGetCenterText with CommandData %s", commandData));
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerAction.GET_CENTER_TEXT, CenterViewController.getInstance().GetCenterText());
    }

    private String handleGetMediaNotificationData(@NonNull String commandData, MediaServerAction mediaServerAction) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleGetMediaNotificationData with CommandData %s", commandData));
        //TODO
        return errorResponse("Not implemented", mediaServerAction != null ? mediaServerAction : MediaServerAction.GET_MEDIA_NOTIFICATION_DATA);
    }

    private String handleGetMediaServerInformation(@NonNull String commandData, MediaServerAction mediaServerAction) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleGetMediaServerInformation with CommandData %s", commandData));
        MediaServerInformationData mediaServerInformationData = new MediaServerInformationData(
                MediaServerSelection.GetByIp(_userInformationController.GetIp()),
                _context.getString(R.string.serverVersion),
                _mediaVolumeController.GetCurrentVolume(),
                _batteryLevel,
                _screenController.GetCurrentBrightness());
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerAction.GET_MEDIA_SERVER_INFORMATION_DATA, mediaServerInformationData.GetCommunicationString());
    }

    private String handleGetRadioData(@NonNull String commandData, MediaServerAction mediaServerAction) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleGetRadioData with CommandData %s", commandData));
        RadioStreamData radioStreamData = new RadioStreamData(
                CenterViewController.getInstance().GetRadioStream(),
                CenterViewController.getInstance().IsRadioStreamPlaying());
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerAction.GET_RADIO_DATA, radioStreamData.GetCommunicationString());
    }

    private String handleGetRssFeedData(@NonNull String commandData, MediaServerAction mediaServerAction) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleGetRssFeedData with CommandData %s", commandData));
        RSSFeed rssFeed = RssViewController.getInstance().GetCurrentRssFeed();
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerAction.GET_RSS_FEED_DATA, String.valueOf(rssFeed.GetId()));
    }

    private String handleGetSleepTimerData(@NonNull String commandData, MediaServerAction mediaServerAction) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleGetSleepTimerData with CommandData %s", commandData));

        long sleepTimerCountDownSec;
        if (!_sleepTimerEnabled) {
            sleepTimerCountDownSec = -1;
        } else {
            if (_sleepTimerStartTime == -1) {
                sleepTimerCountDownSec = -1;
            } else {
                long currentTimeMSec = System.currentTimeMillis();
                sleepTimerCountDownSec = (currentTimeMSec - _sleepTimerStartTime) / 1000;
                while (sleepTimerCountDownSec < 0) {
                    sleepTimerCountDownSec += 24 * 60 * 60;
                }
            }
        }

        SleepTimerData sleepTimerData = new SleepTimerData(_sleepTimerEnabled, (int) sleepTimerCountDownSec);
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerAction.GET_SLEEP_TIMER_DATA, sleepTimerData.GetCommunicationString());
    }

    private String handleGetYoutubeData(@NonNull String commandData, MediaServerAction mediaServerAction) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleGetYoutubeData with CommandData %s", commandData));
        YoutubeData youtubeData = new YoutubeData(
                CenterViewController.getInstance().IsYoutubePlaying(),
                CenterViewController.getInstance().GetYoutubeId().GetYoutubeId(),
                CenterViewController.getInstance().GetCurrentPlayPosition(),
                CenterViewController.getInstance().GetYoutubeDuration(),
                CenterViewController.getInstance().GetYoutubeIds());
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerAction.GET_YOUTUBE_DATA, youtubeData.GetCommunicationString());
    }

    private String handleSystemReboot(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleSystemReboot with CommandData %s", commandData));
        if (_commandController.RebootDevice(3000)) {
            return successResponse(MediaServerAction.SYSTEM_REBOOT, "-");
        } else {
            return errorResponse("Cannot reboot!", MediaServerAction.SYSTEM_REBOOT);
        }
    }

    private String handleSystemShutdown(@NonNull String commandData) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "handleSystemShutdown with CommandData %s", commandData));
        if (_commandController.ShutDownDevice(3000)) {
            return successResponse(MediaServerAction.SYSTEM_SHUTDOWN, "-");
        } else {
            return errorResponse("Cannot shutdown!", MediaServerAction.SYSTEM_SHUTDOWN);
        }
    }

    private String successResponse(@NonNull MediaServerAction mediaServerAction, @NonNull String data) {
        return String.format(Locale.getDefault(), "OK%sCommand performed%s%s%s%s",
                MediaServerService.RESPONSE_SPLIT_CHAR,
                MediaServerService.RESPONSE_SPLIT_CHAR, mediaServerAction.toString(),
                MediaServerService.RESPONSE_SPLIT_CHAR, data);
    }

    private String errorResponse(@NonNull String errorMessage, @NonNull MediaServerAction mediaServerAction) {
        return String.format(Locale.getDefault(), "Error%s%s%s%s%s%s",
                MediaServerService.RESPONSE_SPLIT_CHAR, errorMessage,
                MediaServerService.RESPONSE_SPLIT_CHAR, mediaServerAction.toString(),
                MediaServerService.RESPONSE_SPLIT_CHAR, "-");
    }
}
