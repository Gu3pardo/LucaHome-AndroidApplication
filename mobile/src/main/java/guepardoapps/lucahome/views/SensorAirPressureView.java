package guepardoapps.lucahome.views;

import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.lucahome.common.dto.AirPressureDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.customadapter.AirPressureListAdapter;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;

import guepardoapps.lucahome.R;

public class SensorAirPressureView extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = SensorAirPressureView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final int SENSOR_TYPE = Sensor.TYPE_PRESSURE;

    private boolean _isInitialized;

    private ProgressBar _progressBar;
    private ListView _listView;

    private Context _context;

    private NavigationService _navigationService;

    private SensorManager _sensorManager;
    private Sensor _sensor;
    private boolean _hasAirPressureSensor;
    private Handler _airPressureTimeoutHandler = new Handler();

    private Class<?>[] _activities = {SensorTemperatureView.class, SensorHumidityView.class, null};
    private int[] _images = {R.drawable.wallpaper, R.drawable.main_image_humidity, R.drawable.main_image_airpressure};
    private static final int _startImageIndex = 2;
    private ImageListener _imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(_images[position]);
        }
    };

    private Runnable _airPressureTimeoutCheck = new Runnable() {
        public void run() {
            checkSensorAvailability();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_skeleton_nested_list_carousel);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;

        _navigationService = new NavigationService(_context);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.skeletonList_collapsing);
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(_context, R.color.TextIcon));
        collapsingToolbar.setCollapsedTitleTextColor(android.graphics.Color.argb(0, 0, 0, 0));
        collapsingToolbar.setTitle("Air Pressure");

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
            _isInitialized = true;
            _hasAirPressureSensor = checkSensorAvailability();
        }
    }

    @Override
    public void onPause() {
        if (_hasAirPressureSensor) {
            _sensorManager.unregisterListener(this);
            stopAirPressureTimeout();
        }

        _logger.Debug("onDestroy");
        _isInitialized = false;

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (_hasAirPressureSensor) {
            _sensorManager.unregisterListener(this);
            stopAirPressureTimeout();
        }

        _logger.Debug("onDestroy");
        _isInitialized = false;

        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == SENSOR_TYPE) {
            double airPressure = event.values[0];

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            SerializableTime time = new SerializableTime(hour, minute, second, 0);

            AirPressureDto newEntry = new AirPressureDto(airPressure, "Ambient air pressure", time);

            SerializableList<AirPressureDto> airPressureList = new SerializableList<>();
            airPressureList.addValue(newEntry);

            _listView.setAdapter(new AirPressureListAdapter(_context, airPressureList));

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
        _hasAirPressureSensor = checkSensorAvailability();
        if (!_hasAirPressureSensor) {
            _logger.Warn("No air pressure sensor!");
            Toasty.warning(_context, "No air pressure sensor!", Toast.LENGTH_LONG).show();
            _navigationService.NavigateTo(HomeView.class, true);
        }
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
        _logger.Debug("Starting airPressureTimeoutController...");
        _airPressureTimeoutHandler.postDelayed(_airPressureTimeoutCheck, Timeouts.SENSOR_RELOAD);
    }

    private void stopAirPressureTimeout() {
        _airPressureTimeoutHandler.removeCallbacks(_airPressureTimeoutCheck);
    }
}
