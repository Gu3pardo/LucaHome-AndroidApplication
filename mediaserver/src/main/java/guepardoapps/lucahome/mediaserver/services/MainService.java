package guepardoapps.lucahome.mediaserver.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.common.constants.Keys;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.MediaVolumeController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.controller.TTSController;
import guepardoapps.lucahome.common.controller.mediaserver.WirelessSocketBatteryController;
import guepardoapps.lucahome.common.datatransferobjects.mediaserver.CenterViewDto;
import guepardoapps.lucahome.common.datatransferobjects.mediaserver.RssViewDto;
import guepardoapps.lucahome.common.enums.RSSFeedType;
import guepardoapps.lucahome.common.enums.RadioStreamType;
import guepardoapps.lucahome.common.enums.YoutubeIdType;
import guepardoapps.lucahome.common.rss.RssUpdateService;
import guepardoapps.lucahome.common.server.IServerThread;
import guepardoapps.lucahome.common.server.ServerThread;
import guepardoapps.lucahome.common.server.handler.IDataHandler;
import guepardoapps.lucahome.common.server.handler.MediaServerDataHandler;
import guepardoapps.lucahome.common.services.BirthdayService;
import guepardoapps.lucahome.common.services.CalendarService;
import guepardoapps.lucahome.common.services.MealService;
import guepardoapps.lucahome.common.services.RoomService;
import guepardoapps.lucahome.common.services.ShoppingItemService;
import guepardoapps.lucahome.common.services.TemperatureService;
import guepardoapps.lucahome.common.services.WirelessSocketService;
import guepardoapps.lucahome.common.services.WirelessSwitchService;
import guepardoapps.lucahome.common.services.MediaServerClientService;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.mediaserver.FullscreenActivity;
import guepardoapps.lucahome.mediaserver.common.Constants;

public class MainService extends Service {
    private static final String Tag = MainService.class.getSimpleName();

    private boolean _isInitialized;

    private Context _context;

    private BroadcastController _broadcastController;
    private MediaVolumeController _mediaVolumeController;
    private ReceiverController _receiverController;
    private TTSController _ttsController;
    private WirelessSocketBatteryController _wirelessSocketBatteryController;

    private OpenWeatherService _openWeatherService;

    private BirthdayService _birthdayService;
    private CalendarService _calendarService;
    private MealService _mealService;
    private MediaServerClientService _mediaServerClientService;
    private RoomService _roomService;
    private RssUpdateService _rssUpdateService;
    private ShoppingItemService _shoppingItemService;
    private TemperatureService _temperatureService;
    private WirelessSocketService _wirelessSocketService;
    private WirelessSwitchService _wirelessSwitchService;

    private IDataHandler _mediaServerDataHandler;
    private IServerThread _serverThread;

    private PowerManager _powerManager;
    private PowerManager.WakeLock _wakeLock;

    private WifiManager _wifiManager;
    private WifiLock _wifiLock;

    private final BroadcastReceiver _timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action == null) {
                Logger.getInstance().Error(Tag, "Action is null!");
                return;
            }

            if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                // TODO implement method to perform automatic settings -> dynamic adding => remote and database
            } else if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                try {
                    _birthdayService.LoadData();
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.toString());
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (!_isInitialized) {
            _isInitialized = true;

            _context = this;

            SettingsController.getInstance().Initialize(_context);

            _broadcastController = new BroadcastController(_context);
            _mediaVolumeController = MediaVolumeController.getInstance();
            _receiverController = new ReceiverController(_context);
            _ttsController = new TTSController();
            _wirelessSocketBatteryController = new WirelessSocketBatteryController(_context);

            _openWeatherService = OpenWeatherService.getInstance();

            _birthdayService = BirthdayService.getInstance();
            _calendarService = CalendarService.getInstance();
            _mealService = MealService.getInstance();
            _mediaServerClientService = MediaServerClientService.getInstance();
            _roomService = RoomService.getInstance();
            _rssUpdateService = RssUpdateService.getInstance();
            _shoppingItemService = ShoppingItemService.getInstance();
            _temperatureService = TemperatureService.getInstance();
            _wirelessSocketService = WirelessSocketService.getInstance();
            _wirelessSwitchService = WirelessSwitchService.getInstance();

            _openWeatherService.Initialize(_context, Constants.City, Keys.OpenWeatherApiKey);

            _birthdayService.Initialize(_context, SettingsController.getInstance().IsReloadBirthdayEnabled(), SettingsController.getInstance().GetReloadBirthdayTimeout(), false, null);
            _calendarService.Initialize(_context, true, 30, false, null);
            _mealService.Initialize(_context, SettingsController.getInstance().IsReloadMealEnabled(), SettingsController.getInstance().GetReloadMealTimeout(), false, null);
            _mediaServerClientService.Initialize(_context, true, 3 * 60, false, null);
            _roomService.Initialize(_context, SettingsController.getInstance().IsReloadRoomEnabled(), SettingsController.getInstance().GetReloadRoomTimeout(), false, null);
            _rssUpdateService.Initialize(_context, true, 5 * 60, false, null);
            _shoppingItemService.Initialize(_context, SettingsController.getInstance().IsReloadShoppingEnabled(), SettingsController.getInstance().GetReloadShoppingTimeout(), false, null);
            _temperatureService.Initialize(_context, SettingsController.getInstance().IsReloadTemperatureEnabled(), SettingsController.getInstance().GetReloadTemperatureTimeout(), false, null);
            _wirelessSocketService.Initialize(_context, SettingsController.getInstance().IsReloadWirelessSocketEnabled(), SettingsController.getInstance().GetReloadWirelessSocketTimeout(), false, null);
            _wirelessSwitchService.Initialize(_context, SettingsController.getInstance().IsReloadWirelessSwitchEnabled(), SettingsController.getInstance().GetReloadWirelessSwitchTimeout(), false, null);

            _receiverController.RegisterReceiver(_timeChangedReceiver, new String[]{Intent.ACTION_TIME_TICK, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED});

            _mediaVolumeController.Initialize(_context);
            _ttsController.Initialize(_context, true);

            _mediaServerDataHandler = new MediaServerDataHandler();
            // TODO use correct initialize
            _mediaServerDataHandler.Initialize(_context);

            _serverThread = new ServerThread();
            _serverThread.Start(Constants.ServerPort, _mediaServerDataHandler);

            CenterViewDto centerViewDto = new CenterViewDto(
                    false, "",
                    true, YoutubeIdType.THE_GOOD_LIFE_STREAM,
                    false, "",
                    false, RadioStreamType.BAYERN_3,
                    false, "");
            _broadcastController.SendSerializableBroadcast(MediaServerDataHandler.BroadcastShowCenterModel, MediaServerDataHandler.BundleShowCenterModel, centerViewDto);

            RssViewDto rssViewDto = new RssViewDto(RSSFeedType.SPEKTRUM_DER_WISSENSCHAFT, true);
            _broadcastController.SendSerializableBroadcast(MediaServerDataHandler.BroadcastRssFeedUpdate, MediaServerDataHandler.BundleRssFeedUpdate, rssViewDto);

            setLocks();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (_isInitialized) {
            if (_broadcastController != null) {
                CenterViewDto centerViewDto = new CenterViewDto(
                        false, "",
                        true, YoutubeIdType.THE_GOOD_LIFE_STREAM,
                        false, "",
                        false, RadioStreamType.BAYERN_3,
                        false, "");
                _broadcastController.SendSerializableBroadcast(MediaServerDataHandler.BroadcastShowCenterModel, MediaServerDataHandler.BundleShowCenterModel, centerViewDto);

                RssViewDto rssViewDto = new RssViewDto(RSSFeedType.SPEKTRUM_DER_WISSENSCHAFT, true);
                _broadcastController.SendSerializableBroadcast(MediaServerDataHandler.BroadcastRssFeedUpdate, MediaServerDataHandler.BundleRssFeedUpdate, rssViewDto);
            }
            setLocks();
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        _mediaVolumeController.Dispose();
        _receiverController.Dispose();
        _ttsController.Dispose();
        _wirelessSocketBatteryController.Dispose();

        _openWeatherService.Dispose();

        _birthdayService.Dispose();
        _calendarService.Dispose();
        _mealService.Dispose();
        _mediaServerClientService.Dispose();
        _roomService.Dispose();
        _rssUpdateService.Dispose();
        _shoppingItemService.Dispose();
        _temperatureService.Dispose();
        _wirelessSocketService.Dispose();
        _wirelessSwitchService.Dispose();

        _serverThread.Dispose();
        _mediaServerDataHandler.Dispose();

        _wakeLock.release();
        _wifiLock.release();

        restartActivity();
    }

    private void restartActivity() {
        Toasty.info(_context, "Restarting activity Main.class!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(_context, FullscreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setLocks() {
        if (_powerManager == null) {
            _powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }

        if (_powerManager != null) {
            _wakeLock = _powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MediaServerMainService_WakeLock");
            _wakeLock.acquire();
        } else {
            Logger.getInstance().Error(Tag, "_powerManager is null");
        }


        if (_wifiManager == null) {
            _wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        }

        if (_wifiManager != null) {
            _wifiLock = _wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MediaServerMainService_WifiLock");
            _wifiLock.acquire();
        } else {
            Logger.getInstance().Error(Tag, "_wifiManager is null");
        }
    }
}