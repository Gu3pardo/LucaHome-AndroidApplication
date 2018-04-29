package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.ILucaClass;
import guepardoapps.lucahome.common.classes.User;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SettingsController implements ISettingsController {
    private static final String Tag = SettingsController.class.getSimpleName();

    private static SettingsController Singleton = new SettingsController();

    private SharedPrefController _sharedPrefController;

    private static final String PREF_SETTINGS_INSTALLED_V6_0_0_180214 = "settings_v6.0.0.180214_installed";

    public static final String PREF_USER_UUID = "user_uuid";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_PASS_PHRASE = "user_passphrase";

    public static final String PREF_IP = "ip";
    public static final String PREF_SSID = "ssid";

    public static final String PREF_OPEN_WEATHER_CITY = "open_weather_city";
    public static final String PREF_CHANGE_WEATHER_WALLPAPER = "change_weather_wallpaper";

    public static final String PREF_NOTIFICATION_MESSAGE = "notifications_message";
    public static final String PREF_NOTIFICATION_MESSAGE_BIRTHDAY = "notifications_message_birthday";
    public static final String PREF_NOTIFICATION_MESSAGE_CAMERA = "notifications_message_camera";
    public static final String PREF_NOTIFICATION_MESSAGE_COINS = "notifications_message_coins";
    public static final String PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER = "notifications_message_current_weather";
    public static final String PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER = "notifications_message_forecast_weather";
    public static final String PREF_NOTIFICATION_MESSAGE_TEMPERATURE = "notifications_message_temperature";
    public static final String PREF_NOTIFICATION_MESSAGE_WIRELESS_SOCKETS = "notifications_message_wireless_sockets";
    public static final String PREF_NOTIFICATION_MESSAGE_WIRELESS_SWITCHES = "notifications_message_wireless_switches";

    public static final String PREF_RELOAD_DATA_ENABLED = "reload_data_enabled";
    public static final String PREF_RELOAD_BIRTHDAY_ENABLED = "reload_birthday_enabled";
    public static final String PREF_RELOAD_COIN_ENABLED = "reload_coin_enabled";
    public static final String PREF_RELOAD_LUCA_SECURITY_ENABLED = "reload_luca_security_enabled";
    public static final String PREF_RELOAD_MAP_CONTENT_ENABLED = "reload_map_content_enabled";
    public static final String PREF_RELOAD_MEDIA_SERVER_ENABLED = "reload_media_server_enabled";
    public static final String PREF_RELOAD_MEAL_ENABLED = "reload_meal_enabled";
    public static final String PREF_RELOAD_METER_LOG_ENABLED = "reload_meter_log_enabled";
    public static final String PREF_RELOAD_MONEY_LOG_ENABLED = "reload_money_log_enabled";
    public static final String PREF_RELOAD_MOVIE_ENABLED = "reload_movie_enabled";
    public static final String PREF_RELOAD_PUCK_JS_ENABLED = "reload_puck_js_enabled";
    public static final String PREF_RELOAD_ROOM_ENABLED = "reload_room_enabled";
    public static final String PREF_RELOAD_SHOPPING_ENABLED = "reload_shopping_enabled";
    public static final String PREF_RELOAD_TEMPERATURE_ENABLED = "reload_temperature_enabled";
    public static final String PREF_RELOAD_WEATHER_ENABLED = "reload_weather_enabled";
    public static final String PREF_RELOAD_WIRELESS_SCHEDULE_ENABLED = "reload_wireless_schedule_enabled";
    public static final String PREF_RELOAD_WIRELESS_SOCKET_ENABLED = "reload_wireless_socket_enabled";
    public static final String PREF_RELOAD_WIRELESS_SWITCH_ENABLED = "reload_wireless_switch_enabled";

    public static final String PREF_RELOAD_BIRTHDAY_TIMEOUT = "reload_birthday_timeout";
    public static final String PREF_RELOAD_COIN_TIMEOUT = "reload_coin_timeout";
    public static final String PREF_RELOAD_LUCA_SECURITY_TIMEOUT = "reload_luca_security_timeout";
    public static final String PREF_RELOAD_MAP_CONTENT_TIMEOUT = "reload_map_content_timeout";
    public static final String PREF_RELOAD_MEDIA_SERVER_TIMEOUT = "reload_media_server_timeout";
    public static final String PREF_RELOAD_MEAL_TIMEOUT = "reload_meal_timeout";
    public static final String PREF_RELOAD_METER_LOG_TIMEOUT = "reload_meter_log_timeout";
    public static final String PREF_RELOAD_MONEY_LOG_TIMEOUT = "reload_money_log_timeout";
    public static final String PREF_RELOAD_MOVIE_TIMEOUT = "reload_movie_timeout";
    public static final String PREF_RELOAD_PUCK_JS_TIMEOUT = "reload_puck_js_timeout";
    public static final String PREF_RELOAD_ROOM_TIMEOUT = "reload_room_timeout";
    public static final String PREF_RELOAD_SHOPPING_TIMEOUT = "reload_shopping_timeout";
    public static final String PREF_RELOAD_TEMPERATURE_TIMEOUT = "reload_temperature_timeout";
    public static final String PREF_RELOAD_WEATHER_TIMEOUT = "reload_weather_timeout";
    public static final String PREF_RELOAD_WIRELESS_SCHEDULE_TIMEOUT = "reload_wireless_schedule_timeout";
    public static final String PREF_RELOAD_WIRELESS_SOCKET_TIMEOUT = "reload_wireless_socket_timeout";
    public static final String PREF_RELOAD_WIRELESS_SWITCH_TIMEOUT = "reload_wireless_switch_timeout";

    public static final String PREF_HANDLE_BLUETOOTH_AUTOMATICALLY = "handle_bluetooth_automatically";
    public static final String PREF_BEACONS_SCAN_ENABLED = "beacons_scan_enabled";
    public static final String PREF_BEACONS_TIME_BETWEEN_SCANS_SEC = "beacons_time_between_scans_sec";
    public static final String PREF_BEACONS_TIME_SCANS_M_SEC = "beacons_time_scans_m_sec";

    public static final String PREF_COIN_HOURS_TREND = "coin_hours_trend";

    public static SettingsController getInstance() {
        return Singleton;
    }

    private SettingsController() {
    }

    public void Initialize(@NonNull Context context) {
        _sharedPrefController = new SharedPrefController(context);
        install();
    }

    public void SetUser(User user) {
        if (user != null) {
            _sharedPrefController.SaveStringValue(PREF_USER_UUID, user.GetUuid().toString());
            _sharedPrefController.SaveStringValue(PREF_USER_NAME, user.GetName());
            _sharedPrefController.SaveStringValue(PREF_USER_PASS_PHRASE, user.GetPassphrase());
        } else {
            Logger.getInstance().Error(Tag, "User may not be null! Cannot save!");
        }
    }

    public User GetUser() {
        UUID uuid = UUID.fromString(_sharedPrefController.LoadStringValueFromSharedPreferences(PREF_USER_UUID));

        String userName = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_USER_NAME);
        if (userName == null) {
            userName = "";
        }

        String userPassPhrase = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_USER_PASS_PHRASE);
        if (userPassPhrase == null) {
            userPassPhrase = "";
        }

        return new User(uuid, userName, userPassPhrase, true, ILucaClass.LucaServerDbAction.Null);
    }

    public String GetServerIp() {
        return _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_IP);
    }

    public String GetHomeSsid() {
        return _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_SSID);
    }

    public String GetOpenWeatherCity() {
        return _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_OPEN_WEATHER_CITY);
    }

    public boolean AreNotificationsEnabled() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE);
    }

    public boolean IsBirthdayNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_BIRTHDAY);
    }

    public boolean IsCameraNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_CAMERA);
    }

    public boolean IsCoinsNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_COINS);
    }

    public boolean IsCurrentWeatherNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER);
    }

    public boolean IsForecastWeatherNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER);
    }

    public boolean IsTemperatureNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_TEMPERATURE);
    }

    public boolean IsWirelessSocketNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_WIRELESS_SOCKETS);
    }

    public boolean IsWirelessSocketVisible(@NonNull WirelessSocket wirelessSocket) {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(wirelessSocket.GetSettingsKey());
    }

    public boolean IsWirelessSwitchNotificationEnabled() {
        return AreNotificationsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_NOTIFICATION_MESSAGE_WIRELESS_SWITCHES);
    }

    public boolean IsWirelessSwitchVisible(@NonNull WirelessSwitch wirelessSwitch) {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(wirelessSwitch.GetSettingsKey());
    }

    public boolean IsChangeWeatherWallpaperActive() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_CHANGE_WEATHER_WALLPAPER);
    }

    public boolean AreReloadsEnabled() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_DATA_ENABLED);
    }

    public boolean IsReloadBirthdayEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_BIRTHDAY_ENABLED);
    }

    public boolean IsReloadCoinEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_COIN_ENABLED);
    }

    public boolean IsReloadLucaSecurityEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_LUCA_SECURITY_ENABLED);
    }

    public boolean IsReloadMapContentEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MAP_CONTENT_ENABLED);
    }

    public boolean IsReloadMediaServerEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MEDIA_SERVER_ENABLED);
    }

    public boolean IsReloadMealEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MEAL_ENABLED);
    }

    public boolean IsReloadMeterLogEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_METER_LOG_ENABLED);
    }

    public boolean IsReloadMoneyLogEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MONEY_LOG_ENABLED);
    }

    public boolean IsReloadMovieEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_MOVIE_ENABLED);
    }

    public boolean IsReloadPuckJsEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_PUCK_JS_ENABLED);
    }

    public boolean IsReloadRoomEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_ROOM_ENABLED);
    }

    public boolean IsReloadShoppingEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_SHOPPING_ENABLED);
    }

    public boolean IsReloadTemperatureEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_TEMPERATURE_ENABLED);
    }

    public boolean IsReloadWeatherEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_WEATHER_ENABLED);
    }

    public boolean IsReloadWirelessScheduleEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_WIRELESS_SCHEDULE_ENABLED);
    }

    public boolean IsReloadWirelessSocketEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_WIRELESS_SOCKET_ENABLED);
    }

    public boolean IsReloadWirelessSwitchEnabled() {
        return AreReloadsEnabled() && _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_RELOAD_WIRELESS_SWITCH_ENABLED);
    }

    public int GetReloadBirthdayTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_BIRTHDAY_TIMEOUT);
    }

    public int GetReloadCoinTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_COIN_TIMEOUT);
    }

    public int GetReloadLucaSecurityTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_LUCA_SECURITY_TIMEOUT);
    }

    public int GetReloadMapContentTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_MAP_CONTENT_TIMEOUT);
    }

    public int GetReloadMediaServerTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_MEDIA_SERVER_TIMEOUT);
    }

    public int GetReloadMealTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_MEAL_TIMEOUT);
    }

    public int GetReloadMeterLogTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_METER_LOG_TIMEOUT);
    }

    public int GetReloadMoneyLogTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_MONEY_LOG_TIMEOUT);
    }

    public int GetReloadMovieTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_MOVIE_TIMEOUT);
    }

    public int GetReloadPuckJsTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_PUCK_JS_TIMEOUT);
    }

    public int GetReloadRoomTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_ROOM_TIMEOUT);
    }

    public int GetReloadShoppingTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_SHOPPING_TIMEOUT);
    }

    public int GetReloadTemperatureTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_TEMPERATURE_TIMEOUT);
    }

    public int GetReloadWeatherTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_WEATHER_TIMEOUT);
    }

    public int GetReloadWirelessScheduleTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_WIRELESS_SCHEDULE_TIMEOUT);
    }

    public int GetReloadWirelessSocketTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_WIRELESS_SOCKET_TIMEOUT);
    }

    public int GetReloadWirelessSwitchTimeout() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_RELOAD_WIRELESS_SWITCH_TIMEOUT);
    }

    public boolean HandleBluetoothAutomatically() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_HANDLE_BLUETOOTH_AUTOMATICALLY);
    }

    public boolean IsBeaconScanEnabled() {
        return _sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_BEACONS_SCAN_ENABLED);
    }

    public int GetTimeBetweenBeaconScansSec() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_BEACONS_TIME_BETWEEN_SCANS_SEC);
    }

    public int GetTimeBeaconScansMsec() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_BEACONS_TIME_SCANS_M_SEC);
    }

    public void SetCoinHoursTrend(int hours) {
        _sharedPrefController.SaveStringValue(PREF_COIN_HOURS_TREND, String.valueOf(hours));
    }

    public int GetCoinHoursTrend() {
        return _sharedPrefController.LoadIntegerValueFromSharedPreferences(PREF_COIN_HOURS_TREND);
    }

    private void install() {
        installV6_0_0_180214();
    }

    private void installV6_0_0_180214() {
        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(PREF_SETTINGS_INSTALLED_V6_0_0_180214)) {
            Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Already installed preferences %s", PREF_SETTINGS_INSTALLED_V6_0_0_180214));
            return;
        }

        Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "Installing preferences %s", PREF_SETTINGS_INSTALLED_V6_0_0_180214));

        _sharedPrefController.SaveStringValue(PREF_IP, "");
        _sharedPrefController.SaveStringValue(PREF_SSID, "");

        _sharedPrefController.SaveStringValue(PREF_OPEN_WEATHER_CITY, "");
        _sharedPrefController.SaveBooleanValue(PREF_CHANGE_WEATHER_WALLPAPER, true);

        _sharedPrefController.SaveStringValue(PREF_USER_UUID, "");
        _sharedPrefController.SaveStringValue(PREF_USER_NAME, "");
        _sharedPrefController.SaveStringValue(PREF_USER_PASS_PHRASE, "");

        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE, true);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_BIRTHDAY, true);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_CAMERA, true);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_COINS, false);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER, true);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER, true);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_TEMPERATURE, true);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_WIRELESS_SOCKETS, true);
        _sharedPrefController.SaveBooleanValue(PREF_NOTIFICATION_MESSAGE_WIRELESS_SWITCHES, true);

        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_DATA_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_BIRTHDAY_TIMEOUT, 240);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_BIRTHDAY_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_COIN_TIMEOUT, 30);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_COIN_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_LUCA_SECURITY_TIMEOUT, 15);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_LUCA_SECURITY_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_MAP_CONTENT_TIMEOUT, 240);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_MAP_CONTENT_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_MEDIA_SERVER_TIMEOUT, 30);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_MEDIA_SERVER_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_MEAL_TIMEOUT, 120);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_MEAL_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_METER_LOG_TIMEOUT, 240);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_METER_LOG_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_MONEY_LOG_TIMEOUT, 240);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_MONEY_LOG_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_MOVIE_TIMEOUT, 60);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_MOVIE_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_PUCK_JS_TIMEOUT, 60);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_PUCK_JS_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_ROOM_TIMEOUT, 120);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_ROOM_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_SHOPPING_TIMEOUT, 60);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_SHOPPING_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_TEMPERATURE_TIMEOUT, 15);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_TEMPERATURE_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_WEATHER_TIMEOUT, 5);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_WEATHER_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_WIRELESS_SCHEDULE_TIMEOUT, 60);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_WIRELESS_SCHEDULE_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_WIRELESS_SOCKET_TIMEOUT, 15);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_WIRELESS_SOCKET_ENABLED, true);

        _sharedPrefController.SaveIntegerValue(PREF_RELOAD_WIRELESS_SWITCH_TIMEOUT, 15);
        _sharedPrefController.SaveBooleanValue(PREF_RELOAD_WIRELESS_SWITCH_ENABLED, true);

        _sharedPrefController.SaveBooleanValue(PREF_HANDLE_BLUETOOTH_AUTOMATICALLY, false);
        _sharedPrefController.SaveBooleanValue(PREF_BEACONS_SCAN_ENABLED, false);
        _sharedPrefController.SaveIntegerValue(PREF_BEACONS_TIME_BETWEEN_SCANS_SEC, 15);
        _sharedPrefController.SaveIntegerValue(PREF_BEACONS_TIME_SCANS_M_SEC, 30000);

        _sharedPrefController.SaveIntegerValue(PREF_COIN_HOURS_TREND, 24);

        _sharedPrefController.SaveBooleanValue(PREF_SETTINGS_INSTALLED_V6_0_0_180214, true);
    }
}
