package guepardoapps.lucahome.common.server.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.common.classes.mediaserver.InformationData;
import guepardoapps.lucahome.common.classes.mediaserver.RadioStreamData;
import guepardoapps.lucahome.common.classes.mediaserver.SleepTimerData;
import guepardoapps.lucahome.common.classes.mediaserver.YoutubeData;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.CommandController;
import guepardoapps.lucahome.common.controller.DisplayController;
import guepardoapps.lucahome.common.controller.MediaVolumeController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.UserInformationController;
import guepardoapps.lucahome.common.controller.mediaserver.interfaces.ICenterViewController;
import guepardoapps.lucahome.common.controller.mediaserver.interfaces.IRssViewController;
import guepardoapps.lucahome.common.datatransferobjects.mediaserver.CenterViewDto;
import guepardoapps.lucahome.common.datatransferobjects.mediaserver.RssViewDto;
import guepardoapps.lucahome.common.enums.MediaServerActionType;
import guepardoapps.lucahome.common.enums.RSSFeedType;
import guepardoapps.lucahome.common.enums.RadioStreamType;
import guepardoapps.lucahome.common.enums.YoutubeIdType;
import guepardoapps.lucahome.common.services.BirthdayService;
import guepardoapps.lucahome.common.services.CalendarService;
import guepardoapps.lucahome.common.services.TemperatureService;
import guepardoapps.lucahome.common.services.IMediaServerClientService;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class MediaServerDataHandler implements IMediaServerDataHandler {
    private static final String Tag = MediaServerDataHandler.class.getSimpleName();

    private static final int CommandTimeoutMs = 3000;
    private static final int DefaultSleepTimerTimeoutMs = 15 * 60 * 1000;

    public static final String BroadcastScreenNormal = "guepardoapps.lucahome.common.server.handler.broadcast.screen.normal";
    public static final String BroadcastScreenOn = "guepardoapps.lucahome.common.server.handler.broadcast.screen.on";
    public static final String BroadcastScreenOff = "guepardoapps.lucahome.common.server.handler.broadcast.screen.off";

    public static final String BroadcastVideoPlay = "guepardoapps.lucahome.common.server.handler.broadcast.video.play";
    public static final String BroadcastVideoPause = "guepardoapps.lucahome.common.server.handler.broadcast.video.pause";
    public static final String BroadcastVideoStop = "guepardoapps.lucahome.common.server.handler.broadcast.video.stop";

    public static final String BroadcastVideoPosition = "guepardoapps.lucahome.common.server.handler.broadcast.video.position";
    public static final String BundleVideoPosition = "BundleVideoPosition";

    public static final String BroadcastRadioStreamStop = "guepardoapps.lucahome.common.server.handler.broadcast.radioStream.stop";

    public static final String BroadcastShowCenterModel = "guepardoapps.lucahome.common.server.handler.broadcast.show.centerModel";
    public static final String BundleShowCenterModel = "BundleShowCenterModel";

    public static final String BroadcastRssFeedReset = "guepardoapps.lucahome.common.server.handler.broadcast.rssFeed.reset";

    public static final String BroadcastRssFeedUpdate = "guepardoapps.lucahome.common.server.handler.broadcast.rssFeed.update";
    public static final String BundleRssFeedUpdate = "BundleRssFeedUpdate";

    public static final String BroadcastIpAddressUpdate = "guepardoapps.lucahome.common.server.handler.broadcast.ipAddress.update";

    private Context _context;

    private BroadcastController _broadcastController;
    private CommandController _commandController;
    private DisplayController _displayController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;
    private UserInformationController _userInformationController;

    private ICenterViewController _iCenterViewController;
    private IRssViewController _iRssViewController;

    private int _batteryLevel = -1;

    private boolean _sleepTimerEnabled;
    private long _sleepTimerStartTime = -1;
    private Handler _sleepTimerHandler = new Handler();
    private Runnable _sleepTimerRunnable = new Runnable() {
        @Override
        public void run() {
            CenterViewDto goodNightCenterViewDto = new CenterViewDto(
                    true, "Sleep well!",
                    false, YoutubeIdType.NULL,
                    false, "",
                    false, RadioStreamType.BAYERN_3,
                    false, "");
            _broadcastController.SendSerializableBroadcast(BroadcastShowCenterModel, BundleShowCenterModel, goodNightCenterViewDto);
            _broadcastController.SendSimpleBroadcast(BroadcastScreenOff);
            _sleepTimerEnabled = false;
            _sleepTimerStartTime = -1;
        }
    };

    @Override
    public void Initialize(@NonNull Context context) {
        _context = context;

        _broadcastController = new BroadcastController(_context);
        _commandController = new CommandController(_context);
        _displayController = new DisplayController(_context);
        _mediaVolumeController = MediaVolumeController.getInstance();
        _mediaVolumeController.Initialize(_context);
        _receiverController = new ReceiverController(_context);
        _userInformationController = new UserInformationController(_context);

        _receiverController.RegisterReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            }
        }, new String[]{Intent.ACTION_BATTERY_CHANGED});
    }

    @Override
    public void Initialize(@NonNull Context context, @NonNull ICenterViewController iCenterViewController, @NonNull IRssViewController iRssViewController) {
        Initialize(context);
        _iCenterViewController = iCenterViewController;
        _iRssViewController = iRssViewController;
    }

    @Override
    public String PerformAction(@NonNull String communication) {
        if (communication.startsWith("COMMAND:")) {
            MediaServerActionType commandAction = convertCommandToAction(communication);

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

                    case GET_INFORMATION_DATA:
                        return handleGetInformationData(commandData, commandAction);
                    case GET_NOTIFICATION_DATA:
                        return handleGetNotificationData(commandData, commandAction);
                    case GET_RADIO_STREAM_DATA:
                        return handleGetRadioStreamData(commandData, commandAction);
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
                        Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Error: No handle for %s", commandData));
                        return String.format(Locale.getDefault(), "Error: No handle for %s", commandData);
                }
            } else {
                Logger.getInstance().Warning(Tag, "Action failed to be converted! Is null!\n" + communication);
                return "Action failed to be converted! Is null!\n" + communication;
            }
        } else {
            Logger.getInstance().Warning(Tag, "Communication has wrong format!\n" + communication);
            return "Communication has wrong format!\n" + communication;
        }
    }

    @Override
    public void Dispose() {
        Logger.getInstance().Debug(Tag, "Dispose");
        _mediaVolumeController.Dispose();
        _receiverController.Dispose();
    }

    private MediaServerActionType convertCommandToAction(@NonNull String communication) {
        String[] entries = communication.split(IMediaServerClientService.CommandSplitChar);
        if (entries.length == IMediaServerClientService.CommandDataSize) {
            String command = entries[IMediaServerClientService.IndexCommandAction];
            command = command.replace("COMMAND:", "");
            return MediaServerActionType.GetByString(command);
        }

        Logger.getInstance().Warning(Tag, "Wrong size of entries: " + String.valueOf(entries.length));
        return MediaServerActionType.NULL;
    }

    private String convertCommandToData(@NonNull String communication) {
        String[] entries = communication.split(IMediaServerClientService.CommandSplitChar);
        if (entries.length == IMediaServerClientService.CommandDataSize) {
            String data = entries[IMediaServerClientService.IndexCommandData];
            data = data.replace("DATA:", "");
            return data;
        }
        Logger.getInstance().Warning(Tag, "Wrong size of entries: " + String.valueOf(entries.length));
        return "";
    }

    private String handleYoutubePlay(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleYoutubePlay with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.YOUTUBE_PLAY);
        }

        if (commandData.length() == 0) {
            _broadcastController.SendSimpleBroadcast(BroadcastVideoPlay);

        } else if (commandData.length() > 0 && commandData.length() < 4) {
            int youtubeIdInt;
            try {
                youtubeIdInt = Integer.parseInt(commandData);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
                Logger.getInstance().Warning(Tag, "Setting youtubeId to 0!");
                youtubeIdInt = 0;
            }

            YoutubeIdType youtubeId = YoutubeIdType.GetById(youtubeIdInt);
            if (youtubeId == null) {
                Logger.getInstance().Warning(Tag, "youtubeId is null! Setting to default");
                youtubeId = YoutubeIdType.THE_GOOD_LIFE_STREAM;
            }

            CenterViewDto youtubeCenterViewDto = new CenterViewDto(
                    false, "",
                    true, youtubeId,
                    false, "",
                    false, RadioStreamType.BAYERN_3,
                    false, "");
            _broadcastController.SendSerializableBroadcast(BroadcastShowCenterModel, BundleShowCenterModel, youtubeCenterViewDto);

        } else if (commandData.length() == 11) {
            YoutubeIdType youtubeId = YoutubeIdType.NULL;
            youtubeId.SetYoutubeId(commandData);

            CenterViewDto youtubeCenterViewDto = new CenterViewDto(
                    false, "",
                    true, youtubeId,
                    false, "",
                    false, RadioStreamType.BAYERN_3,
                    false, "");
            _broadcastController.SendSerializableBroadcast(BroadcastShowCenterModel, BundleShowCenterModel, youtubeCenterViewDto);

        } else {
            Logger.getInstance().Warning(Tag, "Wrong size for data of youtube id!");
            return errorResponse("Wrong size for data of youtube id", MediaServerActionType.YOUTUBE_PLAY);
        }

        return handleGetYoutubeData(commandData, MediaServerActionType.YOUTUBE_PLAY);
    }

    private String handleYoutubePause(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleYoutubePause with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.YOUTUBE_PAUSE);
        }
        _broadcastController.SendSimpleBroadcast(BroadcastVideoPause);
        return handleGetYoutubeData(commandData, MediaServerActionType.YOUTUBE_PAUSE);
    }

    private String handleYoutubeStop(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleYoutubeStop with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.YOUTUBE_STOP);
        }
        _broadcastController.SendSimpleBroadcast(BroadcastVideoStop);
        return handleGetYoutubeData(commandData, MediaServerActionType.YOUTUBE_STOP);
    }

    private String handleYoutubeSetPosition(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleYoutubeSetPosition with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.YOUTUBE_SET_POSITION);
        }

        int positionPercent = -1;
        try {
            positionPercent = Integer.parseInt(commandData);
        } catch (Exception ex) {
            Logger.getInstance().Error(Tag, ex.getMessage());
        }

        _broadcastController.SendIntBroadcast(BroadcastVideoPosition, BundleVideoPosition, positionPercent);

        return handleGetYoutubeData(commandData, MediaServerActionType.YOUTUBE_SET_POSITION);
    }

    private String handleCenterTextSet(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleCenterTextSet with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.CENTER_TEXT_SET);
        }

        CenterViewDto textCenterViewDto = new CenterViewDto(
                true, commandData,
                false, YoutubeIdType.NULL,
                false, "",
                false, RadioStreamType.BAYERN_3,
                false, "");
        _broadcastController.SendSerializableBroadcast(BroadcastShowCenterModel, BundleShowCenterModel, textCenterViewDto);

        return handleGetInformationData(commandData, MediaServerActionType.CENTER_TEXT_SET);
    }

    private String handleRadioStreamPlay(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleRadioStreamPlay with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.RADIO_STREAM_PLAY);
        }

        RadioStreamType radioStream = RadioStreamType.BAYERN_3;
        try {
            int radioStreamId = Integer.parseInt(commandData);
            radioStream = RadioStreamType.GetById(radioStreamId);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
        }

        CenterViewDto radioStreamCenterViewDto = new CenterViewDto(
                false, "",
                false, YoutubeIdType.NULL,
                false, "",
                true, radioStream,
                false, "");
        _broadcastController.SendSerializableBroadcast(BroadcastShowCenterModel, BundleShowCenterModel, radioStreamCenterViewDto);

        return handleGetRadioStreamData(commandData, MediaServerActionType.RADIO_STREAM_PLAY);
    }

    private String handleRadioStreamStop(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleRadioStreamStop with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.RADIO_STREAM_STOP);
        }
        _broadcastController.SendSimpleBroadcast(BroadcastRadioStreamStop);
        return handleGetRadioStreamData(commandData, MediaServerActionType.RADIO_STREAM_STOP);
    }

    private String handleMediaNotificationPlay(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleMediaNotificationPlay with CommandData %s", commandData));
        //TODO
        return handleGetNotificationData(commandData, MediaServerActionType.MEDIA_NOTIFICATION_PLAY);
    }

    private String handleMediaNotificationStop(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleMediaNotificationStop with CommandData %s", commandData));
        //TODO
        return handleGetNotificationData(commandData, MediaServerActionType.MEDIA_NOTIFICATION_STOP);
    }

    private String handleSleepSoundPlay(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleSleepSoundPlay with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.SLEEP_SOUND_PLAY);
        }

        int timeOut;
        try {
            timeOut = Integer.parseInt(commandData) * 60 * 1000;
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            Toasty.error(_context, exception.toString(), Toast.LENGTH_LONG).show();
            timeOut = DefaultSleepTimerTimeoutMs;
        }

        CenterViewDto sleepSoundPlayCenterViewDto = new CenterViewDto(
                false, "",
                true, YoutubeIdType.SEA_SOUND,
                false, "",
                false, RadioStreamType.BAYERN_3,
                false, "");
        _broadcastController.SendSerializableBroadcast(BroadcastShowCenterModel, BundleShowCenterModel, sleepSoundPlayCenterViewDto);

        _sleepTimerHandler.postDelayed(_sleepTimerRunnable, timeOut);
        _sleepTimerEnabled = true;
        _sleepTimerStartTime = System.currentTimeMillis();

        return handleGetSleepTimerData(commandData, MediaServerActionType.SLEEP_SOUND_PLAY);
    }

    private String handleSleepSoundStop(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleSleepSoundStop with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.SLEEP_SOUND_STOP);
        }

        CenterViewDto sleepSoundStopCenterViewDto = new CenterViewDto(
                true, "",
                false, YoutubeIdType.NULL,
                false, "",
                false, RadioStreamType.BAYERN_3,
                false, "");
        _broadcastController.SendSerializableBroadcast(BroadcastShowCenterModel, BundleShowCenterModel, sleepSoundStopCenterViewDto);

        _sleepTimerHandler.removeCallbacks(_sleepTimerRunnable);
        _sleepTimerEnabled = false;
        _sleepTimerStartTime = -1;

        return handleGetSleepTimerData(commandData, MediaServerActionType.SLEEP_SOUND_STOP);
    }

    private String handleRssFeedSet(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleRssFeedSet with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.RSS_FEED_SET);
        }

        int feedIdInt;
        try {
            feedIdInt = Integer.parseInt(commandData);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
            Logger.getInstance().Warning(Tag, "Setting feedIdInt to 0!");
            feedIdInt = 0;
        }

        RSSFeedType rssFeed = RSSFeedType.GetById(feedIdInt);
        if (rssFeed == null) {
            Logger.getInstance().Warning(Tag, "rssFeed is null! Setting to default");
            rssFeed = RSSFeedType.SPEKTRUM_DER_WISSENSCHAFT;
        }

        RssViewDto rssViewDto = new RssViewDto(rssFeed, true);
        _broadcastController.SendSerializableBroadcast(BroadcastRssFeedUpdate, BundleRssFeedUpdate, rssViewDto);

        return handleGetInformationData(commandData, MediaServerActionType.RSS_FEED_SET);
    }

    private String handleRssFeedReset(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleRssFeedReset with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.RSS_FEED_RESET);
        }
        _broadcastController.SendSimpleBroadcast(BroadcastRssFeedReset);
        return handleGetInformationData(commandData, MediaServerActionType.RSS_FEED_RESET);
    }

    private String handleUpdateBirthdayAlarm(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleUpdateBirthdayAlarm with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.UPDATE_BIRTHDAY_ALARM);
        }
        BirthdayService.getInstance().LoadData();
        return successResponse(MediaServerActionType.UPDATE_BIRTHDAY_ALARM, "-");
    }

    private String handleUpdateCalendarAlarm(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleUpdateCalendarAlarm with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.UPDATE_CALENDAR_ALARM);
        }
        try {
            CalendarService.getInstance().LoadData();
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }
        return successResponse(MediaServerActionType.UPDATE_CALENDAR_ALARM, "-");
    }

    private String handleUpdateCurrentWeather(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleUpdateCurrentWeather with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.UPDATE_CURRENT_WEATHER);
        }
        OpenWeatherService.getInstance().LoadCurrentWeather();
        return successResponse(MediaServerActionType.UPDATE_CURRENT_WEATHER, "-");
    }

    private String handleUpdateForecastWeather(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleUpdateForecastWeather with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.UPDATE_FORECAST_WEATHER);
        }
        OpenWeatherService.getInstance().LoadForecastWeather();
        return successResponse(MediaServerActionType.UPDATE_FORECAST_WEATHER, "-");
    }

    private String handleUpdateIpAddress(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleUpdateIpAddress with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.UPDATE_IP_ADDRESS);
        }
        _broadcastController.SendSimpleBroadcast(BroadcastIpAddressUpdate);
        return successResponse(MediaServerActionType.UPDATE_IP_ADDRESS, "-");
    }

    private String handleUpdateRaspberryTemperature(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleUpdateRaspberryTemperature with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.UPDATE_RASPBERRY_TEMPERATURE);
        }
        TemperatureService.getInstance().LoadData();
        return successResponse(MediaServerActionType.UPDATE_RASPBERRY_TEMPERATURE, "-");
    }

    private String handleVolumeIncrease(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleVolumeIncrease with CommandData %s", commandData));
        _mediaVolumeController.IncreaseVolume();
        return handleGetInformationData(commandData, MediaServerActionType.VOLUME_INCREASE);
    }

    private String handleVolumeDecrease(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleVolumeDecrease with CommandData %s", commandData));
        _mediaVolumeController.DecreaseVolume();
        return handleGetInformationData(commandData, MediaServerActionType.VOLUME_DECREASE);
    }

    private String handleVolumeMute(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleVolumeMute with CommandData %s", commandData));
        _mediaVolumeController.MuteVolume();
        return handleGetInformationData(commandData, MediaServerActionType.VOLUME_MUTE);
    }

    private String handleVolumeUnMute(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleVolumeUnMute with CommandData %s", commandData));
        _mediaVolumeController.UnMuteVolume();
        return handleGetInformationData(commandData, MediaServerActionType.VOLUME_UN_MUTE);
    }

    private String handleScreenBrightnessIncrease(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleScreenBrightnessIncrease with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.SCREEN_BRIGHTNESS_INCREASE);
        }
        _displayController.SetBrightness(_displayController.GetCurrentBrightness() + 1);
        return handleGetInformationData(commandData, MediaServerActionType.SCREEN_BRIGHTNESS_INCREASE);
    }

    private String handleScreenBrightnessDecrease(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleScreenBrightnessDecrease with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is not enabled!");
            return errorResponse("Screen is not enabled", MediaServerActionType.SCREEN_BRIGHTNESS_DECREASE);
        }
        _displayController.SetBrightness(_displayController.GetCurrentBrightness() - 1);
        return handleGetInformationData(commandData, MediaServerActionType.SCREEN_BRIGHTNESS_DECREASE);

    }

    private String handleScreenNormal(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleScreenNormal with CommandData %s", commandData));
        _broadcastController.SendSimpleBroadcast(BroadcastScreenNormal);
        return handleGetInformationData(commandData, MediaServerActionType.SCREEN_NORMAL);
    }

    private String handleScreenOn(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleScreenOn with CommandData %s", commandData));
        if (_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is already on!");
            return errorResponse("Screen is already on", MediaServerActionType.SCREEN_ON);
        }
        _broadcastController.SendSimpleBroadcast(BroadcastScreenOn);
        return handleGetInformationData(commandData, MediaServerActionType.SCREEN_ON);
    }

    private String handleScreenOff(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleScreenOff with CommandData %s", commandData));
        if (!_displayController.IsScreenOn()) {
            Logger.getInstance().Error(Tag, "Screen is already off!");
            return errorResponse("Screen is already off", MediaServerActionType.SCREEN_OFF);
        }
        _broadcastController.SendSimpleBroadcast(BroadcastScreenOff);
        return handleGetInformationData(commandData, MediaServerActionType.SCREEN_OFF);
    }

    private String handleGetInformationData(@NonNull String commandData, MediaServerActionType mediaServerAction) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleGetMediaServerInformation with CommandData %s", commandData));

        String serverVersion = "";
        String centerText = _iCenterViewController.GetCenterText();
        RSSFeedType rssFeed = _iRssViewController.GetCurrentRssFeed();
        int currentVolume = _mediaVolumeController.GetVolume();
        int screenBrightness = _displayController.GetCurrentBrightness();

        try {
            PackageInfo packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
            serverVersion = packageInfo.versionName;
        } catch (Exception exception) {
            Logger.getInstance().Warning(Tag, exception.toString());
        }

        InformationData informationData = new InformationData(
                serverVersion,
                centerText,
                rssFeed,
                currentVolume,
                _batteryLevel,
                screenBrightness
        );
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerActionType.GET_INFORMATION_DATA, informationData.GetCommunicationString());
    }

    private String handleGetNotificationData(@NonNull String commandData, MediaServerActionType mediaServerAction) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleGetNotificationData with CommandData %s", commandData));
        //TODO
        return errorResponse("Not implemented", mediaServerAction != null ? mediaServerAction : MediaServerActionType.GET_NOTIFICATION_DATA);
    }

    private String handleGetRadioStreamData(@NonNull String commandData, MediaServerActionType mediaServerAction) throws NullPointerException {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleGetRadioStreamData with CommandData %s", commandData));
        RadioStreamData radioStreamData = new RadioStreamData(
                _iCenterViewController.GetRadioStream(),
                _iCenterViewController.IsRadioStreamPlaying());
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerActionType.GET_RADIO_STREAM_DATA, radioStreamData.GetCommunicationString());
    }

    private String handleGetSleepTimerData(@NonNull String commandData, MediaServerActionType mediaServerAction) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleGetSleepTimerData with CommandData %s", commandData));

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
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerActionType.GET_SLEEP_TIMER_DATA, sleepTimerData.GetCommunicationString());
    }

    private String handleGetYoutubeData(@NonNull String commandData, MediaServerActionType mediaServerAction) throws NullPointerException {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleGetYoutubeData with CommandData %s", commandData));
        YoutubeData youtubeData = new YoutubeData(
                _iCenterViewController.IsYoutubePlaying(),
                _iCenterViewController.GetYoutubeId().GetYoutubeId(),
                _iCenterViewController.GetCurrentPlayPosition(),
                _iCenterViewController.GetYoutubeDuration(),
                _iCenterViewController.GetYoutubeIds());
        return successResponse(mediaServerAction != null ? mediaServerAction : MediaServerActionType.GET_YOUTUBE_DATA, youtubeData.GetCommunicationString());
    }

    private String handleSystemReboot(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleSystemReboot with CommandData %s", commandData));
        if (_commandController.RebootDevice(CommandTimeoutMs)) {
            return successResponse(MediaServerActionType.SYSTEM_REBOOT, "-");
        } else {
            return errorResponse("Cannot reboot!", MediaServerActionType.SYSTEM_REBOOT);
        }
    }

    private String handleSystemShutdown(@NonNull String commandData) {
        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "handleSystemShutdown with CommandData %s", commandData));
        if (_commandController.ShutDownDevice(CommandTimeoutMs)) {
            return successResponse(MediaServerActionType.SYSTEM_SHUTDOWN, "-");
        } else {
            return errorResponse("Cannot shutdown!", MediaServerActionType.SYSTEM_SHUTDOWN);
        }
    }

    private String successResponse(@NonNull MediaServerActionType mediaServerAction, @NonNull String data) {
        return String.format(Locale.getDefault(), "OK%sCommand performed%s%s%s%s",
                IMediaServerClientService.ResponseSplitChar,
                IMediaServerClientService.ResponseSplitChar, mediaServerAction.toString(),
                IMediaServerClientService.ResponseSplitChar, data);
    }

    private String errorResponse(@NonNull String errorMessage, @NonNull MediaServerActionType mediaServerAction) {
        return String.format(Locale.getDefault(), "Error%s%s%s%s%s%s",
                IMediaServerClientService.ResponseSplitChar, errorMessage,
                IMediaServerClientService.ResponseSplitChar, mediaServerAction.toString(),
                IMediaServerClientService.ResponseSplitChar, "-");
    }
}
