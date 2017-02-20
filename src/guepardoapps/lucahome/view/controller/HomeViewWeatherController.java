package guepardoapps.lucahome.view.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.view.ForecastWeatherView;

import guepardoapps.lucahomelibrary.common.enums.MainServiceAction;
import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;
import guepardoapps.lucahomelibrary.services.helper.NavigationService;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;
import guepardoapps.toolset.openweather.converter.WeatherConverter;
import guepardoapps.toolset.openweather.model.WeatherModel;

public class HomeViewWeatherController {

	private static final String TAG = HomeViewWeatherController.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;

	private Context _context;

	private BroadcastController _broadcastController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;

	private Button _buttonCenterWeather;

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

				Drawable drawable = _context.getResources()
						.getDrawable(WeatherConverter.GetIconId(currentWeather.GetDescription()));
				drawable.setBounds(0, 0, 30, 30);
				_buttonCenterWeather.setCompoundDrawablesRelative(drawable, null, null, null);
			} else {
				_logger.Warn("currentWeather is null!");
			}
		}
	};

	public HomeViewWeatherController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_broadcastController = new BroadcastController(_context);
		_navigationService = new NavigationService(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");
		_buttonCenterWeather = (Button) ((Activity) _context).findViewById(R.id.buttonCenterWeather);
		_buttonCenterWeather.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(ForecastWeatherView.class, true);
			}
		});
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			if (_receiverController != null && _broadcastController != null) {
				_receiverController.RegisterReceiver(_updateWeatherViewReceiver,
						new String[] { Broadcasts.UPDATE_WEATHER_VIEW });
				_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
						new String[] { Bundles.MAIN_SERVICE_ACTION },
						new Object[] { MainServiceAction.GET_WEATHER_CURRENT });
				_isInitialized = true;
			}
		}
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_updateWeatherViewReceiver);
	}
}
