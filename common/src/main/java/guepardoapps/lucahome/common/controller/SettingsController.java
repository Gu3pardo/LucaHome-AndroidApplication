package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.SharedPrefController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.R;

public class SettingsController {
    private static final SettingsController SINGLETON = new SettingsController();

    private static final String TAG = SettingsController.class.getSimpleName();

    private static final String PREF_SETTINGS_INSTALLED = "settings_v5.2.0.180105_installed";

    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_PASS_PHRASE = "user_passphrase";

    public static final String PREF_IP = "ip";
    public static final String PREF_SSID = "ssid";

    public static final String PREF_OPEN_WEATHER_CITY = "open_weather_city";
    public static final String PREF_CHANGE_WEATHER_WALLPAPER = "change_weather_wallpaper";

    public static final String PREF_NOTIFICATION_MESSAGE = "notifications_message";
    public static final String PREF_NOTIFICATION_MESSAGE_SOCKETS = "notifications_message_sockets";
    public static final String PREF_NOTIFICATION_MESSAGE_BIRTHDAY = "notifications_message_birthday";
    public static final String PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER = "notifications_message_current_weather";
    public static final String PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER = "notifications_message_forecast_weather";
    public static final String PREF_NOTIFICATION_MESSAGE_TEMPERATURE = "notifications_message_temperature";
    public static final String PREF_NOTIFICATION_MESSAGE_CAMERA = "notifications_message_camera";
    public static final String PREF_NOTIFICATION_MESSAGE_COINS = "notifications_message_coins";
    public static final String PREF_NOTIFICATION_MESSAGE_SWITCHES = "notifications_message_switches";

    public static final String PREF_RELOAD_DATA_ENABLED = "reload_data_enabled";
    public static final String PREF_RELOAD_BIRTHDAY_ENABLED = "reload_birthday_enabled";
    public static final String PREF_RELOAD_COIN_ENABLED = "reload_coin_enabled";
    public static final String PREF_RELOAD_MAPCONTENT_ENABLED = "reload_mapcontent_enabled";
    public static final String PREF_RELOAD_MEDIASERVER_ENABLED = "reload_mediaserver_enabled";
    public static final String PREF_RELOAD_MENU_ENABLED = "reload_menu_enabled";
    public static final String PREF_RELOAD_METER_DATA_ENABLED = "reload_meter_data_enabled";
    public static final String PREF_RELOAD_MONEY_METER_DATA_ENABLED = "reload_money_meter_data_enabled";
    public static final String PREF_RELOAD_MOVIE_ENABLED = "reload_movie_enabled";
    public static final String PREF_RELOAD_PUCKJS_ENABLED = "reload_puckjs_enabled";
    public static final String PREF_RELOAD_SCHEDULE_ENABLED = "reload_schedule_enabled";
    public static final String PREF_RELOAD_SECURITY_ENABLED = "reload_security_enabled";
    public static final String PREF_RELOAD_SHOPPING_ENABLED = "reload_shopping_enabled";
    public static final String PREF_RELOAD_TEMPERATURE_ENABLED = "reload_temperature_enabled";
    public static final String PREF_RELOAD_WEATHER_ENABLED = "reload_weather_enabled";
    public static final String PREF_RELOAD_WIRELESSSOCKET_ENABLED = "reload_wirelesssocket_enabled";
    public static final String PREF_RELOAD_WIRELESSSWITCH_ENABLED = "reload_wirelessswitch_enabled";

    public static final String PREF_RELOAD_BIRTHDAY_TIMEOUT = "reload_birthday_timeout";
    public static final String PREF_RELOAD_COIN_TIMEOUT = "reload_coin_timeout";
    public static final String PREF_RELOAD_MAPCONTENT_TIMEOUT = "reload_mapcontent_timeout";
    public static final String PREF_RELOAD_MEDIASERVER_TIMEOUT = "reload_mediaserver_timeout";
    public static final String PREF_RELOAD_MENU_TIMEOUT = "reload_menu_timeout";
    public static final String PREF_RELOAD_METER_DATA_TIMEOUT = "reload_meter_data_timeout";
    public static final String PREF_RELOAD_MONEY_METER_DATA_TIMEOUT = "reload_money_meter_data_timeout";
    public static final String PREF_RELOAD_MOVIE_TIMEOUT = "reload_movie_timeout";
    public static final String PREF_RELOAD_PUCKJS_TIMEOUT = "reload_puckjs_timeout";
    public static final String PREF_RELOAD_SCHEDULE_TIMEOUT = "reload_schedule_timeout";
    public static final String PREF_RELOAD_SECURITY_TIMEOUT = "reload_security_timeout";
    public static final String PREF_RELOAD_SHOPPING_TIMEOUT = "reload_shopping_timeout";
    public static final String PREF_RELOAD_TEMPERATURE_TIMEOUT = "reload_temperature_timeout";
    public static final String PREF_RELOAD_WEATHER_TIMEOUT = "reload_weather_timeout";
    public static final String PREF_RELOAD_WIRELESSSOCKET_TIMEOUT = "reload_wirelesssocket_timeout";
    public static final String PREF_RELOAD_WIRELESSSWITCH_TIMEOUT = "reload_wirelessswitch_timeout";

    public static final String PREF_HANDLE_BLUETOOTH_AUTOMATICALLY = "handle_bluetooth_automatically";
    public static final String PREF_BEACONS_SCAN_ENABLED = "beacons_scan_enabled";
    public static final String PREF_BEACONS_TIME_BETWEEN_SCANS_SEC = "beacons_time_between_scans_sec";
    public static final String PREF_BEACONS_TIME_SCANS_MSEC = "beacons_time_scans_msec";

    public static final String PREF_COIN_HOURS_TREND = "coin_hours_trend";

    private Context _context;
    private SharedPrefController _sharedPrefController;

    private boolean _isInitialized;

    private SettingsController() {
    }

    public static SettingsController getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        if (_isInitialized) {
            Logger.getInstance().Error(TAG, "Already initialized!");
            return;
        }

        _context = context;
        _sharedPrefController = new SharedPrefController(_context);

        _isInitialized = true;

        install();
    }

    private void install() {
        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_SETTINGS_INSTALLED)) {
            return;
        }

        _sharedPrefController.SaveStringValue(PREF_RELOAD_BIRTHDAY_TIMEOUT, "240");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_COIN_TIMEOUT, "30");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_MAPCONTENT_TIMEOUT, "240");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_MEDIASERVER_TIMEOUT, "30");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_MENU_TIMEOUT, "120");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_METER_DATA_TIMEOUT, "240");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_MONEY_METER_DATA_TIMEOUT, "240");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_MOVIE_TIMEOUT, "60");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_PUCKJS_TIMEOUT, "60");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_SCHEDULE_TIMEOUT, "60");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_SECURITY_TIMEOUT, "15");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_SHOPPING_TIMEOUT, "60");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_TEMPERATURE_TIMEOUT, "15");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_WEATHER_TIMEOUT, "5");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_WIRELESSSOCKET_TIMEOUT, "15");
        _sharedPrefController.SaveStringValue(PREF_RELOAD_WIRELESSSWITCH_TIMEOUT, "15");

        _sharedPrefController.SaveStringValue(PREF_BEACONS_TIME_BETWEEN_SCANS_SEC, "15");
        _sharedPrefController.SaveStringValue(PREF_BEACONS_TIME_SCANS_MSEC, "30000");

        _sharedPrefController.SaveBooleanValue(PREF_SETTINGS_INSTALLED, true);
    }

    public LucaUser GetUser() {
        if (!_isInitialized) {
            Logger.getInstance().Error(TAG, "Not initialized!");
            return null;
        }

        String userName = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_USER_NAME);
        if (userName == null) {
            userName = _context.getResources().getString(R.string.pref_default_user_name);
        }

        String userPassPhrase = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_USER_PASS_PHRASE);
        if (userPassPhrase == null) {
            userPassPhrase = _context.getResources().getString(R.string.pref_default_user_passphrase);
        }

        return new LucaUser(userName, userPassPhrase);
    }

    public void SetUser(LucaUser user) {
        if (user != null) {
            _sharedPrefController.SaveStringValue(PREF_USER_NAME, user.GetName());
            _sharedPrefController.SaveStringValue(PREF_USER_PASS_PHRASE, user.GetPassphrase());
        } else {
            Logger.getInstance().Error(TAG, "User may not be null! Cannot save!");
        }
    }

    public String GetServerIp() {
        String serverIp = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_IP);
        if (serverIp == null || serverIp.length() == 0) {
            serverIp = _context.getResources().getString(R.string.pref_default_ip);
        }
        return serverIp;
    }

    public String GetHomeSsid() {
        String serverSsid = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_SSID);
        if (serverSsid == null || serverSsid.length() == 0) {
            serverSsid = _context.getResources().getString(R.string.pref_default_ssid);
        }
        return serverSsid;
    }

    public String GetOpenWeatherCity() {
        String openWeatherCity = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_OPEN_WEATHER_CITY);
        if (openWeatherCity == null || openWeatherCity.length() == 0) {
            openWeatherCity = _context.getResources().getString(R.string.pref_default_open_weather_city);
        }
        return openWeatherCity;
    }

    public boolean AreNotificationsEnabled() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE);
    }

    public boolean IsSocketNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_SOCKETS);
    }

    public boolean IsBirthdayNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_BIRTHDAY);
    }

    public boolean IsCurrentWeatherNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER);
    }

    public boolean IsForecastWeatherNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER);
    }

    public boolean IsTemperatureNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_TEMPERATURE);
    }

    public boolean IsWirelessSocketVisible(@NonNull WirelessSocket wirelessSocket) {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(wirelessSocket.GetSettingsKey());
    }

    public boolean IsCameraNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_CAMERA);
    }

    public boolean IsCoinsNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_COINS);
    }

    public boolean IsSwitchNotificationEnabled() {
        if (!AreNotificationsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_SWITCHES);
    }

    public boolean IsChangeWeatherWallpaperActive() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_CHANGE_WEATHER_WALLPAPER);
    }

    public boolean AreReloadsEnabled() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_DATA_ENABLED);
    }

    public boolean IsReloadBirthdayEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_BIRTHDAY_ENABLED);
    }

    public boolean IsReloadCoinEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_COIN_ENABLED);
    }

    public boolean IsReloadMapContentEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MAPCONTENT_ENABLED);
    }

    public boolean IsReloadMediaServerEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MEDIASERVER_ENABLED);
    }

    public boolean IsReloadMenuEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MENU_ENABLED);
    }

    public boolean IsReloadMeterDataEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_METER_DATA_ENABLED);
    }

    public boolean IsReloadMoneyMeterDataEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MONEY_METER_DATA_ENABLED);
    }

    public boolean IsReloadMovieEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MOVIE_ENABLED);
    }

    public boolean IsReloadPuckJsEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_PUCKJS_ENABLED);
    }

    public boolean IsReloadScheduleEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_SCHEDULE_ENABLED);
    }

    public boolean IsReloadSecurityEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_SECURITY_ENABLED);
    }

    public boolean IsReloadShoppingEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_SHOPPING_ENABLED);
    }

    public boolean IsReloadTemperatureEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_TEMPERATURE_ENABLED);
    }

    public boolean IsReloadWeatherEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_WEATHER_ENABLED);
    }

    public boolean IsReloadWirelessSocketEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_WIRELESSSOCKET_ENABLED);
    }

    public boolean IsReloadWirelessSwitchEnabled() {
        if (!AreReloadsEnabled()) {
            return false;
        }
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_WIRELESSSWITCH_ENABLED);
    }

    public int GetReloadBirthdayTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_BIRTHDAY_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadCoinTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_COIN_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadMapContentTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_MAPCONTENT_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadMediaServerTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_MEDIASERVER_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadMenuTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_MENU_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadMeterDataTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_METER_DATA_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadMoneyMeterDataTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_MONEY_METER_DATA_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadMovieTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_MOVIE_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadPuckJsTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_PUCKJS_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadScheduleTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_SCHEDULE_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadSecurityTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_SECURITY_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadShoppingTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_SHOPPING_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadTemperatureTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_TEMPERATURE_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadWeatherTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_WEATHER_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadWirelessSocketTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_WIRELESSSOCKET_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public int GetReloadWirelessSwitchTimeout() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_RELOAD_WIRELESSSWITCH_TIMEOUT);
        return Integer.parseInt(stringValue);
    }

    public boolean HandleBluetoothAutomatically() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_HANDLE_BLUETOOTH_AUTOMATICALLY);
    }

    public boolean IsBeaconScanEnabled() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_BEACONS_SCAN_ENABLED);
    }

    public int GetTimeBetweenBeaconScansSec() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_BEACONS_TIME_BETWEEN_SCANS_SEC);
        return Integer.parseInt(stringValue);
    }

    public int GetTimeBeaconScansMsec() {
        String stringValue = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_BEACONS_TIME_SCANS_MSEC);
        return Integer.parseInt(stringValue);
    }

    public void SetCoinHoursTrend(int hours) {
        _sharedPrefController.SaveIntegerValue(PREF_COIN_HOURS_TREND, hours);
    }

    public int GetCoinHoursTrend() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_COIN_HOURS_TREND);
    }
}
