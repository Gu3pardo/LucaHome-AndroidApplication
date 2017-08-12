package guepardoapps.lucahome.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.classes.SerializablePair;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.data.controller.SettingsController;
import guepardoapps.lucahome.data.service.*;
import guepardoapps.lucahome.receiver.SocketActionReceiver;
import guepardoapps.lucahome.views.BirthdayActivity;

public class MainService extends Service {
    public class MainServiceBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    public static class MainServiceDownloadCountContent implements Serializable {
        public SerializableList<SerializablePair> DownloadResultList;
        public double DownloadProgress;
        public boolean DownloadFinished;

        public MainServiceDownloadCountContent(SerializableList<SerializablePair> downloadResultList, double downloadProgress, boolean downloadFinished) {
            DownloadResultList = downloadResultList;
            DownloadProgress = downloadProgress;
            DownloadFinished = downloadFinished;
        }
    }

    public static final String MainServiceDownloadCountBroadcast = "guepardoapps.lucahome.main.service.download.count";
    public static final String MainServiceDownloadCountBundle = "MainServiceDownloadCountBundle";

    private static final String TAG = MainService.class.getSimpleName();
    private Logger _logger;

    private final IBinder _mainServiceBinder = new MainServiceBinder();
    private Messenger _messenger;
    private Context _context;

    private BirthdayService _birthdayService;
    private CoinService _coinService;
    private LibraryService _libraryService;
    private MapContentService _mapContentService;
    private MenuService _menuService;
    private MovieService _movieService;
    private OpenWeatherService _openWeatherService;
    private ScheduleService _scheduleService;
    private ShoppingListService _shoppingListService;
    private TemperatureService _temperatureService;
    private UserService _userService;
    private WirelessSocketService _wirelessSocketService;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;
    private SettingsController _settingsController;

    private int _currentDownloadCount;
    private SerializableList<SerializablePair> _downloadResultList;
    private boolean _downloading;

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

    private BroadcastReceiver _libraryDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_libraryDownloadFinishedReceiver");
            LibraryService.MagazinDownloadFinishedContent result = (LibraryService.MagazinDownloadFinishedContent) intent.getSerializableExtra(LibraryService.MagazinDownloadFinishedBundle);
            if (result != null) {
                _downloadResultList.addValue(new SerializablePair("Library", result.Success));
            } else {
                _downloadResultList.addValue(new SerializablePair("Library", false));
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

            _temperatureService.LoadTemperatureList();
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

            if (_temperatureService.GetTemperatureList() != null) {
                _mapContentService.LoadMapContentList();
            }
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

            if (_scheduleService.GetScheduleList() != null) {
                _mapContentService.LoadMapContentList();
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

            _scheduleService.LoadScheduleList();
        }
    };

    public void Initialize() {
        if (_logger == null) {
            _logger = new Logger(TAG);
        }
        _logger.Debug("Initialize");

        _receiverController.RegisterReceiver(_birthdayDownloadFinishedReceiver, new String[]{BirthdayService.BirthdayDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinConversionDownloadFinishedReceiver, new String[]{CoinService.CoinConversionDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDownloadFinishedReceiver, new String[]{CoinService.CoinDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_libraryDownloadFinishedReceiver, new String[]{LibraryService.MagazinDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_mapContentDownloadFinishedReceiver, new String[]{MapContentService.MapContentDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_listedMenuDownloadFinishedReceiver, new String[]{MenuService.ListedMenuDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuDownloadFinishedReceiver, new String[]{MenuService.MenuDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_movieDownloadFinishedReceiver, new String[]{MovieService.MovieDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherCurrentWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.CurrentWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherForecastWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.ForecastWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleDownloadFinishedReceiver, new String[]{ScheduleService.ScheduleDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingListDownloadFinishedReceiver, new String[]{ShoppingListService.ShoppingListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_temperatureDownloadFinishedReceiver, new String[]{TemperatureService.TemperatureDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketDownloadFinishedReceiver, new String[]{WirelessSocketService.WirelessSocketDownloadFinishedBroadcast});

        _birthdayService.Initialize(_context, BirthdayActivity.class);
        _coinService.Initialize(_context);
        _libraryService.Initialize(_context);
        _mapContentService.Initialize(_context);
        _menuService.Initialize(_context);
        _movieService.Initialize(_context);
        _openWeatherService.Initialize(_context, _settingsController.GetOpenWeatherCity());
        _scheduleService.Initialize(_context);
        _shoppingListService.Initialize(_context);
        _temperatureService.Initialize(_context);
        _userService.Initialize(_context);
        _wirelessSocketService.Initialize(_context, SocketActionReceiver.class);

        _currentDownloadCount = 0;
        _downloadResultList = new SerializableList<>();
    }

    public void StartDownloadAll(@NonNull String caller) {
        _logger.Debug("StartDownloadAll with " + caller);

        if (_downloading) {
            _logger.Debug("Already downloading!");
            return;
        }

        _currentDownloadCount = 0;
        _downloadResultList = new SerializableList<>();
        _downloading = true;

        _birthdayService.LoadBirthdayList();
        _coinService.LoadCoinConversionList();
        _libraryService.LoadMagazinList();
        _menuService.LoadListedMenuList();
        _menuService.LoadMenuList();
        _movieService.LoadMovieList();
        _openWeatherService.LoadCurrentWeather();
        _openWeatherService.LoadForecastWeather();
        _shoppingListService.LoadShoppingList();
        _wirelessSocketService.LoadWirelessSocketList();
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
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        _logger.Debug("onBind");

        Bundle extras = intent.getExtras();
        if (extras != null) {
            _logger.Debug("onBind with extra");
            _messenger = (Messenger) extras.get("MESSENGER");
            /*TODO do something with extras*/
        }

        return _mainServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _logger = new Logger(TAG);
        _logger.Debug("onCreate");

        _context = this;

        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
        _settingsController = SettingsController.getInstance();
        _settingsController.Initialize(_context);

        _birthdayService = BirthdayService.getInstance();
        _coinService = CoinService.getInstance();
        _libraryService = LibraryService.getInstance();
        _mapContentService = MapContentService.getInstance();
        _menuService = MenuService.getInstance();
        _movieService = MovieService.getInstance();
        _openWeatherService = OpenWeatherService.getInstance();
        _scheduleService = ScheduleService.getInstance();
        _shoppingListService = ShoppingListService.getInstance();
        _temperatureService = TemperatureService.getInstance();
        _userService = UserService.getInstance();
        _wirelessSocketService = WirelessSocketService.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }
}
