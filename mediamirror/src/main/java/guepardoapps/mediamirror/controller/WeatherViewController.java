package guepardoapps.mediamirror.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.library.openweather.models.ForecastPartModel;
import guepardoapps.library.openweather.models.WeatherModel;
import guepardoapps.library.openweather.service.OpenWeatherService;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.converter.TimeConverter;
import guepardoapps.mediamirror.interfaces.IViewController;

public class WeatherViewController implements IViewController {
    private static final String TAG = WeatherViewController.class.getSimpleName();

    private static int FORECAST_COUNT = 5;

    private Context _context;
    private ReceiverController _receiverController;

    private ImageView _conditionImageView;
    private TextView _conditionTextView;
    private TextView _temperatureTextView;
    private TextView _humidityTextView;
    private TextView _pressureTextView;
    private TextView _updatedTimeTextView;

    private ImageView[] _weatherForecastConditionImageViews;
    private TextView[] _weatherForecastWeekdayTextViews;
    private TextView[] _weatherForecastDateTextViews;
    private TextView[] _weatherForecastTimeTextViews;
    private TextView[] _weatherForecastTemperatureRangeTextViews;

    private BroadcastReceiver _currentWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WeatherModel currentWeather = (WeatherModel) intent.getSerializableExtra(Bundles.CURRENT_WEATHER_MODEL);
            if (currentWeather != null) {
                _conditionImageView.setImageResource(currentWeather.GetCondition().GetIcon());
                _conditionTextView.setText(currentWeather.GetCondition().GetDescription());
                _temperatureTextView.setText(String.format(Locale.getDefault(), "%.2f C", currentWeather.GetTemperature()));
                _humidityTextView.setText(String.format(Locale.getDefault(), "%.2f %%", currentWeather.GetHumidity()));
                _pressureTextView.setText(String.format(Locale.getDefault(), "%.2f mBar", currentWeather.GetPressure()));
                _updatedTimeTextView.setText(TimeConverter.GetTime(Calendar.getInstance()));
            } else {
                Logger.getInstance().Error(TAG, "CurrentWeather is null!");
                Toasty.error(_context, "CurrentWeather is null!", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver _forecastWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OpenWeatherService.ForecastWeatherDownloadFinishedContent model =
                    (OpenWeatherService.ForecastWeatherDownloadFinishedContent) intent.getSerializableExtra(Bundles.FORECAST_WEATHER_MODEL);
            if (model != null) {
                if (model.ForecastWeather != null) {
                    List<ForecastPartModel> forecastList = model.ForecastWeather.GetList();
                    if (forecastList != null) {
                        int forecastCount = 0;
                        int viewCount = 0;

                        while (forecastList.size() >= FORECAST_COUNT && viewCount < FORECAST_COUNT && forecastCount < forecastList.size()) {
                            if (forecastList.get(forecastCount).GetForecastListType() == ForecastPartModel.ForecastListType.FORECAST) {
                                _weatherForecastConditionImageViews[viewCount].setImageResource(forecastList.get(forecastCount).GetCondition().GetIcon());
                                _weatherForecastDateTextViews[viewCount].setText(forecastList.get(forecastCount).GetDate());
                                _weatherForecastTimeTextViews[viewCount].setText(forecastList.get(forecastCount).GetTime());
                                _weatherForecastTemperatureRangeTextViews[viewCount].setText(forecastList.get(forecastCount).GetTemperatureString());
                                viewCount++;
                            }
                            forecastCount++;
                        }
                    } else {
                        Logger.getInstance().Error(TAG, "Model forecastList is null!");
                        Toasty.error(_context, "Model forecastList is null!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Logger.getInstance().Error(TAG, "Model ForecastWeather is null!");
                    Toasty.error(_context, "Model ForecastWeather is null!", Toast.LENGTH_LONG).show();
                }
            } else {
                Logger.getInstance().Error(TAG, "Model ForecastWeatherDownloadFinishedContent is null!");
                Toasty.error(_context, "Model ForecastWeatherDownloadFinishedContent is null!", Toast.LENGTH_LONG).show();
            }
        }
    };

    public WeatherViewController(@NonNull Context context) {
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _conditionImageView = ((Activity) _context).findViewById(R.id.weatherConditionImageView);
        _conditionTextView = ((Activity) _context).findViewById(R.id.weatherConditionTextView);
        _temperatureTextView = ((Activity) _context).findViewById(R.id.weatherTemperatureTextView);
        _humidityTextView = ((Activity) _context).findViewById(R.id.weatherHumidityTextView);
        _pressureTextView = ((Activity) _context).findViewById(R.id.weatherPressureTextView);
        _updatedTimeTextView = ((Activity) _context).findViewById(R.id.weatherUpdateTextView);

        _weatherForecastConditionImageViews = new ImageView[FORECAST_COUNT];
        _weatherForecastWeekdayTextViews = new TextView[FORECAST_COUNT];
        _weatherForecastDateTextViews = new TextView[FORECAST_COUNT];
        _weatherForecastTimeTextViews = new TextView[FORECAST_COUNT];
        _weatherForecastTemperatureRangeTextViews = new TextView[FORECAST_COUNT];

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
    }

    @Override
    public void onResume() {
        _receiverController.RegisterReceiver(_currentWeatherReceiver, new String[]{Broadcasts.SHOW_CURRENT_WEATHER_MODEL});
        _receiverController.RegisterReceiver(_forecastWeatherReceiver, new String[]{Broadcasts.SHOW_FORECAST_WEATHER_MODEL});
    }

    @Override
    public void onPause() {
        _receiverController.Dispose();
    }

    @Override
    public void onDestroy() {
        _receiverController.Dispose();
    }
}
