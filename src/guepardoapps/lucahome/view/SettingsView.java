package guepardoapps.lucahome.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Color;
import guepardoapps.lucahome.common.constants.IDs;
import guepardoapps.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.NavigationService;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;
import guepardoapps.toolset.controller.SharedPrefController;
import guepardoapps.toolset.openweather.common.OpenWeatherConstants;

public class SettingsView extends Activity {

	private static final String TAG = SettingsView.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<WirelessSocketDto> _socketList;
	private boolean _isInitialized;

	private LinearLayout _socketLayout;

	private Context _context;

	private BroadcastController _broadcastController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;
	private ServiceController _serviceController;
	private SharedPrefController _sharedPrefController;

	private BroadcastReceiver _updateSocketListCheckBoxViewReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateSocketListCheckBoxViewReceiver");
			_socketList = (SerializableList<WirelessSocketDto>) intent.getSerializableExtra(Bundles.SOCKET_LIST);
			if (_socketList != null) {
				initializeSocketNotificationCheckboxes();
			} else {
				_logger.Warn("SocketList is null!");
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_settings);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_socketLayout = (LinearLayout) findViewById(R.id.notificationSocketLayout);

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_navigationService = new NavigationService(_context);
		_receiverController = new ReceiverController(_context);
		_serviceController = new ServiceController(_context);
		_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);

		initializeSwitches();
		initializeAppCheckboxes();
		initializeSocketNotificationCheckboxes();
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
				_receiverController.RegisterReceiver(_updateSocketListCheckBoxViewReceiver,
						new String[] { Broadcasts.UPDATE_SOCKET });
				_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
						new String[] { Bundles.MAIN_SERVICE_ACTION }, new Object[] { MainServiceAction.GET_SOCKETS });
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (_logger != null) {
			_logger.Debug("onPause");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (_logger != null) {
			_logger.Debug("onDestroy");
		}

		_receiverController.UnregisterReceiver(_updateSocketListCheckBoxViewReceiver);
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
		displayNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_SOCKET_NOTIFICATION, isChecked);
				if (isChecked) {
					_socketLayout.setVisibility(View.VISIBLE);
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
							new String[] { Bundles.MAIN_SERVICE_ACTION },
							new Object[] { MainServiceAction.GET_SOCKETS });
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
							new String[] { Bundles.MAIN_SERVICE_ACTION },
							new Object[] { MainServiceAction.SHOW_NOTIFICATION_SOCKET });
				} else {
					_socketLayout.setVisibility(View.GONE);
					_serviceController.CloseNotification(IDs.NOTIFICATION_WEAR);
				}
			}
		});

		Switch weatherNotification = (Switch) findViewById(R.id.switch_display_weather_notification);
		weatherNotification.setChecked(_sharedPrefController
				.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION));
		weatherNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_WEATHER_NOTIFICATION, isChecked);
				if (isChecked) {
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
							new String[] { Bundles.MAIN_SERVICE_ACTION },
							new Object[] { MainServiceAction.SHOW_NOTIFICATION_WEATHER });
				} else {
					_serviceController.CloseNotification(OpenWeatherConstants.FORECAST_NOTIFICATION_ID);
				}
			}
		});

		Switch temperatureNotification = (Switch) findViewById(R.id.switch_display_temperature_notification);
		temperatureNotification.setChecked(_sharedPrefController
				.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION));
		temperatureNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_sharedPrefController.SaveBooleanValue(SharedPrefConstants.DISPLAY_TEMPERATURE_NOTIFICATION, isChecked);
				if (isChecked) {
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
							new String[] { Bundles.MAIN_SERVICE_ACTION },
							new Object[] { MainServiceAction.SHOW_NOTIFICATION_TEMPERATURE });
				} else {
					_serviceController.CloseNotification(IDs.NOTIFICATION_TEMPERATURE);
				}
			}
		});
	}

	private void initializeAppCheckboxes() {
		CheckBox audioStart = (CheckBox) findViewById(R.id.startAudioAppCheckbox);
		audioStart.setChecked(
				_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.START_AUDIO_APP));
		audioStart.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_AUDIO_APP, isChecked);
			}
		});

		CheckBox osmcStart = (CheckBox) findViewById(R.id.startOsmcAppCheckbox);
		osmcStart.setChecked(
				_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.START_OSMC_APP));
		osmcStart.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_sharedPrefController.SaveBooleanValue(SharedPrefConstants.START_OSMC_APP, isChecked);
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
			boolean notificationVisibility = _sharedPrefController.LoadBooleanValueFromSharedPreferences(key);

			CheckBox socketCheckbox = new CheckBox(getApplicationContext());
			socketCheckbox.setText(socket.GetName());
			socketCheckbox.setChecked(notificationVisibility);
			socketCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					_sharedPrefController.SaveBooleanValue(key, isChecked);
					_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
							new String[] { Bundles.MAIN_SERVICE_ACTION },
							new Object[] { MainServiceAction.SHOW_NOTIFICATION_SOCKET });
				}
			});

			_socketLayout.addView(socketCheckbox);
		}
	}
}
