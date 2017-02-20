package guepardoapps.lucahome.view;

import java.sql.Time;
import java.util.Calendar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;

import guepardoapps.lucahomelibrary.common.classes.*;
import guepardoapps.lucahomelibrary.common.constants.Color;
import guepardoapps.lucahomelibrary.common.controller.*;
import guepardoapps.lucahomelibrary.common.dto.*;
import guepardoapps.lucahomelibrary.common.enums.MainServiceAction;
import guepardoapps.lucahomelibrary.common.enums.TemperatureType;
import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;
import guepardoapps.lucahomelibrary.services.helper.NavigationService;
import guepardoapps.lucahomelibrary.view.customadapter.*;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

import guepardoapps.toolset.openweather.model.WeatherModel;

public class SensorTemperatureView extends Activity implements SensorEventListener {

	private static final String TAG = SensorTemperatureView.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
	private WeatherModel _currentWeather;
	private SerializableList<TemperatureDto> _temperatureList;

	private ProgressBar _progressBar;
	private ListView _listView;
	private Button _buttonAdd;

	private ListAdapter _listAdapter;

	private Context _context;

	private BroadcastController _broadcastController;
	private LucaNotificationController _notificationController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;

	private SensorManager _sensorManager;
	private Sensor _sensor;
	private boolean _hasTemperatureSensor;
	private Handler _temperatureTimeoutHandler = new Handler();
	private int _temperatureTimeout = 5000;

	private Runnable _getDataRunnable = new Runnable() {
		public void run() {
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
					new String[] { Bundles.MAIN_SERVICE_ACTION }, new Object[] { MainServiceAction.GET_TEMPERATURE });
		}
	};

	private Runnable _temperatureTimeoutCheck = new Runnable() {
		public void run() {
			checkSensorAvailability();
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");

			_temperatureList = (SerializableList<TemperatureDto>) intent.getSerializableExtra(Bundles.TEMPERATURE_LIST);

			if (_temperatureList != null) {
				_listAdapter = new TemperatureListAdapter(_context, _temperatureList);
				_listView.setAdapter(_listAdapter);

				_progressBar.setVisibility(View.GONE);
				_listView.setVisibility(View.VISIBLE);
			}
		}
	};

	private BroadcastReceiver _temperatureReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_temperatureReceiver onReceive");

			int id = intent.getIntExtra(Bundles.TEMPERATURE_ID, -1);
			TemperatureDto updatedEntry;
			TemperatureType temperatureType = (TemperatureType) intent.getSerializableExtra(Bundles.TEMPERATURE_TYPE);

			if (temperatureType != null) {
				switch (temperatureType) {
				case RASPBERRY:
					updatedEntry = (TemperatureDto) intent.getSerializableExtra(Bundles.TEMPERATURE_SINGLE);
					if (updatedEntry != null) {
						_temperatureList.setValue(id, updatedEntry);
					}
					break;
				case CITY:
					_currentWeather = (WeatherModel) intent.getSerializableExtra(Bundles.TEMPERATURE_SINGLE);
					if (_currentWeather != null) {
						updatedEntry = new TemperatureDto(_currentWeather.GetTemperature(), _currentWeather.GetCity(),
								_currentWeather.GetLastUpdate(), "n.a.", TemperatureType.CITY, "n.a.");
						if (updatedEntry != null) {
							_temperatureList.setValue(id, updatedEntry);
						}
					}
					break;
				default:
					_logger.Warn(temperatureType.toString() + " is not supported!");
					break;
				}
			}

			if (_temperatureList != null && _currentWeather != null) {
				_notificationController.CreateTemperatureNotification(SensorTemperatureView.class, _temperatureList,
						_currentWeather);
			}

			_listAdapter = new TemperatureListAdapter(_context, _temperatureList);
			_listView.setAdapter(_listAdapter);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_skeleton_list);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_navigationService = new NavigationService(_context);
		_notificationController = new LucaNotificationController(_context);
		_receiverController = new ReceiverController(_context);

		_listView = (ListView) findViewById(R.id.listView);
		_progressBar = (ProgressBar) findViewById(R.id.progressBarListView);

		_buttonAdd = (Button) findViewById(R.id.buttonAddListView);
		_buttonAdd.setVisibility(View.GONE);

		initializeSensor();
	}

	@Override
	public void onResume() {
		super.onResume();
		_logger.Debug("onResume");
		if (!_isInitialized) {
			if (_receiverController != null && _broadcastController != null) {
				_isInitialized = true;
				_receiverController.RegisterReceiver(_updateReceiver, new String[] { Broadcasts.UPDATE_TEMPERATURE });
				_hasTemperatureSensor = checkSensorAvailability();
				_receiverController.RegisterReceiver(_temperatureReceiver,
						new String[] { Broadcasts.UPDATE_TEMPERATURE });
				_getDataRunnable.run();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		_logger.Debug("onPause");
		if (_hasTemperatureSensor) {
			_sensorManager.unregisterListener(this);
			stopTemperatureTimeout();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateReceiver);
		if (_hasTemperatureSensor) {
			_sensorManager.unregisterListener(this);
			stopTemperatureTimeout();
			_receiverController.UnregisterReceiver(_temperatureReceiver);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
			double temperature = event.values[0];
			temperature = Math.floor(temperature * 100) / 100;

			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			@SuppressWarnings("deprecation")
			Time time = new Time(hour, minute, second);

			TemperatureDto newEntry = new TemperatureDto(temperature, "Ambient temperature", time, "n.a.",
					TemperatureType.SMARTPHONE_SENSOR, "n.a.");

			if (_temperatureList != null) {
				for (int index = 0; index < _temperatureList.getSize(); index++) {
					if (_temperatureList.getValue(index).GetTemperatureType() == TemperatureType.SMARTPHONE_SENSOR) {
						_temperatureList.removeValue(_temperatureList.getValue(index));
					}
				}

				_temperatureList.addValue(newEntry);
				_listAdapter = new TemperatureListAdapter(_context, _temperatureList);
				_listView.setAdapter(_listAdapter);
			}

			_sensorManager.unregisterListener(this);
			startTemperatureTimeout();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int arg1) {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			_navigationService.NavigateTo(HomeView.class, true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initializeSensor() {
		_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		_sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		_hasTemperatureSensor = checkSensorAvailability();
	}

	private boolean checkSensorAvailability() {
		if (_sensor != null) {
			_logger.Debug("Sensor is available");
			_sensorManager.registerListener(this, _sensor, SensorManager.SENSOR_DELAY_NORMAL);
			return true;
		} else {
			Toasty.warning(_context, "Sensor is not available", Toast.LENGTH_SHORT).show();
			_logger.Debug("Sensor is not available");
			return false;
		}
	}

	private void startTemperatureTimeout() {
		_logger.Debug("Starting temperatureTimeoutController...");
		_temperatureTimeoutHandler.postDelayed(_temperatureTimeoutCheck, _temperatureTimeout);
	}

	private void stopTemperatureTimeout() {
		_temperatureTimeoutHandler.removeCallbacks(_temperatureTimeoutCheck);
	}
}
