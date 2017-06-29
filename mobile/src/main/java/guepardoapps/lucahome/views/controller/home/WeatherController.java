package guepardoapps.lucahome.views.controller.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.flaviofaria.kenburnsview.KenBurnsView;

import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.library.openweather.common.enums.WeatherCondition;
import guepardoapps.library.openweather.common.model.WeatherModel;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.library.toolset.controller.SharedPrefController;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.ForecastWeatherView;

public class WeatherController {

    private static final String TAG = WeatherController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;

    private Context _context;

    private BroadcastController _broadcastController;
    private NavigationService _navigationService;
    private ReceiverController _receiverController;

    private Button _buttonCenterWeather;
    private KenBurnsView _imageViewWeatherCondition;

    private BroadcastReceiver _updateWeatherViewReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateWeatherViewReceiver onReceive");
            WeatherModel currentWeather = (WeatherModel) intent.getSerializableExtra(Bundles.WEATHER_CURRENT);
            if (currentWeather != null) {
                _logger.Debug(currentWeather.toString());

                String text = currentWeather.GetBasicText();
                text = text.replace("\n", " ");
                _buttonCenterWeather.setText(text);

                WeatherCondition weatherCondition = currentWeather.GetCondition();
                _logger.Debug(String.format(Locale.getDefault(), "WeatherCondition: %s", weatherCondition));

                Drawable drawable = _context.getResources().getDrawable(weatherCondition.GetIcon());
                drawable.setBounds(0, 0, 30, 30);
                _buttonCenterWeather.setCompoundDrawablesRelative(drawable, null, null, null);

                _imageViewWeatherCondition.setImageResource(weatherCondition.GetWallpaper());
            } else {
                _logger.Warn("currentWeather is null!");
            }
        }
    };

    public WeatherController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _navigationService = new NavigationService(_context);
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _buttonCenterWeather = ((Activity) _context).findViewById(R.id.buttonCenterWeather);
        _buttonCenterWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(ForecastWeatherView.class, true);
            }
        });

        ImageButton imageButtonReloadWeather = ((Activity) _context).findViewById(R.id.imageButtonReloadWeather);
        imageButtonReloadWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_CURRENT_WEATHER);
            }
        });

        _imageViewWeatherCondition = ((Activity) _context).findViewById(R.id.imageViewWeatherCondition);
        if (!new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME).LoadBooleanValueFromSharedPreferences(SharedPrefConstants.MOVE_IMAGES)) {
            _imageViewWeatherCondition.pause();
        }
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            if (_receiverController != null && _broadcastController != null) {
                _receiverController.RegisterReceiver(_updateWeatherViewReceiver, new String[]{Broadcasts.UPDATE_WEATHER_VIEW});

                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.HOME_AUTOMATION_COMMAND,
                        new String[]{Bundles.HOME_AUTOMATION_ACTION},
                        new Object[]{HomeAutomationAction.GET_WEATHER_CURRENT});

                _isInitialized = true;
            }
        }
    }

    public void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }
}
