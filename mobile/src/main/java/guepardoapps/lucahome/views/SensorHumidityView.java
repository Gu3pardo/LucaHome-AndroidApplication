package guepardoapps.lucahome.views;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.dto.*;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.customadapter.*;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;

public class SensorHumidityView extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = SensorHumidityView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final int SENSOR_TYPE = Sensor.TYPE_RELATIVE_HUMIDITY;
    private static final int HUMIDITY_TIMEOUT = 5000;

    private boolean _isInitialized;
    private SerializableList<HumidityDto> _humidityList;

    private ProgressBar _progressBar;
    private ListView _listView;

    private ListAdapter _listAdapter;

    private Context _context;

    private BroadcastController _broadcastController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private SensorManager _sensorManager;
    private Sensor _sensor;
    private boolean _hasHumiditySensor;
    private Handler _humidityTimeoutHandler = new Handler();

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
                    new String[]{Bundles.MAIN_SERVICE_ACTION}, new Object[]{MainServiceAction.GET_HUMIDITY});
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
        setContentView(R.layout.view_skeleton_nested_list);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;

        _broadcastController = new BroadcastController(_context);
        _navigationService = new NavigationService(_context);
        _receiverController = new ReceiverController(_context);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing);
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        collapsingToolbar.setCollapsedTitleTextColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setTitle("Humidity");

        _listView = (ListView) findViewById(R.id.skeletonList_listView);
        _progressBar = (ProgressBar) findViewById(R.id.skeletonList_progressBarListView);

        ImageView mainBackground = (ImageView) findViewById(R.id.skeletonList_backdrop);
        mainBackground.setImageResource(R.drawable.main_image_humidity);

        FloatingActionButton buttonAdd = (FloatingActionButton) findViewById(R.id.skeletonList_addButton);
        buttonAdd.setVisibility(View.GONE);

        initializeSensor();
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                _isInitialized = true;
                _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_HUMIDITY});
                _hasHumiditySensor = checkSensorAvailability();
                _getDataRunnable.run();
            }
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");

        if (_hasHumiditySensor) {
            _sensorManager.unregisterListener(this);
            stopAirPressureTimeout();
        }

        _receiverController.Dispose();
        _isInitialized = false;

        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");

        if (_hasHumiditySensor) {
            _sensorManager.unregisterListener(this);
            stopAirPressureTimeout();
        }

        _receiverController.Dispose();
        _isInitialized = false;

        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == SENSOR_TYPE) {
            double humidity = event.values[0];

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            SerializableTime time = new SerializableTime(hour, minute, second, 0);

            HumidityDto newEntry = new HumidityDto(humidity, "Ambient humidity", time);

            _humidityList = new SerializableList<>();
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
            Toasty.warning(_context, "Sensor is not available", Toast.LENGTH_SHORT).show();
            _logger.Debug("Sensor is not available");
            return false;
        }
    }

    private void startAirPressureTimeout() {
        _logger.Debug("Starting humidityTimeoutController...");
        _humidityTimeoutHandler.postDelayed(_humidityTimeoutCheck, HUMIDITY_TIMEOUT);
    }

    private void stopAirPressureTimeout() {
        _humidityTimeoutHandler.removeCallbacks(_humidityTimeoutCheck);
    }
}
