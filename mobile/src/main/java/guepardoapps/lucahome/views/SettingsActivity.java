package guepardoapps.lucahome.views;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import de.mateware.snacky.Snacky;
import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.controller.SharedPrefController;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.controller.SettingsController;
import guepardoapps.lucahome.common.service.BirthdayService;
import guepardoapps.lucahome.common.service.CoinService;
import guepardoapps.lucahome.common.service.MapContentService;
import guepardoapps.lucahome.common.service.MediaServerService;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.lucahome.common.service.MeterListService;
import guepardoapps.lucahome.common.service.MoneyMeterListService;
import guepardoapps.lucahome.common.service.MovieService;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.common.service.SecurityService;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.common.service.TemperatureService;
import guepardoapps.lucahome.common.service.UserService;
import guepardoapps.lucahome.common.service.WirelessSocketService;
import guepardoapps.lucahome.common.service.WirelessSwitchService;
import guepardoapps.lucahome.common.service.broadcasts.content.ObjectChangeFinishedContent;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    // private static final String TAG = SettingsActivity.class.getSimpleName();

    private ReceiverController _receiverController;
    private static SharedPrefController _sharedPrefController;
    private static SettingsController _settingsController;

    //private NavigationService _navigationService;

    private static BirthdayService _birthdayService;
    private static CoinService _coinService;
    private static MapContentService _mapContentService;
    private static MediaServerService _mediaServerService;
    private static MenuService _menuService;
    private static MeterListService _meterListService;
    private static MoneyMeterListService _moneyMeterListService;
    private static MovieService _movieService;
    private static OpenWeatherService _openWeatherService;
    private static ScheduleService _scheduleService;
    private static SecurityService _securityService;
    private static ShoppingListService _shoppingListService;
    private static TemperatureService _temperatureService;
    private static UserService _userService;
    private static WirelessSocketService _wirelessSocketService;
    private static WirelessSwitchService _wirelessSwitchService;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                String stringValue = value.toString();
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else if (preference instanceof EditTextPreference) {
                if (preference.getKey().contentEquals(SettingsController.PREF_USER_NAME)) {
                    _userService.ValidateUserName(value.toString());
                    preference.setSummary(value.toString());
                } else if (preference.getKey().contentEquals(SettingsController.PREF_USER_PASS_PHRASE)) {
                    _userService.ValidateUserPassphrase(value.toString());
                    preference.setSummary(value.toString().replaceAll("[A-Za-z0-9]", "*"));
                } else if (preference.getKey().contentEquals(SettingsController.PREF_OPEN_WEATHER_CITY)) {
                    _openWeatherService.SetCity(value.toString());
                    preference.setSummary(value.toString());

                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_BIRTHDAY_TIMEOUT)) {
                    _birthdayService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_COIN_TIMEOUT)) {
                    _coinService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MAPCONTENT_TIMEOUT)) {
                    _mapContentService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MEDIASERVER_TIMEOUT)) {
                    _mediaServerService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MENU_TIMEOUT)) {
                    _menuService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_METER_DATA_TIMEOUT)) {
                    _meterListService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MONEY_METER_DATA_TIMEOUT)) {
                    _moneyMeterListService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MOVIE_TIMEOUT)) {
                    _movieService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_SCHEDULE_TIMEOUT)) {
                    _scheduleService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_SECURITY_TIMEOUT)) {
                    _securityService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_SHOPPING_TIMEOUT)) {
                    _shoppingListService.SetReloadTimeout(Integer.parseInt((String) value));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_TEMPERATURE_TIMEOUT)) {
                    _temperatureService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_WEATHER_TIMEOUT)) {
                    _openWeatherService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_WIRELESSSOCKET_TIMEOUT)) {
                    _wirelessSocketService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_WIRELESSSWITCH_TIMEOUT)) {
                    _wirelessSwitchService.SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);

                } else if (preference.getKey().contentEquals(SettingsController.PREF_COIN_HOURS_TREND)) {
                    int integerValue = Integer.parseInt((String) value);
                    _coinService.SetCoinHoursTrend(integerValue);
                    preference.setSummary((String) value);
                }

            } else {
                _sharedPrefController.SaveBooleanValue(preference.getKey(), (boolean) value);

                if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE)) {
                    if ((boolean) value) {
                        _birthdayService.SetDisplayNotification(_settingsController.IsBirthdayNotificationEnabled());
                        _coinService.SetDisplayNotification(_settingsController.IsCoinsNotificationEnabled());
                        _openWeatherService.SetDisplayCurrentWeatherNotification(_settingsController.IsCurrentWeatherNotificationEnabled());
                        _openWeatherService.SetDisplayForecastWeatherNotification(_settingsController.IsForecastWeatherNotificationEnabled());
                        _securityService.SetDisplayNotification(_settingsController.IsCameraNotificationEnabled());
                        _temperatureService.SetDisplayNotification(_settingsController.IsTemperatureNotificationEnabled());
                        _wirelessSocketService.SetDisplayNotification(_settingsController.IsSocketNotificationEnabled());
                        _wirelessSwitchService.SetDisplayNotification(_settingsController.IsSwitchNotificationEnabled());
                    } else {
                        _birthdayService.SetDisplayNotification(false);
                        _coinService.SetDisplayNotification(false);
                        _openWeatherService.SetDisplayCurrentWeatherNotification(false);
                        _openWeatherService.SetDisplayForecastWeatherNotification(false);
                        _securityService.SetDisplayNotification(false);
                        _temperatureService.SetDisplayNotification(false);
                        _wirelessSocketService.SetDisplayNotification(false);
                        _wirelessSwitchService.SetDisplayNotification(false);
                    }

                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_BIRTHDAY)) {
                    _birthdayService.SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_COINS)) {
                    _coinService.SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER)) {
                    _openWeatherService.SetDisplayCurrentWeatherNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER)) {
                    _openWeatherService.SetDisplayForecastWeatherNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_SOCKETS)) {
                    _wirelessSocketService.SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contains(WirelessSocket.SETTINGS_HEADER)) {
                    _wirelessSocketService.ShowNotification();
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_SWITCHES)) {
                    _wirelessSwitchService.SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contains(WirelessSwitch.SETTINGS_HEADER)) {
                    _wirelessSwitchService.ShowNotification();
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_CAMERA)) {
                    _securityService.SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_TEMPERATURE)) {
                    _temperatureService.SetDisplayNotification((boolean) value);

                } else if (preference.getKey().contentEquals(SettingsController.PREF_CHANGE_WEATHER_WALLPAPER)) {
                    _openWeatherService.SetChangeWallpaper((boolean) value);

                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_DATA_ENABLED)) {
                    if ((boolean) value) {
                        _birthdayService.SetReloadEnabled(_settingsController.IsReloadBirthdayEnabled());
                        _coinService.SetReloadEnabled(_settingsController.IsReloadCoinEnabled());
                        _mapContentService.SetReloadEnabled(_settingsController.IsReloadMapContentEnabled());
                        _mediaServerService.SetReloadEnabled(_settingsController.IsReloadMediaServerEnabled());
                        _menuService.SetReloadEnabled(_settingsController.IsReloadMenuEnabled());
                        _meterListService.SetReloadEnabled(_settingsController.IsReloadMeterDataEnabled());
                        _moneyMeterListService.SetReloadEnabled(_settingsController.IsReloadMoneyMeterDataEnabled());
                        _movieService.SetReloadEnabled(_settingsController.IsReloadMovieEnabled());
                        _openWeatherService.SetReloadEnabled(_settingsController.IsReloadWeatherEnabled());
                        _scheduleService.SetReloadEnabled(_settingsController.IsReloadScheduleEnabled());
                        _securityService.SetReloadEnabled(_settingsController.IsReloadSecurityEnabled());
                        _shoppingListService.SetReloadEnabled(_settingsController.IsReloadShoppingEnabled());
                        _temperatureService.SetReloadEnabled(_settingsController.IsReloadTemperatureEnabled());
                        _wirelessSocketService.SetReloadEnabled(_settingsController.IsReloadWirelessSocketEnabled());
                        _wirelessSwitchService.SetReloadEnabled(_settingsController.IsReloadWirelessSwitchEnabled());
                    } else {
                        _birthdayService.SetReloadEnabled(false);
                        _coinService.SetReloadEnabled(false);
                        _mapContentService.SetReloadEnabled(false);
                        _mediaServerService.SetReloadEnabled(false);
                        _menuService.SetReloadEnabled(false);
                        _meterListService.SetReloadEnabled(false);
                        _moneyMeterListService.SetReloadEnabled(false);
                        _movieService.SetReloadEnabled(false);
                        _openWeatherService.SetReloadEnabled(false);
                        _scheduleService.SetReloadEnabled(false);
                        _securityService.SetReloadEnabled(false);
                        _shoppingListService.SetReloadEnabled(false);
                        _temperatureService.SetReloadEnabled(false);
                        _wirelessSocketService.SetReloadEnabled(false);
                        _wirelessSwitchService.SetReloadEnabled(false);
                    }
                }
            }

            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(@NonNull Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        if (preference instanceof SwitchPreference) {
            // Trigger the listener immediately with the preference's current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));

        } else {
            // Trigger the listener immediately with the preference's current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    private BroadcastReceiver _validateUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ObjectChangeFinishedContent result = (ObjectChangeFinishedContent) intent.getSerializableExtra(UserService.UserCheckedFinishedBundle);
            if (!result.Success) {
                Snacky.builder()
                        .setActivty(SettingsActivity.this)
                        .setText("Could not validate user!")
                        .setDuration(Snacky.LENGTH_INDEFINITE)
                        .setActionText(android.R.string.ok)
                        .error()
                        .show();
            } else {
                Snacky.builder()
                        .setActivty(SettingsActivity.this)
                        .setText("Successfully validated user!")
                        .setDuration(Snacky.LENGTH_LONG)
                        .setActionText(android.R.string.ok)
                        .success()
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _receiverController = new ReceiverController(this);
        _settingsController = SettingsController.getInstance();
        _sharedPrefController = new SharedPrefController(this);

        //_navigationService = NavigationService.getInstance();

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

        setupActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_validateUserReceiver, new String[]{UserService.UserCheckedFinishedBroadcast});
    }

    @Override
    protected void onPause() {
        super.onPause();
        _receiverController.Dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        _logger.Debug(String.format("onKeyDown: keyCode: %s | event: %s", keyCode, event));

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _navigationService.GoBack(this);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }*/

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(@NonNull String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || UserPreferenceFragment.class.getName().equals(fragmentName)
                || NetworkPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || ReloadPreferenceFragment.class.getName().equals(fragmentName)
                || OpenWeatherPreferenceFragment.class.getName().equals(fragmentName)
                || WirelessSocketPreferenceFragment.class.getName().equals(fragmentName)
                || CoinPreferenceFragment.class.getName().equals(fragmentName)
                || WirelessSwitchPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows user preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class UserPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_user);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_USER_NAME));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_USER_PASS_PHRASE));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows network preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NetworkPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_network);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_SSID));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_IP));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE_SOCKETS));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE_SWITCHES));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE_BIRTHDAY));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE_CAMERA));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_NOTIFICATION_MESSAGE_COINS));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows reload preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ReloadPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_reload_data);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_DATA_ENABLED));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_BIRTHDAY_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_BIRTHDAY_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_COIN_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_COIN_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MAPCONTENT_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MAPCONTENT_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MEDIASERVER_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MEDIASERVER_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MENU_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MENU_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_METER_DATA_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_METER_DATA_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MONEY_METER_DATA_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MONEY_METER_DATA_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MOVIE_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_MOVIE_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_SCHEDULE_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_SCHEDULE_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_SECURITY_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_SECURITY_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_SHOPPING_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_SHOPPING_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_TEMPERATURE_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_TEMPERATURE_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_WEATHER_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_WEATHER_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_WIRELESSSOCKET_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_WIRELESSSOCKET_TIMEOUT));

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_WIRELESSSWITCH_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_WIRELESSSWITCH_TIMEOUT));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows open weather preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class OpenWeatherPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_open_weather);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_OPEN_WEATHER_CITY));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_CHANGE_WEATHER_WALLPAPER));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows open weather preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CoinPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_coins);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_COIN_HOURS_TREND));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows wireless socket preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class WirelessSocketPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_wireless_socket);
            setHasOptionsMenu(true);

            PreferenceScreen preferenceScreen = this.getPreferenceScreen();

            PreferenceCategory preferenceCategory = new PreferenceCategory(preferenceScreen.getContext());
            preferenceCategory.setTitle("Wireless sockets");
            preferenceScreen.addPreference(preferenceCategory);

            for (int index = 0; index < _wirelessSocketService.GetDataList().getSize(); index++) {
                WirelessSocket wirelessSocket = _wirelessSocketService.GetDataList().getValue(index);

                SwitchPreference preference = new SwitchPreference(preferenceScreen.getContext());

                preference.setTitle(wirelessSocket.GetName());
                preference.setKey(wirelessSocket.GetSettingsKey());
                preference.setDefaultValue(false);

                preferenceCategory.addPreference(preference);

                bindPreferenceSummaryToValue(preference);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows wireless switch preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class WirelessSwitchPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_wireless_switch);
            setHasOptionsMenu(true);

            PreferenceScreen preferenceScreen = this.getPreferenceScreen();

            PreferenceCategory preferenceCategory = new PreferenceCategory(preferenceScreen.getContext());
            preferenceCategory.setTitle("Wireless switches");
            preferenceScreen.addPreference(preferenceCategory);

            for (int index = 0; index < _wirelessSwitchService.GetDataList().getSize(); index++) {
                WirelessSwitch wirelessSwitch = _wirelessSwitchService.GetDataList().getValue(index);

                SwitchPreference preference = new SwitchPreference(preferenceScreen.getContext());

                preference.setTitle(wirelessSwitch.GetName());
                preference.setKey(wirelessSwitch.GetSettingsKey());
                preference.setDefaultValue(false);

                preferenceCategory.addPreference(preference);

                bindPreferenceSummaryToValue(preference);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
