package guepardoapps.lucahome.views;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import guepardoapps.lucahome.basic.utils.Logger;
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
import guepardoapps.lucahome.common.service.PositioningService;
import guepardoapps.lucahome.common.service.PuckJsListService;
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
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private ReceiverController _receiverController;
    private static SharedPrefController _sharedPrefController;

    /**
     * Binder for PositioningService
     */
    private static PositioningService _positioningServiceBinder;

    /**
     * ServiceConnection for PositioningServiceBinder
     */
    private ServiceConnection _positioningServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.getInstance().Information(TAG, "onServiceConnected PositioningService");
            _positioningServiceBinder = ((PositioningService.PositioningServiceBinder) binder).getService();
            _positioningServiceBinder.SetActiveActivityContext(SettingsActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            Logger.getInstance().Information(TAG, "onServiceDisconnected PositioningService");
            _positioningServiceBinder = null;
        }
    };

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
                    UserService.getInstance().ValidateUserName(value.toString());
                    preference.setSummary(value.toString());
                } else if (preference.getKey().contentEquals(SettingsController.PREF_USER_PASS_PHRASE)) {
                    UserService.getInstance().ValidateUserPassphrase(value.toString());
                    preference.setSummary(value.toString().replaceAll("[A-Za-z0-9]", "*"));
                } else if (preference.getKey().contentEquals(SettingsController.PREF_OPEN_WEATHER_CITY)) {
                    OpenWeatherService.getInstance().SetCity(value.toString());
                    preference.setSummary(value.toString());

                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_BIRTHDAY_TIMEOUT)) {
                    BirthdayService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_COIN_TIMEOUT)) {
                    CoinService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MAPCONTENT_TIMEOUT)) {
                    MapContentService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MEDIASERVER_TIMEOUT)) {
                    MediaServerService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MENU_TIMEOUT)) {
                    MenuService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_METER_DATA_TIMEOUT)) {
                    MeterListService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MONEY_METER_DATA_TIMEOUT)) {
                    MoneyMeterListService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_MOVIE_TIMEOUT)) {
                    MovieService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_PUCKJS_TIMEOUT)) {
                    PuckJsListService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_SCHEDULE_TIMEOUT)) {
                    ScheduleService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_SECURITY_TIMEOUT)) {
                    SecurityService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_SHOPPING_TIMEOUT)) {
                    ShoppingListService.getInstance().SetReloadTimeout(Integer.parseInt((String) value));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_TEMPERATURE_TIMEOUT)) {
                    TemperatureService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_WEATHER_TIMEOUT)) {
                    OpenWeatherService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_WIRELESSSOCKET_TIMEOUT)) {
                    WirelessSocketService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_WIRELESSSWITCH_TIMEOUT)) {
                    WirelessSwitchService.getInstance().SetReloadTimeout((Integer.parseInt((String) value)));
                    preference.setSummary((String) value);

                } else if (preference.getKey().contentEquals(SettingsController.PREF_COIN_HOURS_TREND)) {
                    CoinService.getInstance().SetCoinHoursTrend(Integer.parseInt((String) value));
                    preference.setSummary((String) value);

                } else if (preference.getKey().contentEquals(SettingsController.PREF_BEACONS_TIME_BETWEEN_SCANS_SEC)) {
                    try {
                        _positioningServiceBinder.SetBetweenScanPeriod(Long.parseLong((String) value));
                        preference.setSummary((String) value);
                    } catch (Exception exception) {
                        Logger.getInstance().Error(TAG, exception.toString());
                    }
                } else if (preference.getKey().contentEquals(SettingsController.PREF_BEACONS_TIME_SCANS_MSEC)) {
                    try {
                        _positioningServiceBinder.SetScanPeriod(Long.parseLong((String) value));
                        preference.setSummary((String) value);
                    } catch (Exception exception) {
                        Logger.getInstance().Error(TAG, exception.toString());
                    }
                }

            } else {
                _sharedPrefController.SaveBooleanValue(preference.getKey(), (boolean) value);

                if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE)) {
                    if ((boolean) value) {
                        BirthdayService.getInstance().SetDisplayNotification(SettingsController.getInstance().IsBirthdayNotificationEnabled());
                        CoinService.getInstance().SetDisplayNotification(SettingsController.getInstance().IsCoinsNotificationEnabled());
                        OpenWeatherService.getInstance().SetDisplayCurrentWeatherNotification(SettingsController.getInstance().IsCurrentWeatherNotificationEnabled());
                        OpenWeatherService.getInstance().SetDisplayForecastWeatherNotification(SettingsController.getInstance().IsForecastWeatherNotificationEnabled());
                        SecurityService.getInstance().SetDisplayNotification(SettingsController.getInstance().IsCameraNotificationEnabled());
                        TemperatureService.getInstance().SetDisplayNotification(SettingsController.getInstance().IsTemperatureNotificationEnabled());
                        WirelessSocketService.getInstance().SetDisplayNotification(SettingsController.getInstance().IsSocketNotificationEnabled());
                        WirelessSwitchService.getInstance().SetDisplayNotification(SettingsController.getInstance().IsSwitchNotificationEnabled());
                    } else {
                        BirthdayService.getInstance().SetDisplayNotification(false);
                        CoinService.getInstance().SetDisplayNotification(false);
                        OpenWeatherService.getInstance().SetDisplayCurrentWeatherNotification(false);
                        OpenWeatherService.getInstance().SetDisplayForecastWeatherNotification(false);
                        SecurityService.getInstance().SetDisplayNotification(false);
                        TemperatureService.getInstance().SetDisplayNotification(false);
                        WirelessSocketService.getInstance().SetDisplayNotification(false);
                        WirelessSwitchService.getInstance().SetDisplayNotification(false);
                    }

                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_BIRTHDAY)) {
                    BirthdayService.getInstance().SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_COINS)) {
                    CoinService.getInstance().SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_CURRENT_WEATHER)) {
                    OpenWeatherService.getInstance().SetDisplayCurrentWeatherNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_FORECAST_WEATHER)) {
                    OpenWeatherService.getInstance().SetDisplayForecastWeatherNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_SOCKETS)) {
                    WirelessSocketService.getInstance().SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contains(WirelessSocket.SETTINGS_HEADER)) {
                    WirelessSocketService.getInstance().ShowNotification();
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_SWITCHES)) {
                    WirelessSwitchService.getInstance().SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contains(WirelessSwitch.SETTINGS_HEADER)) {
                    WirelessSwitchService.getInstance().ShowNotification();
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_CAMERA)) {
                    SecurityService.getInstance().SetDisplayNotification((boolean) value);
                } else if (preference.getKey().contentEquals(SettingsController.PREF_NOTIFICATION_MESSAGE_TEMPERATURE)) {
                    TemperatureService.getInstance().SetDisplayNotification((boolean) value);

                } else if (preference.getKey().contentEquals(SettingsController.PREF_CHANGE_WEATHER_WALLPAPER)) {
                    OpenWeatherService.getInstance().SetChangeWallpaper((boolean) value);

                } else if (preference.getKey().contentEquals(SettingsController.PREF_RELOAD_DATA_ENABLED)) {
                    if ((boolean) value) {
                        BirthdayService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadBirthdayEnabled());
                        CoinService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadCoinEnabled());
                        MapContentService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadMapContentEnabled());
                        MediaServerService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadMediaServerEnabled());
                        MenuService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadMenuEnabled());
                        MeterListService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadMeterDataEnabled());
                        MoneyMeterListService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadMoneyMeterDataEnabled());
                        MovieService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadMovieEnabled());
                        OpenWeatherService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadWeatherEnabled());
                        PuckJsListService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadPuckJsEnabled());
                        ScheduleService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadScheduleEnabled());
                        SecurityService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadSecurityEnabled());
                        ShoppingListService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadShoppingEnabled());
                        TemperatureService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadTemperatureEnabled());
                        WirelessSocketService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadWirelessSocketEnabled());
                        WirelessSwitchService.getInstance().SetReloadEnabled(SettingsController.getInstance().IsReloadWirelessSwitchEnabled());
                    } else {
                        BirthdayService.getInstance().SetReloadEnabled(false);
                        CoinService.getInstance().SetReloadEnabled(false);
                        MapContentService.getInstance().SetReloadEnabled(false);
                        MediaServerService.getInstance().SetReloadEnabled(false);
                        MenuService.getInstance().SetReloadEnabled(false);
                        MeterListService.getInstance().SetReloadEnabled(false);
                        MoneyMeterListService.getInstance().SetReloadEnabled(false);
                        MovieService.getInstance().SetReloadEnabled(false);
                        OpenWeatherService.getInstance().SetReloadEnabled(false);
                        PuckJsListService.getInstance().SetReloadEnabled(false);
                        ScheduleService.getInstance().SetReloadEnabled(false);
                        SecurityService.getInstance().SetReloadEnabled(false);
                        ShoppingListService.getInstance().SetReloadEnabled(false);
                        TemperatureService.getInstance().SetReloadEnabled(false);
                        WirelessSocketService.getInstance().SetReloadEnabled(false);
                        WirelessSwitchService.getInstance().SetReloadEnabled(false);
                    }

                } else if (preference.getKey().contentEquals(SettingsController.PREF_HANDLE_BLUETOOTH_AUTOMATICALLY)) {
                    try {
                        _positioningServiceBinder.SetScanEnabled((boolean) value);
                    } catch (Exception exception) {
                        Logger.getInstance().Error(TAG, exception.toString());
                    }
                } else if (preference.getKey().contentEquals(SettingsController.PREF_BEACONS_SCAN_ENABLED)) {
                    try {
                        _positioningServiceBinder.SetScanEnabled((boolean) value);
                    } catch (Exception exception) {
                        Logger.getInstance().Error(TAG, exception.toString());
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
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
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
        _sharedPrefController = new SharedPrefController(this);

        setupActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _receiverController.RegisterReceiver(_validateUserReceiver, new String[]{UserService.UserCheckedFinishedBroadcast});

        if (_positioningServiceBinder == null) {
            bindService(new Intent(this, PositioningService.class), _positioningServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        _receiverController.Dispose();
        unbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _receiverController.Dispose();
        unbindService();
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
                || WirelessSwitchPreferenceFragment.class.getName().equals(fragmentName)
                || PositionPreferenceFragment.class.getName().equals(fragmentName);
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

            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_PUCKJS_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_RELOAD_PUCKJS_TIMEOUT));

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

            for (int index = 0; index < WirelessSocketService.getInstance().GetDataList().getSize(); index++) {
                WirelessSocket wirelessSocket = WirelessSocketService.getInstance().GetDataList().getValue(index);

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

            for (int index = 0; index < WirelessSwitchService.getInstance().GetDataList().getSize(); index++) {
                WirelessSwitch wirelessSwitch = WirelessSwitchService.getInstance().GetDataList().getValue(index);

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

    /**
     * This fragment shows reload preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PositionPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_positioning);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_HANDLE_BLUETOOTH_AUTOMATICALLY));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_BEACONS_SCAN_ENABLED));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_BEACONS_TIME_BETWEEN_SCANS_SEC));
            bindPreferenceSummaryToValue(findPreference(SettingsController.PREF_BEACONS_TIME_SCANS_MSEC));
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

    private void unbindService() {
        Logger.getInstance().Debug(TAG, "unbindServices");

        if (_positioningServiceBinder != null) {
            try {
                unbindService(_positioningServiceConnection);
            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.getMessage());
            } finally {
                _positioningServiceBinder = null;
            }
        }
    }
}
