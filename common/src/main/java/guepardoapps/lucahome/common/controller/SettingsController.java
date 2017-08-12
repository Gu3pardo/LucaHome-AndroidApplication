package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.basic.controller.SharedPrefController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaUser;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.R;

public class SettingsController {
    private static final SettingsController SINGLETON = new SettingsController();

    private static final String TAG = SettingsController.class.getSimpleName();
    private Logger _logger;

    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_PASS_PHRASE = "user_passphrase";

    public static final String PREF_IP = "ip";
    public static final String PREF_SSID = "ssid";

    public static final String PREF_OPEN_WEATHER_CITY = "open_weather_city";

    public static final String PREF_NOTIFICATION_MESSAGE = "notifications_message";
    public static final String PREF_NOTIFICATION_MESSAGE_SOCKETS = "notifications_message_sockets";
    public static final String PREF_NOTIFICATION_MESSAGE_BIRTHDAY = "notifications_message_birthday";
    public static final String PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER = "notifications_message_current_weather";
    public static final String PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER = "notifications_message_forecast_weather";
    public static final String PREF_NOTIFICATION_MESSAGE_TEMPERATURE = "notifications_message_temperature";

    private Context _context;
    private SharedPrefController _sharedPrefController;

    private boolean _isInitialized;

    private SettingsController() {
        _logger = new Logger(TAG);
    }

    public static SettingsController getInstance() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        if (_isInitialized) {
            _logger.Error("Already initialized!");
            return;
        }

        _context = context;
        _sharedPrefController = new SharedPrefController(_context);

        _isInitialized = true;
    }

    public LucaUser GetUser() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new LucaUser("DUMMY", "DUMMY");
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
            _logger.Debug(String.format(Locale.getDefault(), "SetUser: %s", user));

            _sharedPrefController.SaveStringValue(PREF_USER_NAME, user.GetName());
            _sharedPrefController.SaveStringValue(PREF_USER_PASS_PHRASE, user.GetPassphrase());
        } else {
            _logger.Error("User may not be null! Cannot save!");
        }
    }

    public String GetServerIp() {
        String serverIp = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_IP);
        if (serverIp == null) {
            serverIp = _context.getResources().getString(R.string.pref_default_ip);
        }
        return serverIp;
    }

    public String GetHomeSsid() {
        String serverSsid = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_SSID);
        if (serverSsid == null) {
            serverSsid = _context.getResources().getString(R.string.pref_default_ssid);
        }
        return serverSsid;
    }

    public String GetOpenWeatherCity() {
        String openWeatherCity = _sharedPrefController.LoadStringValueFromSharedPreferences(PREF_OPEN_WEATHER_CITY);
        if (openWeatherCity == null) {
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
}
