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
import guepardoapps.lucahome.receiver.SwitchActionReceiver;
import guepardoapps.lucahome.views.BirthdayActivity;
import guepardoapps.lucahome.views.CoinActivity;
import guepardoapps.lucahome.views.ForecastWeatherActivity;
import guepardoapps.lucahome.views.MainActivity;
import guepardoapps.lucahome.views.SecurityActivity;

public class MainService extends Service {
    private static final String TAG = MainService.class.getSimpleName();

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

    public class MainServiceBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    private final IBinder _mainServiceBinder = new MainServiceBinder();

    private static final SparseArray<Runnable> DOWNLOAD_HASH_MAP = new SparseArray<>();

    static {
        DOWNLOAD_HASH_MAP.append(0, () -> BirthdayService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(1, () -> CoinService.getInstance().LoadCoinConversionList());
        DOWNLOAD_HASH_MAP.append(2, () -> CoinService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(3, () -> ListedMenuService.getInstance().LoadData());
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
        DOWNLOAD_HASH_MAP.append(16, () -> TimerService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(17, () -> PuckJsListService.getInstance().LoadData());
        DOWNLOAD_HASH_MAP.append(18, () -> MapContentService.getInstance().LoadData());
    }

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
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _coinConversionDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _coinDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _mapContentDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _listedMenuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _menuDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _meterListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _moneyMeterListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _movieDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _openWeatherCurrentWeatherDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _puckJsDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _openWeatherForecastWeatherDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _scheduleDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _securityDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _shoppingListDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _temperatureDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _timerDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _wirelessSocketDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    private BroadcastReceiver _wirelessSwitchDownloadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcastDownloadCount();
            _receiverController.UnregisterReceiver(this);
        }
    };

    public void StartDownloadAll(@NonNull String caller) {
        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "StartDownloadAll by caller %s", caller));

        if (_downloading) {
            return;
        }

        registerReceiver();

        _currentDownloadCount = 0;
        _downloading = true;

        DOWNLOAD_HASH_MAP.get(_currentDownloadCount).run();
    }

    public void Cancel() {
        Logger.getInstance().Warning(TAG, "Cancel");
        stopSelf();
    }

    private void broadcastDownloadCount() {
        _currentDownloadCount++;

        double downloadPercentage = (_currentDownloadCount * 100) / DOWNLOAD_HASH_MAP.size();
        _downloading = !(downloadPercentage >= 100);

        Logger.getInstance().Information(TAG, String.format(Locale.getDefault(),
                "broadcastDownloadCount: _currentDownloadCount: %d | downloadPercentage: %.2f | _downloading: %s",
                _currentDownloadCount, downloadPercentage, _downloading));

        _broadcastController.SendSerializableBroadcast(
                MainServiceDownloadCountBroadcast,
                MainServiceDownloadCountBundle,
                new MainServiceDownloadCountContent(downloadPercentage, !_downloading)
        );

        if (_currentDownloadCount < DOWNLOAD_HASH_MAP.size()) {
            DOWNLOAD_HASH_MAP.get(_currentDownloadCount).run();
        } else {
            _receiverController.Dispose();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Logger.getInstance().Information(TAG, "onStartCommand");

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
        Logger.getInstance().Information(TAG, "onBind");
        return _mainServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.getInstance().Information(TAG, "onCreate");

        _broadcastController = new BroadcastController(this);
        _receiverController = new ReceiverController(this);

        SettingsController.getInstance().Initialize(this);

        BirthdayService.getInstance().Initialize(
                this,
                BirthdayActivity.class,
                SettingsController.getInstance().IsBirthdayNotificationEnabled(),
                SettingsController.getInstance().IsReloadBirthdayEnabled(),
                SettingsController.getInstance().GetReloadBirthdayTimeout());

        CoinService.getInstance().Initialize(
                this,
                CoinActivity.class,
                SettingsController.getInstance().IsCoinsNotificationEnabled(),
                SettingsController.getInstance().IsReloadCoinEnabled(),
                SettingsController.getInstance().GetReloadCoinTimeout());

        ListedMenuService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadMenuEnabled(),
                SettingsController.getInstance().GetReloadMenuTimeout());

        MapContentService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadMapContentEnabled(),
                SettingsController.getInstance().GetReloadMapContentTimeout());

        MediaServerService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadMediaServerEnabled(),
                SettingsController.getInstance().GetReloadMediaServerTimeout());

        MenuService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadMenuEnabled(),
                SettingsController.getInstance().GetReloadMenuTimeout());

        MeterListService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadMeterDataEnabled(),
                SettingsController.getInstance().GetReloadMeterDataTimeout()
        );

        MoneyMeterListService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadMoneyMeterDataEnabled(),
                SettingsController.getInstance().GetReloadMoneyMeterDataTimeout()
        );

        MovieService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadMovieEnabled(),
                SettingsController.getInstance().GetReloadMovieTimeout());

        OpenWeatherService.getInstance().Initialize(
                this,
                SettingsController.getInstance().GetOpenWeatherCity(),
                SettingsController.getInstance().IsCurrentWeatherNotificationEnabled(),
                SettingsController.getInstance().IsForecastWeatherNotificationEnabled(),
                MainActivity.class,
                ForecastWeatherActivity.class,
                SettingsController.getInstance().IsChangeWeatherWallpaperActive(),
                SettingsController.getInstance().IsReloadWeatherEnabled(),
                SettingsController.getInstance().GetReloadWeatherTimeout());

        PuckJsListService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadPuckJsEnabled(),
                SettingsController.getInstance().GetReloadPuckJsTimeout());

        ScheduleService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadScheduleEnabled(),
                SettingsController.getInstance().GetReloadScheduleTimeout());

        SecurityService.getInstance().Initialize(
                this,
                SecurityActivity.class,
                SettingsController.getInstance().IsCameraNotificationEnabled(),
                SettingsController.getInstance().IsReloadSecurityEnabled(),
                SettingsController.getInstance().GetReloadSecurityTimeout());

        ShoppingListService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadShoppingEnabled(),
                SettingsController.getInstance().GetReloadShoppingTimeout());

        TemperatureService.getInstance().Initialize(
                this,
                MainActivity.class,
                SettingsController.getInstance().IsTemperatureNotificationEnabled(),
                SettingsController.getInstance().IsReloadTemperatureEnabled(),
                SettingsController.getInstance().GetReloadTemperatureTimeout());

        TimerService.getInstance().Initialize(
                this,
                SettingsController.getInstance().IsReloadScheduleEnabled(),
                SettingsController.getInstance().GetReloadScheduleTimeout());

        UserService.getInstance().Initialize(this);

        WirelessSocketService.getInstance().Initialize(
                this,
                SocketActionReceiver.class,
                SettingsController.getInstance().IsSocketNotificationEnabled(),
                SettingsController.getInstance().IsReloadWirelessSocketEnabled(),
                SettingsController.getInstance().GetReloadWirelessSocketTimeout());

        WirelessSwitchService.getInstance().Initialize(
                this,
                SwitchActionReceiver.class,
                SettingsController.getInstance().IsSwitchNotificationEnabled(),
                SettingsController.getInstance().IsReloadWirelessSwitchEnabled(),
                SettingsController.getInstance().GetReloadWirelessSwitchTimeout()
        );

        _currentDownloadCount = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.getInstance().Information(TAG, "onDestroy");

        _receiverController.Dispose();

        BirthdayService.getInstance().Dispose();
        CoinService.getInstance().Dispose();
        ListedMenuService.getInstance().Dispose();
        MapContentService.getInstance().Dispose();
        MediaServerService.getInstance().Dispose();
        MenuService.getInstance().Dispose();
        MeterListService.getInstance().Dispose();
        MoneyMeterListService.getInstance().Dispose();
        MovieService.getInstance().Dispose();
        OpenWeatherService.getInstance().Dispose();
        PuckJsListService.getInstance().Dispose();
        ScheduleService.getInstance().Dispose();
        SecurityService.getInstance().Dispose();
        ShoppingListService.getInstance().Dispose();
        TemperatureService.getInstance().Dispose();
        TimerService.getInstance().Dispose();
        UserService.getInstance().Dispose();
        WirelessSocketService.getInstance().Dispose();
        WirelessSwitchService.getInstance().Dispose();
    }

    private void registerReceiver() {
        Logger.getInstance().Information(TAG, "registerReceiver");

        _receiverController.RegisterReceiver(_startDownloadAllReceiver, new String[]{MainServiceStartDownloadAllBroadcast});
        _receiverController.RegisterReceiver(_birthdayDownloadFinishedReceiver, new String[]{BirthdayService.BirthdayDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinConversionDownloadFinishedReceiver, new String[]{CoinService.CoinConversionDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_coinDownloadFinishedReceiver, new String[]{CoinService.CoinDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_mapContentDownloadFinishedReceiver, new String[]{MapContentService.MapContentDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_listedMenuDownloadFinishedReceiver, new String[]{ListedMenuService.ListedMenuDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_menuDownloadFinishedReceiver, new String[]{MenuService.MenuDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_meterListDownloadFinishedReceiver, new String[]{MeterListService.MeterDataListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_moneyMeterListDownloadFinishedReceiver, new String[]{MoneyMeterListService.MoneyMeterDataListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_movieDownloadFinishedReceiver, new String[]{MovieService.MovieDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherCurrentWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.CurrentWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_openWeatherForecastWeatherDownloadFinishedReceiver, new String[]{OpenWeatherService.ForecastWeatherDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_puckJsDownloadFinishedReceiver, new String[]{PuckJsListService.PuckJsListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_scheduleDownloadFinishedReceiver, new String[]{ScheduleService.ScheduleDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_securityDownloadFinishedReceiver, new String[]{SecurityService.SecurityDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_shoppingListDownloadFinishedReceiver, new String[]{ShoppingListService.ShoppingListDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_temperatureDownloadFinishedReceiver, new String[]{TemperatureService.TemperatureDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_timerDownloadFinishedReceiver, new String[]{TimerService.TimerDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSocketDownloadFinishedReceiver, new String[]{WirelessSocketService.WirelessSocketDownloadFinishedBroadcast});
        _receiverController.RegisterReceiver(_wirelessSwitchDownloadFinishedReceiver, new String[]{WirelessSwitchService.WirelessSwitchDownloadFinishedBroadcast});
    }
}
