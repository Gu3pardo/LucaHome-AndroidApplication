package guepardoapps.lucahome.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.*;
import guepardoapps.lucahome.receiver.SocketActionReceiver;
import guepardoapps.lucahome.views.BirthdayActivity;
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
        SerializableList<SerializablePair> DownloadResultList;
        public double DownloadProgress;
        public boolean DownloadFinished;

        MainServiceDownloadCountContent(SerializableList<SerializablePair> downloadResultList, double downloadProgress, boolean downloadFinished) {
            DownloadResultList = downloadResultList;
            DownloadProgress = downloadProgress;
            DownloadFinished = downloadFinished;
        }
    }

    public static final String MainServiceStartDownloadAllBroadcast = "guepardoapps.lucahome.main.service.download.all.start";
    public static final String MainServiceDownloadCountBroadcast = "guepardoapps.lucahome.main.service.download.count";

    public static final String MainServiceDownloadCountBundle = "MainServiceDownloadCountBundle";
    public static final String MainServiceOnStartCommandBundle = "MainServiceOnStartCommandBundle";

    private static final String TAG = MainService.class.getSimpleName();
    private Logger _logger;

    private final IBinder _mainServiceBinder = new MainServiceBinder();

    private BirthdayService _birthdayService;
    private CoinService _coinService;
    private MapContentService _mapContentService;
    private MediaMirrorService _mediaMirrorService;
    private MenuService _menuService;
    private MovieService _movieService;
    private OpenWeatherService _openWeatherService;
    private ScheduleService _scheduleService;
    private SecurityService _securityService;
    private ShoppingListService _shoppingListService;
    private TemperatureService _temperatureService;
    private UserService _userService;
    private WirelessSocketService _wirelessSocketService;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private int _currentDownloadCount;
    private SerializableList<SerializablePair> _downloadResultList;
    private boolean _downloading;

    private BroadcastReceiver _startDownloadAllReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayDownloadFinishedReceiver");
            StartDownloadAll("_birthdayDownloadFinishedReceiver onReceive");
        }
    };

    private BroadcastReceiver _birthdayDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_birthdayDownloadFinishedReceiver");
            BirthdayService.BirthdayDownloadFinishedContent result = (BirthdayService.BirthdayDownloadFinishedContent) intent.getSerializableExtra(BirthdayService.BirthdayDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Birthday", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Birthday", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _coinConversionDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinConversionDownloadFinishedReceiver");
            CoinService.CoinConversionDownloadFinishedContent result = (CoinService.CoinConversionDownloadFinishedContent) intent.getSerializableExtra(CoinService.CoinConversionDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("CoinConversion", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("CoinConversion", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _coinDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_coinDownloadFinishedReceiver");
            CoinService.CoinDownloadFinishedContent result = (CoinService.CoinDownloadFinishedContent) intent.getSerializableExtra(CoinService.CoinDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Coin", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Coin", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _mapContentDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_mapContentDownloadFinishedReceiver");
            MapContentService.MapContentDownloadFinishedContent result = (MapContentService.MapContentDownloadFinishedContent) intent.getSerializableExtra(MapContentService.MapContentDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("MapContent", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("MapContent", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _listedMenuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_listedMenuDownloadFinishedReceiver");
            MenuService.ListedMenuDownloadFinishedContent result = (MenuService.ListedMenuDownloadFinishedContent) intent.getSerializableExtra(MenuService.ListedMenuDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("ListedMenu", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("ListedMenu", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _menuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_menuDownloadFinishedReceiver");
            MenuService.MenuDownloadFinishedContent result = (MenuService.MenuDownloadFinishedContent) intent.getSerializableExtra(MenuService.MenuDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Menu", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Menu", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _movieDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_movieDownloadFinishedReceiver");
            MovieService.MovieDownloadFinishedContent result = (MovieService.MovieDownloadFinishedContent) intent.getSerializableExtra(MovieService.MovieDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Movie", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Movie", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _openWeatherCurrentWeatherDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_openWeatherCurrentWeatherDownloadFinishedReceiver");
            OpenWeatherService.CurrentWeatherDownloadFinishedContent result = (OpenWeatherService.CurrentWeatherDownloadFinishedContent) intent.getSerializableExtra(OpenWeatherService.CurrentWeatherDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("CurrentWeather", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("CurrentWeather", false));
            }
            broadcastDownloadCount();

            _temperatureService.LoadData();
        }
    };

    private BroadcastReceiver _openWeatherForecastWeatherDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_openWeatherForecastWeatherDownloadFinishedReceiver");
            OpenWeatherService.ForecastWeatherDownloadFinishedContent result = (OpenWeatherService.ForecastWeatherDownloadFinishedContent) intent.getSerializableExtra(OpenWeatherService.ForecastWeatherDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("ForecastWeather", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("ForecastWeather", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _scheduleDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_scheduleDownloadFinishedReceiver");
            ScheduleService.ScheduleDownloadFinishedContent result = (ScheduleService.ScheduleDownloadFinishedContent) intent.getSerializableExtra(ScheduleService.ScheduleDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Schedule", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Schedule", false));
            }
            broadcastDownloadCount();

            if (_temperatureService.GetDataList() != null) {
                _mapContentService.LoadData();
            }
        }
    };

    private BroadcastReceiver _securityDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_securityDownloadFinishedReceiver");
            SecurityService.SecurityDownloadFinishedContent result = (SecurityService.SecurityDownloadFinishedContent) intent.getSerializableExtra(SecurityService.SecurityDownloadFinishedBroadcast);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Security", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Security", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _shoppingListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_shoppingListDownloadFinishedReceiver");
            ShoppingListService.ShoppingListDownloadFinishedContent result = (ShoppingListService.ShoppingListDownloadFinishedContent) intent.getSerializableExtra(ShoppingListService.ShoppingListDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("ShoppingList", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("ShoppingList", false));
            }
            broadcastDownloadCount();
        }
    };

    private BroadcastReceiver _temperatureDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_temperatureDownloadFinishedReceiver");
            TemperatureService.TemperatureDownloadFinishedContent result = (TemperatureService.TemperatureDownloadFinishedContent) intent.getSerializableExtra(TemperatureService.TemperatureDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Temperature", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Temperature", false));
            }
            broadcastDownloadCount();

            if (_scheduleService.GetDataList() != null) {
                _mapContentService.LoadData();
            }
        }
    };

    private BroadcastReceiver _wirelessSocketDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wirelessSocketDownloadFinishedReceiver");
            WirelessSocketService.WirelessSocketDownloadFinishedContent result = (WirelessSocketService.WirelessSocketDownloadFinishedContent) intent.getSerializableExtra(WirelessSocketService.WirelessSocketDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("WirelessSocket", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("WirelessSocket", false));
            }
            broadcastDownloadCount();

            _scheduleService.LoadData();
        }
    };

    public void StartDownloadAll(@NonNull String caller) {
        _logger.Debug("StartDownloadAll with " + caller);

        if (_downloading) {
            _logger.Debug("Already downloading!");
            return;
        }

        _currentDownloadCount = 0;
        _downloadResultList = new SerializableList<>();
        _downloading = true;

        _birthdayService.LoadData();
        _coinService.LoadCoinConversionList();
        _menuService.LoadListedMenuList();
        _menuService.LoadData();
        _movieService.LoadData();
        _openWeatherService.LoadCurrentWeather();
        _openWeatherService.LoadForecastWeather();
        _securityService.LoadData();
        _shoppingListService.LoadData();
        _wirelessSocketService.LoadData();
    }

    public void Cancel() {
        stopSelf();
    }

    private void broadcastDownloadCount() {
        _logger.Debug("broadcastDownloadCount");

        _currentDownloadCount++;
        double downloadPercentage = (_currentDownloadCount * 100) / Constants.DOWNLOAD_STEPS;
        _downloading = !(downloadPercentage >= 100);

        _logger.Debug(String.format(Locale.getDefault(), "broadcastDownloadCount with _currentDownloadCount %d and downloadPercentage %.2f and _downloading: %s", _currentDownloadCount, downloadPercentage, _downloading));

        _broadcastController.SendSerializableBroadcast(
                MainServiceDownloadCountBroadcast,
                MainServiceDownloadCountBundle,
                new MainServiceDownloadCountContent(_downloadResultList, downloadPercentage, downloadPercentage >= 100)
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        _logger.Debug("onStartCommand");

        try {
            boolean downloadAll = intent.getBooleanExtra(MainServiceOnStartCommandBundle, false);
            if (downloadAll) {
                StartDownloadAll("onStartCommand");
            }
        } catch (Exception exception) {
            _logger.Error(exception.getMessage());
            Toasty.error(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        _logger.Debug("onBind");

        Bundle extras = intent.getExtras();
        if (extras != null) {
            _logger.Debug("onBind with extra");
            // _messenger = (Messenger) extras.get("MESSENGER");
            /*TODO do something with extras*/
        }

        return _mainServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

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
        _receiverController.RegisterReceiver(_movieDownloadFinishedReceiver, new String[]{MovieService.MovieDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherCurrentWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.CurrentWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherForecastWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.ForecastWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleDownloadFinishedReceiver, new String[]{ScheduleService.ScheduleDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_securityDownloadFinishedReceiver, new String[]{SecurityService.SecurityDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingListDownloadFinishedReceiver, new String[]{ShoppingListService.ShoppingListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_temperatureDownloadFinishedReceiver, new String[]{TemperatureService.TemperatureDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketDownloadFinishedReceiver, new String[]{WirelessSocketService.WirelessSocketDownloadFinishedBroadcast});

        _birthdayService = BirthdayService.getInstance();
        _coinService = CoinService.getInstance();
        _mapContentService = MapContentService.getInstance();
        _mediaMirrorService = MediaMirrorService.getInstance();
        _menuService = MenuService.getInstance();
        _movieService = MovieService.getInstance();
        _openWeatherService = OpenWeatherService.getInstance();
        _scheduleService = ScheduleService.getInstance();
        _securityService = SecurityService.getInstance();
        _shoppingListService = ShoppingListService.getInstance();
        _temperatureService = TemperatureService.getInstance();
        _userService = UserService.getInstance();
        _wirelessSocketService = WirelessSocketService.getInstance();

        _birthdayService.Initialize(
                this,
                BirthdayActivity.class,
                SettingsController.getInstance().IsBirthdayNotificationEnabled(),
                SettingsController.getInstance().IsReloadBirthdayEnabled(),
                SettingsController.getInstance().GetReloadBirthdayTimeout());

        _coinService.Initialize(
                this,
                SettingsController.getInstance().IsReloadCoinEnabled(),
                SettingsController.getInstance().GetReloadCoinTimeout());

        _mapContentService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMapContentEnabled(),
                SettingsController.getInstance().GetReloadMapContentTimeout());

        _mediaMirrorService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMediaMirrorEnabled(),
                SettingsController.getInstance().GetReloadMediaMirrorTimeout());

        _menuService.Initialize(
                this,
                SettingsController.getInstance().IsReloadMenuEnabled(),
                SettingsController.getInstance().GetReloadMenuTimeout());

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

        _currentDownloadCount = 0;
        _downloadResultList = new SerializableList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _receiverController.Dispose();

        _birthdayService.Dispose();
        _coinService.Dispose();
        _mapContentService.Dispose();
        _mediaMirrorService.Dispose();
        _menuService.Dispose();
        _movieService.Dispose();
        _openWeatherService.Dispose();
        _scheduleService.Dispose();
        _securityService.Dispose();
        _shoppingListService.Dispose();
        _temperatureService.Dispose();
        _userService.Dispose();
        _wirelessSocketService.Dispose();
    }
}
