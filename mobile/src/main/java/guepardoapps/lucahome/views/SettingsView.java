package guepardoapps.lucahome.views;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.ActionDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.DatabaseController;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.customadapter.StoredActionListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.openweather.common.OWIds;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.SharedPrefController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.services.MainService;

public class SettingsView extends Activity {

    private static final String TAG = SettingsView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<ActionDto> _storedActions;
    private SerializableList<WirelessSocketDto> _socketList;
    private boolean _isInitialized;

    private Button _deleteActionDatabase;
    private LinearLayout _socketLayout;
    private ListView _storedActionsListView;

    private ListAdapter _storedActionListAdapter;

    private Context _context;

    private BroadcastController _broadcastController;
    private DatabaseController _databaseController;
    private LucaNotificationController _notificationController;
    private NavigationService _navigationService;
    private NetworkController _networkController;
    private ReceiverController _receiverController;
    private SharedPrefController _sharedPrefController;

    private BroadcastReceiver _updateSocketListCheckBoxViewReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateSocketListCheckBoxViewReceiver onReceive");
            _socketList = (SerializableList<WirelessSocketDto>) intent.getSerializableExtra(Bundles.SOCKET_LIST);
            if (_socketList != null) {
                initializeSocketNotificationCheckboxes();
            } else {
                _logger.Warn("SocketList is null!");
            }
        }
    };

    private BroadcastReceiver _reloadStoredActionsFromDB = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_reloadStoredActionsFromDB onReceive");
            _storedActions = _databaseController.GetActions();

            if (_storedActions.getSize() < 1) {
                _deleteActionDatabase.setEnabled(false);
                _deleteActionDatabase.setTextColor(0xFF545454);
                _storedActionsListView.setVisibility(View.GONE);
            } else {
                _deleteActionDatabase.setEnabled(true);
                _deleteActionDatabase.setTextColor(0xFFFFFFFF);
                _storedActionsListView.setVisibility(View.VISIBLE);
            }

            _storedActionListAdapter = new StoredActionListAdapter(
                    _context,
                    _storedActions,
                    _broadcastController,
                    _databaseController);
            _storedActionsListView.setAdapter(_storedActionListAdapter);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_settings);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));
        } else {
            _logger.Error("ActionBar is null!");
        }

        _socketLayout = (LinearLayout) findViewById(R.id.notificationSocketLayout);

        _context = this;

        _broadcastController = new BroadcastController(_context);
        _databaseController = DatabaseController.getInstance();
        _navigationService = new NavigationService(_context);
        _networkController = new NetworkController(_context);
        _notificationController = new LucaNotificationController(_context);
        _receiverController = new ReceiverController(_context);
        _sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

        initializeSwitches();
        initializeAppCheckboxes();
        initializeBeaconCheckbox();
        initializeSocketNotificationCheckboxes();
        initializeStoredActions();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_logger != null) {
            _logger.Debug("onResume");
        }

        if (!_isInitialized) {
            if (_receiverController != null) {
                _isInitialized = true;
                _receiverController.RegisterReceiver(_updateSocketListCheckBoxViewReceiver, new String[]{Broadcasts.UPDATE_SOCKET_LIST});
                _receiverController.RegisterReceiver(_reloadStoredActionsFromDB, new String[]{Broadcasts.RELOAD_SHOPPING_LIST_FROM_DB});

                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.HOME_AUTOMATION_COMMAND,
                        new String[]{Bundles.HOME_AUTOMATION_ACTION},
                        new Object[]{HomeAutomationAction.GET_SOCKET_LIST});
            }
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _navigationService.NavigateTo(HomeView.class, true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initializeSwitches() {
        Switch displayNotification = (Switch) findViewById(R.id.switch_display_socket_notification);
        displayNotification.setChecked(_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION));
        displayNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION, isChecked);
                if (isChecked) {
                    _socketLayout.setVisibility(View.VISIBLE);
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{HomeAutomationAction.GET_SOCKET_LIST});
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{HomeAutomationAction.SHOW_NOTIFICATION_SOCKET});
                } else {
                    _socketLayout.setVisibility(View.GONE);
                    _notificationController.CloseNotification(IDs.NOTIFICATION_WEAR);
                }
            }
        });

        Switch weatherNotification = (Switch) findViewById(R.id.switch_display_weather_notification);
        weatherNotification.setChecked(_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION));
        weatherNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION, isChecked);
                if (isChecked) {
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{HomeAutomationAction.SHOW_NOTIFICATION_WEATHER});
                } else {
                    _notificationController.CloseNotification(OWIds.FORECAST_NOTIFICATION_ID);
                }
            }
        });

        Switch temperatureNotification = (Switch) findViewById(R.id.switch_display_temperature_notification);
        temperatureNotification.setChecked(_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION));
        temperatureNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION, isChecked);
                if (isChecked) {
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{HomeAutomationAction.SHOW_NOTIFICATION_TEMPERATURE});
                } else {
                    _notificationController.CloseNotification(IDs.NOTIFICATION_TEMPERATURE);
                }
            }
        });

        Switch birthdayNotification = (Switch) findViewById(R.id.switch_display_birthday_notification);
        birthdayNotification.setChecked(_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_BIRTHDAY_NOTIFICATION));
        birthdayNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_BIRTHDAY_NOTIFICATION, isChecked);
                if (isChecked) {
                    _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_BIRTHDAY);
                } else {
                    _notificationController.CloseNotification(IDs.NOTIFICATION_BIRTHDAY);
                }
            }
        });

        Switch sleepNotification = (Switch) findViewById(R.id.switch_display_sleep_notification);
        sleepNotification.setChecked(_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION));
        sleepNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION, isChecked);

                if (isChecked) {
                    Calendar calendar = Calendar.getInstance();

                    if ((int) calendar.get(Calendar.HOUR_OF_DAY) == MainService.SLEEP_NOTIFICATION_HOUR) {
                        if (calendar.get(Calendar.MINUTE) > MainService.SLEEP_NOTIFICATION_MINUTE - MainService.SLEEP_NOTIFICATION_TIME_SPAN_MINUTE
                                && calendar.get(Calendar.MINUTE) < MainService.SLEEP_NOTIFICATION_MINUTE + MainService.SLEEP_NOTIFICATION_TIME_SPAN_MINUTE) {
                            _logger.Debug("We are in the timeSpan!");

                            if (_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
                                _logger.Debug("Showing notification go to sleep!");
                                _notificationController.CreateSleepHeatingNotification();
                            } else {
                                _logger.Debug("Saving flag to display notification later!");
                                _sharedPrefController
                                        .SaveBooleanValue(SharedPrefConstants.DISPLAY_SLEEP_NOTIFICATION_ACTIVE, true);
                            }
                        } else {
                            _logger.Debug("We are NOT in the timeSpan!");
                        }
                    }
                } else {
                    _notificationController.CloseNotification(IDs.NOTIFICATION_SLEEP);
                }
            }
        });

        Switch cameraNotification = (Switch) findViewById(R.id.switch_display_camera_notification);
        cameraNotification.setChecked(_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_CAMERA_NOTIFICATION));
        cameraNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_CAMERA_NOTIFICATION, isChecked);
                if (isChecked) {
                    // TODO check if camera is active
                    _logger.Warn("Implement check for active camera in settings!");
                } else {
                    _notificationController.CloseNotification(IDs.NOTIFICATION_CAMERA);
                }
            }
        });

        // TODO show after above TODO is done!
        cameraNotification.setVisibility(View.GONE);
    }

    private void initializeAppCheckboxes() {
        CheckBox audioStart = (CheckBox) findViewById(R.id.startAudioAppCheckbox);
        audioStart.setChecked(_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.START_AUDIO_APP));
        audioStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_AUDIO_APP, isChecked);
            }
        });

        CheckBox osmcStart = (CheckBox) findViewById(R.id.startOsmcAppCheckbox);
        osmcStart.setChecked(_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.START_OSMC_APP));
        osmcStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_OSMC_APP, isChecked);
            }
        });
    }

    private void initializeBeaconCheckbox() {
        CheckBox beaconActivate = (CheckBox) findViewById(R.id.startBeaconsCheckbox);
        beaconActivate.setChecked(_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.USE_BEACONS));
        beaconActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _sharedPrefController.SaveBooleanValue(SharedPrefConstants.USE_BEACONS, isChecked);
                if (isChecked) {
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{HomeAutomationAction.BEACON_SCANNING_START});
                } else {
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{HomeAutomationAction.BEACON_SCANNING_STOP});
                }
            }
        });
    }

    private void initializeSocketNotificationCheckboxes() {
        if (!_sharedPrefController
                .LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION)) {
            _socketLayout.setVisibility(View.GONE);
            return;
        }

        if (_socketList == null) {
            _logger.Warn("_wirelessSocketList is null!");
            return;
        }

        _socketLayout.removeAllViews();

        for (int index = 0; index < _socketList.getSize(); index++) {
            WirelessSocketDto socket = _socketList.getValue(index);
            final String key = socket.GetNotificationVisibilitySharedPrefKey();
            _logger.Info(String.format("Key of socket %s is %s", socket.GetName(), key));
            boolean notificationVisibility = _sharedPrefController.LoadBooleanValueFromSharedPreferences(key);

            CheckBox socketCheckbox = new CheckBox(getApplicationContext());
            socketCheckbox.setText(socket.GetName());
            socketCheckbox.setTextColor(0xffffffff);
            socketCheckbox.setChecked(notificationVisibility);
            socketCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    _logger.Info(String.format("Saving key %s with value %s", key, isChecked));
                    _sharedPrefController.SaveBooleanValue(key, isChecked);
                    _broadcastController.SendSerializableArrayBroadcast(
                            Broadcasts.HOME_AUTOMATION_COMMAND,
                            new String[]{Bundles.HOME_AUTOMATION_ACTION},
                            new Object[]{HomeAutomationAction.SHOW_NOTIFICATION_SOCKET});
                }
            });

            _socketLayout.addView(socketCheckbox);
        }
    }

    private void initializeStoredActions() {
        _deleteActionDatabase = (Button) findViewById(R.id.btnDeleteDatabaseEntries);
        _deleteActionDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _databaseController.ClearDatabaseActions();
                Toasty.info(_context, "Cleared actions!", Toast.LENGTH_LONG).show();
            }
        });

        _storedActions = _databaseController.GetActions();

        _storedActionsListView = (ListView) findViewById(R.id.storedActionListView);
        _storedActionListAdapter = new StoredActionListAdapter(
                _context,
                _storedActions,
                _broadcastController,
                _databaseController);
        _storedActionsListView.setAdapter(_storedActionListAdapter);

        if (_storedActions.getSize() < 1) {
            _deleteActionDatabase.setEnabled(false);
            _deleteActionDatabase.setTextColor(0xFF545454);
            _storedActionsListView.setVisibility(View.GONE);
        } else {
            _deleteActionDatabase.setEnabled(true);
            _deleteActionDatabase.setTextColor(0xFFFFFFFF);
            _storedActionsListView.setVisibility(View.VISIBLE);
        }
    }
}
