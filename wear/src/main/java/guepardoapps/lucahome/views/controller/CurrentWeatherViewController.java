package guepardoapps.lucahome.views.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Display;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.WeatherModelDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.openweather.common.enums.WeatherCondition;

import guepardoapps.library.toolset.common.Tools;
import guepardoapps.library.toolset.controller.DisplayController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.common.constants.CanvasConstants;

@SuppressWarnings("deprecation")
public class CurrentWeatherViewController {

    private static final String TAG = CurrentWeatherViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private ReceiverController _receiverController;
    private Tools _tools;

    private Display _display;

    private WeatherModelDto _currentWeather;

    private BroadcastReceiver _currentWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_currentWeatherReceiver onReceive");
            WeatherModelDto newWeather = (WeatherModelDto) intent.getSerializableExtra(Bundles.CURRENT_WEATHER);
            if (newWeather != null) {
                _currentWeather = newWeather;
                _logger.Debug("New current weather: " + _currentWeather.toString());
            }
        }
    };

    public CurrentWeatherViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);

        _receiverController = new ReceiverController(context);
        _receiverController.RegisterReceiver(_currentWeatherReceiver,
                new String[]{Broadcasts.UPDATE_CURRENT_WEATHER});
        _tools = new Tools();

        _display = new DisplayController(context).GetDisplayDimension();

        _currentWeather = new WeatherModelDto(-273.15, WeatherCondition.NULL, null, null, null);
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.UnregisterReceiver(_currentWeatherReceiver);
    }

    public void Draw(Canvas canvas, int color) {
        _logger.Debug("Draw");
        Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_VERY_SMALL);
        paint.setColor(color);
        canvas.drawText(_currentWeather.GetWatchFaceText(), CanvasConstants.DRAW_DEFAULT_OFFSET_X,
                _display.getHeight() - 120, paint);
    }
}
