package guepardoapps.lucahome.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.*;
import guepardoapps.lucahome.receiver.SocketActionReceiver;
import guepardoapps.lucahome.views.BirthdayActivity;
import guepardoapps.lucahome.views.CoinActivity;
import guepardoapps.lucahome.views.ForecastWeatherActivity;
import guepardoapps.lucahome.views.MainActivity;
import guepardoapps.lucahome.views.SecurityActivity;

public class MainService extends Service {
    public class MainServiceBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    public static class MainServiceDownloadCountContent implements Serializable {
        public double DownloadProgress;
        public boolean DownloadFinished;

        MainServiceDownloadCountContent(double downloadProgress, boolean downloadFinished) {
            DownloadProgress = downloadProgress;
            DownloadFinished = downloadFinished;
        }
    }

    public static final String MainServiceStartDownloadAllBroadcast = "guepardoapps.lucahome.main.service.download.all.start";
    public static final String MainServiceDownloadCountBroadcast = "guepardoapps.lucahome.main.service.download.count";

    public static final String MainServiceDownloadCountBundle = "MainServiceDownloadCountBundle";
    public static final String MainServiceOnStartCommandBundle = "MainServiceOnStartCommandBundle";

    private static final String TAG = MainService.class.getSimpleName();

    private static final SparseArray<Runnable> DOWNLOAD_HASH_MAP = new SparseArray<>();

    static {
        DOWNLOAD_HASH_MAP.append(0, () -> BirthdayService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(1, () -> CoinService.getInstance().LoadCoinConversionList());
        DOWNLOAD_HASH_MAP.append(2, () -> CoinService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(3, () -> MenuService.getInstance().LoadListedMenuList());
        DOWNLOAD_HASH_MAP.append(4, () -> MenuService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(5, () -> MeterListService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(6, () -> MoneyMeterListService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(7, () -> MovieService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(8, () -> OpenWeatherService.getInstance().LoadCurrentWeather());
        DOWNLOAD_HASH_MAP.append(9, () -> OpenWeatherService.getInstance().LoadForecastWeather());
        DOWNLOAD_HASH_MAP.append(10, () -> TemperatureService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(11, () -> SecurityService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(12, () -> ShoppingListService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(13, () -> WirelessSocketService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(14, () -> WirelessSwitchService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(15, () -> ScheduleService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(16, () -> MapContentService.getInstance().LoadData());
    }

    private final IBinder _mainServiceBinder = new MainServiceBinder();

    private BirthdayService _birthdayService;
    private CoinService _coinService;
    private MapContentService _mapContentService;
    private MediaServerService _mediaServerService;
    private MenuService _menuService;
    private MeterListService _meterListService;
    private MoneyMeterListService _moneyMeterListService;
    private MovieService _movieService;
    private OpenWeatherService _openWeatherService;
    private ScheduleService _scheduleService;
    private SecurityService _securityService;
    private ShoppingListService _shoppingListService;
    private TemperatureService _temperatureService;
    private UserService _userService;
    private WirelessSocketService _wirelessSocketService;
    private WirelessSwitchService _wirelessSwitchService;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private int _currentDownloadCount;
    private boolean _downloading;

    private BroadcastReceiver _startDownloadAllReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StartDownloadAll("_birthdayDownloadFinishedReceiver onReceive");
        }
    };

    private BroadcastReceiver _birthdayDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _coinConversionDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _coinDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _mapContentDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _listedMenuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _menuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _meterListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _moneyMeterListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _movieDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _openWeatherCurrentWeatherDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _openWeatherForecastWeatherDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _scheduleDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _securityDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _shoppingListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _temperatureDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _wirelessSocketDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _wirelessSwitchDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
        }
    };

    public void StartDownloadAll(@NonNull String caller) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "StartDownloadAll by caller %s", caller));
        if (_downloading) {
            return;
        }
        _currentDownloadCount = 0;
        _downloading = true;
        DOWNLOAD_HASH_MAP.get(_currentDownloadCount).run();
    }

    public void Cancel() {
        stopSelf();
    }

    private void broadcastDownloadCount() {
        _currentDownloadCount++;
        double downloadPercentage = (_currentDownloadCount * 100) / DOWNLOAD_HASH_MAP.size();
        _downloading = !(downloadPercentage >= 100);
        _broadcastController.SendSerializableBroadcast(
                MainServiceDownloadCountBroadcast,
                MainServiceDownloadCountBundle,
                new MainServiceDownloadCountContent(downloadPercentage, downloadPercentage >= 100)
        );
        if (_currentDownloadCount < DOWNLOAD_HASH_MAP.size()) {
            DOWNLOAD_HASH_MAP.get(_currentDownloadCount).run();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try {
            boolean downloadAll = intent.getBooleanExtra(MainServiceOnStartCommandBundle, false);
            if (downloadAll) {
                StartDownloadAll("onStartCommand");
            }
        } catch (Exception exception) {
            Logger.getInstance().Error(TAG, exception.getMessage());
            Toasty.error(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*Bundle extras = intent.getExtras();
        if (extras != null) {
            // _messenger = (Messenger) extras.get("MESSENGER");
            // TODO do something with extras
        }*/
        return _mainServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _broadcastController = new BroadcastController(this);
        _receiverController = new ReceiverController(this);
        SettingsController.getInstance().Initialize(this);

        _receiverController.RegisterReceiver(_startDownloadAllReceiver, new String[]{MainServiceStartDownloadAllBroadcast});
        _receiverController.RegisterReceiver(_birthdayDownloadFinishedReceiver, new String[]{BirthdayService.BirthdayDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinConversionDownloadFinishedReceiver, new String[]{CoinService.CoinConversionDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDownloadFinishedReceiver, new String[]{CoinService.CoinDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_mapContentDownloadFinishedReceiver, new String[]{MapContentService.MapContentDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_listedMenuDownloadFinishedReceiver, new String[]{MenuService.ListedMenuDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuDownloadFinishedReceiver, new String[]{MenuService.MenuDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterListDownloadFinishedReceiver, new String[]{MeterListService.MeterDataListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyMeterListDownloadFinishedReceiver, new String[]{MoneyMeterListService.MoneyMeterDataListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_movieDownloadFinishedReceiver, new String[]{MovieService.MovieDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherCurrentWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.CurrentWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherForecastWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.ForecastWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleDownloadFinishedReceiver, new String[]{ScheduleService.ScheduleDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_securityDownloadFinishedReceiver, new String[]{SecurityService.SecurityDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingListDownloadFinishedReceiver, new String[]{ShoppingListService.ShoppingListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_temperatureDownloadFinishedReceiver, new String[]{TemperatureService.TemperatureDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketDownloadFinishedReceiver, new String[]{WirelessSocketService.WirelessSocketDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchDownloadFinishedReceiver, new String[]{WirelessSwitchService.WirelessSwitchDownloadFinishedBroadcast});

        _birthdayService = BirthdayService.getInstance();
        _coinService = CoinService.getInstance();
        _mapContentService = MapContentService.getInstance();
        _mediaServerService = MediaServerService.getInstance();
        _menuService = MenuService.getInstance();
        _meterListService = MeterListService.getInstance();
        _moneyMeterListService = MoneyMeterListService.getInstance();
        _movieService = MovieService.getInstance();
        _openWeatherService = OpenWeatherService.getInstance();
        _scheduleService = ScheduleService.getInstance();
        _securityService = SecurityService.getInstance();
        _shoppingListService = ShoppingListService.getInstance();
        _temperatureService = TemperatureService.getInstance();
        _userService = UserService.getInstance();
        _wirelessSocketService = WirelessSocketService.getInstance();
        _wirelessSwitchService = WirelessSwitchService.getInstance();

        _birthdayService.Initialize(
                this,
                BirthdayActivity.class,
                SettingsController.getInstance().IsBirthdayNotificationEnabled(),
                SettingsController.getInstance().IsReloadBirthdayEnabled(),
                SettingsController.getInstance().GetReloadBirthdayTimeout());

        _coinService.Initialize(
                this,
                CoinActivity.class,
                SettingsController.getInstance().IsCoinsNotificationEnabled(),
                SettingsController.getInstance().IsReloadCoinEnabled(),
                SettingsController.getInstance().GetReloadCoinTimeout());

        _mapContentService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMapContentEnabled(),
                SettingsController.getInstance().GetReloadMapContentTimeout());

        _mediaServerService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMediaServerEnabled(),
                SettingsController.getInstance().GetReloadMediaServerTimeout());

        _menuService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMenuEnabled(),
                SettingsController.getInstance().GetReloadMenuTimeout());

        _meterListService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMeterDataEnabled(),
                SettingsController.getInstance().GetReloadMeterDataTimeout()
        );

        _moneyMeterListService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMoneyMeterDataEnabled(),
                SettingsController.getInstance().GetReloadMoneyMeterDataTimeout()
        );

        _movieService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMovieEnabled(),
                SettingsController.getInstance().GetReloadMovieTimeout());

        _openWeatherService.Initialize(
                this,
                SettingsController.getInstance().GetOpenWeatherCity(),
                SettingsController.getInstance().IsCurrentWeatherNotificationEnabled(),
                SettingsController.getInstance().IsForecastWeatherNotificationEnabled(),
                MainActivity.class,
                ForecastWeatherActivity.class,
                SettingsController.getInstance().IsChangeWeatherWallpaperActive(),
                SettingsController.getInstance().IsReloadWeatherEnabled(),
                SettingsController.getInstance().GetReloadWeatherTimeout());

        _scheduleService.Initialize(
                this,
                SettingsController.getInstance().IsReloadScheduleEnabled(),
                SettingsController.getInstance().GetReloadScheduleTimeout());

        _securityService.Initialize(
                this,
                SecurityActivity.class,
                SettingsController.getInstance().IsCameraNotificationEnabled(),
                SettingsController.getInstance().IsReloadSecurityEnabled(),
                SettingsController.getInstance().GetReloadSecurityTimeout());

        _shoppingListService.Initialize(
                this,
                SettingsController.getInstance().IsReloadShoppingEnabled(),
                SettingsController.getInstance().GetReloadShoppingTimeout());

        _temperatureService.Initialize(
                this,
                MainActivity.class,
                SettingsController.getInstance().IsTemperatureNotificationEnabled(),
                SettingsController.getInstance().IsReloadTemperatureEnabled(),
                SettingsController.getInstance().GetReloadTemperatureTimeout());

        _userService.Initialize(this);

        _wirelessSocketService.Initialize(
                this,
                SocketActionReceiver.class,
                SettingsController.getInstance().IsSocketNotificationEnabled(),
                SettingsController.getInstance().IsReloadWirelessSocketEnabled(),
                SettingsController.getInstance().GetReloadWirelessSocketTimeout());

        _wirelessSwitchService.Initialize(
                this,
                /* TODO add ReceiverClass! */
                null,
                SettingsController.getInstance().IsSwitchNotificationEnabled(),
                SettingsController.getInstance().IsReloadWirelessSwitchEnabled(),
                SettingsController.getInstance().GetReloadWirelessSwitchTimeout()
        );

        _currentDownloadCount = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();

        _birthdayService.Dispose();
        _coinService.Dispose();
        _mapContentService.Dispose();
        _mediaServerService.Dispose();
        _menuService.Dispose();
        _meterListService.Dispose();
        _moneyMeterListService.Dispose();
        _movieService.Dispose();
        _openWeatherService.Dispose();
        _scheduleService.Dispose();
        _securityService.Dispose();
        _shoppingListService.Dispose();
        _temperatureService.Dispose();
        _userService.Dispose();
        _wirelessSocketService.Dispose();
        _wirelessSwitchService.Dispose();
    }
}
