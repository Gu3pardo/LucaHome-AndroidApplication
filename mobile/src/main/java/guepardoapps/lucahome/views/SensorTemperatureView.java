package guepardoapps.lucahome.views;

import java.util.Calendar;
import java.util.Locale;

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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.enums.TemperatureType;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;
import guepardoapps.library.lucahome.customadapter.TemperatureListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.openweather.common.classes.SerializableTime;
import guepardoapps.library.openweather.common.model.WeatherModel;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class SensorTemperatureView extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = SensorTemperatureView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;
    private WeatherModel _currentWeather;
    private SerializableList<TemperatureDto> _temperatureList;

    private PullRefreshLayout _pullRefreshLayout;

    private ProgressBar _progressBar;
    private ListView _listView;

    private ListAdapter _listAdapter;

    private Context _context;

    private BroadcastController _broadcastController;
    private LucaNotificationController _notificationController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private Class<?>[] _activities = {null, SensorHumidityView.class, SensorAirPressureView.class};
    private int[] _images = {R.drawable.wallpaper, R.drawable.main_image_humidity, R.drawable.main_image_airpressure};
    private static final int _startImageIndex = 0;
    private ImageListener _imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(_images[position]);
        }
    };

    private SensorManager _sensorManager;
    private Sensor _sensor;
    private boolean _hasTemperatureSensor;
    private Handler _temperatureTimeoutHandler = new Handler();

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.HOME_AUTOMATION_COMMAND,
                    new String[]{Bundles.HOME_AUTOMATION_ACTION},
                    new Object[]{HomeAutomationAction.GET_TEMPERATURE_LIST});
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
            }

            _pullRefreshLayout.setRefreshing(false);
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
                            _temperatureList.setValue(id, updatedEntry);
                        }
                        break;
                    default:
                        _logger.Warn(String.format(Locale.getDefault(), "%s is not supported!", temperatureType));
                        break;
                }
            }

            if (_temperatureList != null && _currentWeather != null) {
                _notificationController.CreateTemperatureNotification(SensorTemperatureView.class, _temperatureList,
                        _currentWeather);
            }

            if (_temperatureList != null) {
                _listAdapter = new TemperatureListAdapter(_context, _temperatureList);
                _listView.setAdapter(_listAdapter);
            }

            _pullRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_skeleton_nested_list_carousel);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;

        _broadcastController = new BroadcastController(_context);
        _navigationService = new NavigationService(_context);
        _notificationController = new LucaNotificationController(_context);
        _receiverController = new ReceiverController(_context);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing);
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        collapsingToolbar.setCollapsedTitleTextColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setTitle("Temperature");

        _pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.skeletonList_pullRefreshLayout);
        _pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _logger.Debug("onRefresh " + TAG);
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_CURRENT_WEATHER);
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_TEMPERATURE);
            }
        });

        _listView = (ListView) findViewById(R.id.skeletonList_listView);
        _progressBar = (ProgressBar) findViewById(R.id.skeletonList_progressBarListView);

        CarouselView carouselView = (CarouselView) findViewById(R.id.skeletonList_carouselView);
        carouselView.setPageCount(_images.length);
        carouselView.setCurrentItem(_startImageIndex);
        carouselView.setImageListener(_imageListener);
        carouselView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                _logger.Info(String.format(Locale.getDefault(),
                        "onPageScrolled at position %d with positionOffset %f and positionOffsetPixels %d",
                        position, positionOffset, positionOffsetPixels));
            }

            @Override
            public void onPageSelected(int position) {
                _logger.Info(String.format(Locale.getDefault(), "onPageSelected at position %d", position));
                Class<?> targetActivity = _activities[position];
                if (targetActivity != null) {
                    _navigationService.NavigateTo(targetActivity, true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                _logger.Info(String.format(Locale.getDefault(), "onPageScrollStateChanged at state %d", state));
            }
        });

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
                _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_TEMPERATURE});
                _receiverController.RegisterReceiver(_temperatureReceiver, new String[]{Broadcasts.UPDATE_TEMPERATURE});
                _isInitialized = true;
                _hasTemperatureSensor = checkSensorAvailability();
                _getDataRunnable.run();
            }
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");

        if (_hasTemperatureSensor) {
            _sensorManager.unregisterListener(this);
            stopTemperatureTimeout();
        }

        _receiverController.Dispose();
        _isInitialized = false;

        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");

        if (_hasTemperatureSensor) {
            _sensorManager.unregisterListener(this);
            stopTemperatureTimeout();
        }

        _receiverController.Dispose();
        _isInitialized = false;

        super.onDestroy();
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
            SerializableTime time = new SerializableTime(hour, minute, second, 0);

            TemperatureDto newEntry = new TemperatureDto(
                    temperature,
                    "Ambient temperature",
                    time,
                    "n.a.",
                    TemperatureType.SMARTPHONE_SENSOR,
                    "n.a.");

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
        _temperatureTimeoutHandler.postDelayed(_temperatureTimeoutCheck, Timeouts.SENSOR_RELOAD);
    }

    private void stopTemperatureTimeout() {
        _temperatureTimeoutHandler.removeCallbacks(_temperatureTimeoutCheck);
    }
}
