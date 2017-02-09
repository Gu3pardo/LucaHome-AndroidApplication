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

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Color;
import guepardoapps.lucahome.common.dto.sensor.HumidityDto;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.NavigationService;
import guepardoapps.lucahome.view.customadapter.*;
import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class SensorHumidityView extends Activity implements SensorEventListener {

	private static final String TAG = SensorHumidityView.class.getName();
	private LucaHomeLogger _logger;

	private static final int SENSOR_TYPE = Sensor.TYPE_RELATIVE_HUMIDITY;

	private boolean _isInitialized;
	private SerializableList<HumidityDto> _humidityList;

	private ProgressBar _progressBar;
	private ListView _listView;
	private Button _buttonAdd;

	private ListAdapter _listAdapter;

	private Context _context;

	private BroadcastController _broadcastController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;

	private SensorManager _sensorManager;
	private Sensor _sensor;
	private boolean _hasHumiditySensor;
	private Handler _humidityTimeoutHandler = new Handler();
	private int _humidityTimeout = 5000;

	private Runnable _getDataRunnable = new Runnable() {
		public void run() {
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
					new String[] { Bundles.MAIN_SERVICE_ACTION }, new Object[] { MainServiceAction.GET_HUMIDITY });
		}
	};

	private Runnable _humidityTimeoutCheck = new Runnable() {
		public void run() {
			checkSensorAvailability();
		}
	};

	private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_updateReceiver onReceive");

			_humidityList = (SerializableList<HumidityDto>) intent.getSerializableExtra(Bundles.HUMIDITY_LIST);

			if (_humidityList != null) {
				_listAdapter = new HumidityListAdapter(_context, _humidityList);
				_listView.setAdapter(_listAdapter);

				_progressBar.setVisibility(View.GONE);
				_listView.setVisibility(View.VISIBLE);
			}
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
				_receiverController.RegisterReceiver(_updateReceiver, new String[] { Broadcasts.UPDATE_HUMIDITY });
				_hasHumiditySensor = checkSensorAvailability();
				_getDataRunnable.run();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		_logger.Debug("onPause");
		if (_hasHumiditySensor) {
			_sensorManager.unregisterListener(this);
			stopAirPressureTimeout();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateReceiver);
		if (_hasHumiditySensor) {
			_sensorManager.unregisterListener(this);
			stopAirPressureTimeout();
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == SENSOR_TYPE) {
			double humidity = event.values[0];

			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			@SuppressWarnings("deprecation")
			Time time = new Time(hour, minute, second);

			HumidityDto newEntry = new HumidityDto(humidity, "Ambient humidity", time);

			_humidityList = new SerializableList<HumidityDto>();
			_humidityList.addValue(newEntry);

			_listAdapter = new HumidityListAdapter(_context, _humidityList);
			_listView.setAdapter(_listAdapter);

			_progressBar.setVisibility(View.GONE);
			_listView.setVisibility(View.VISIBLE);

			_sensorManager.unregisterListener(this);
			startAirPressureTimeout();
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
		_sensor = _sensorManager.getDefaultSensor(SENSOR_TYPE);
		_hasHumiditySensor = checkSensorAvailability();
	}

	private boolean checkSensorAvailability() {
		if (_sensor != null) {
			_logger.Debug("Sensor is available");
			_sensorManager.registerListener(this, _sensor, SensorManager.SENSOR_DELAY_NORMAL);
			return true;
		} else {
			Toast.makeText(_context, "Sensor is not available", Toast.LENGTH_SHORT).show();
			_logger.Debug("Sensor is not available");
			return false;
		}
	}

	private void startAirPressureTimeout() {
		_logger.Debug("Starting humidityTimeoutController...");
		_humidityTimeoutHandler.postDelayed(_humidityTimeoutCheck, _humidityTimeout);
	}

	private void stopAirPressureTimeout() {
		_humidityTimeoutHandler.removeCallbacks(_humidityTimeoutCheck);
	}
}
