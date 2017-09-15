package guepardoapps.mediamirrorv2.controller;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirrorv2.R;
import guepardoapps.mediamirrorv2.interfaces.IViewController;

public class WeatherViewController implements IViewController {
    private static final String TAG = WeatherViewController.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private ImageView _conditionImageView;
    private TextView _conditionTextView;
    private TextView _temperatureTextView;
    private TextView _humidityTextView;
    private TextView _pressureTextView;
    private TextView _updatedTimeTextView;

    private static int _forecastCount = 5;

    private ImageView[] _weatherForecastConditionImageViews;
    private TextView[] _weatherForecastWeekdayTextViews;
    private TextView[] _weatherForecastDateTextViews;
    private TextView[] _weatherForecastTimeTextViews;
    private TextView[] _weatherForecastTemperatureRangeTextViews;

    public WeatherViewController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        _conditionImageView = ((Activity) _context).findViewById(R.id.weatherConditionImageView);
        _conditionTextView = ((Activity) _context).findViewById(R.id.weatherConditionTextView);
        _temperatureTextView = ((Activity) _context).findViewById(R.id.weatherTemperatureTextView);
        _humidityTextView = ((Activity) _context).findViewById(R.id.weatherHumidityTextView);
        _pressureTextView = ((Activity) _context).findViewById(R.id.weatherPressureTextView);
        _updatedTimeTextView = ((Activity) _context).findViewById(R.id.weatherUpdateTextView);

        _weatherForecastConditionImageViews = new ImageView[_forecastCount];
        _weatherForecastWeekdayTextViews = new TextView[_forecastCount];
        _weatherForecastDateTextViews = new TextView[_forecastCount];
        _weatherForecastTimeTextViews = new TextView[_forecastCount];
        _weatherForecastTemperatureRangeTextViews = new TextView[_forecastCount];

        _weatherForecastConditionImageViews[0] = ((Activity) _context).findViewById(R.id.weatherForecast1Condition);
        _weatherForecastWeekdayTextViews[0] = ((Activity) _context).findViewById(R.id.weatherForecast1Weekday);
        _weatherForecastDateTextViews[0] = ((Activity) _context).findViewById(R.id.weatherForecast1Date);
        _weatherForecastTimeTextViews[0] = ((Activity) _context).findViewById(R.id.weatherForecast1Time);
        _weatherForecastTemperatureRangeTextViews[0] = ((Activity) _context).findViewById(R.id.weatherForecast1TemperatureRange);

        _weatherForecastConditionImageViews[1] = ((Activity) _context).findViewById(R.id.weatherForecast2Condition);
        _weatherForecastWeekdayTextViews[1] = ((Activity) _context).findViewById(R.id.weatherForecast2Weekday);
        _weatherForecastDateTextViews[1] = ((Activity) _context).findViewById(R.id.weatherForecast2Date);
        _weatherForecastTimeTextViews[1] = ((Activity) _context).findViewById(R.id.weatherForecast2Time);
        _weatherForecastTemperatureRangeTextViews[1] = ((Activity) _context).findViewById(R.id.weatherForecast2TemperatureRange);

        _weatherForecastConditionImageViews[2] = ((Activity) _context).findViewById(R.id.weatherForecast3Condition);
        _weatherForecastWeekdayTextViews[2] = ((Activity) _context).findViewById(R.id.weatherForecast3Weekday);
        _weatherForecastDateTextViews[2] = ((Activity) _context).findViewById(R.id.weatherForecast3Date);
        _weatherForecastTimeTextViews[2] = ((Activity) _context).findViewById(R.id.weatherForecast3Time);
        _weatherForecastTemperatureRangeTextViews[2] = ((Activity) _context).findViewById(R.id.weatherForecast3TemperatureRange);

        _weatherForecastConditionImageViews[3] = ((Activity) _context).findViewById(R.id.weatherForecast4Condition);
        _weatherForecastWeekdayTextViews[3] = ((Activity) _context).findViewById(R.id.weatherForecast4Weekday);
        _weatherForecastDateTextViews[3] = ((Activity) _context).findViewById(R.id.weatherForecast4Date);
        _weatherForecastTimeTextViews[3] = ((Activity) _context).findViewById(R.id.weatherForecast4Time);
        _weatherForecastTemperatureRangeTextViews[3] = ((Activity) _context).findViewById(R.id.weatherForecast4TemperatureRange);

        _weatherForecastConditionImageViews[4] = ((Activity) _context).findViewById(R.id.weatherForecast5Condition);
        _weatherForecastWeekdayTextViews[4] = ((Activity) _context).findViewById(R.id.weatherForecast5Weekday);
        _weatherForecastDateTextViews[4] = ((Activity) _context).findViewById(R.id.weatherForecast5Date);
        _weatherForecastTimeTextViews[4] = ((Activity) _context).findViewById(R.id.weatherForecast5Time);
        _weatherForecastTemperatureRangeTextViews[4] = ((Activity) _context).findViewById(R.id.weatherForecast5TemperatureRange);
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");

    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");

    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }
}
