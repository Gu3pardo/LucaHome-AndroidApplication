package guepardoapps.lucahome.view.controller;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.view.*;

import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;
import guepardoapps.lucahomelibrary.services.helper.NavigationService;

public class HomeViewListButtonController {

	private static final String TAG = HomeViewListButtonController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private NavigationService _navigationService;

	private Button _buttonControl;
	private Button _buttonMedia;
	private Button _buttonSensors;
	private Button _buttonShoppingList;
	private Button _buttonSocial;

	private LinearLayout _linearLayoutButtonControl;
	private Button _buttonSockets;
	private Button _buttonSchedules;
	private Button _buttonTimer;

	private LinearLayout _linearLayoutButtonMedia;
	private Button _buttonMovies;
	private Button _buttonSound;
	private Button _buttonMediaServer;

	private LinearLayout _linearLayoutButtonSensors;
	private Button _buttonTemperature;
	private Button _buttonHumidity;
	private Button _buttonAirPressure;

	private LinearLayout _linearLayoutButtonSocial;
	private Button _buttonBirthdays;
	private Button _buttonGames;

	private int _controlVisibility = View.GONE;
	private int _mediaVisibility = View.GONE;
	private int _sensorVisibility = View.GONE;
	private int _socialVisibility = View.GONE;

	private boolean _controlSelectionActive = _controlVisibility == View.VISIBLE;
	private boolean _mediaSelectionActive = _mediaVisibility == View.VISIBLE;
	private boolean _sensorSelectionActive = _sensorVisibility == View.VISIBLE;
	private boolean _socialSelectionActive = _socialVisibility == View.VISIBLE;

	public HomeViewListButtonController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_navigationService = new NavigationService(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		initializeButtonMain();
		initializeButtonControl();
		initializeButtonMedia();
		initializeButtonSensor();
		initializeButtonSocial();
	}

	public void onResume() {
		_logger.Debug("onResume");
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
	}

	private void initializeButtonMain() {
		_logger.Debug("initializeButtonMain");

		_buttonControl = (Button) ((Activity) _context).findViewById(R.id.buttonControl);
		_buttonControl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_controlSelectionActive) {
					_controlVisibility = View.GONE;
				} else {
					_controlVisibility = View.VISIBLE;
				}
				_controlSelectionActive = _controlVisibility == View.VISIBLE;
				_linearLayoutButtonControl.setVisibility(_controlVisibility);
			}
		});

		_buttonMedia = (Button) ((Activity) _context).findViewById(R.id.buttonMedia);
		_buttonMedia.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_mediaSelectionActive) {
					_mediaVisibility = View.GONE;
				} else {
					_mediaVisibility = View.VISIBLE;
				}
				_mediaSelectionActive = _mediaVisibility == View.VISIBLE;
				_linearLayoutButtonMedia.setVisibility(_mediaVisibility);
			}
		});

		_buttonSensors = (Button) ((Activity) _context).findViewById(R.id.buttonSensors);
		_buttonSensors.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_sensorSelectionActive) {
					_sensorVisibility = View.GONE;
				} else {
					_sensorVisibility = View.VISIBLE;
				}
				_sensorSelectionActive = _sensorVisibility == View.VISIBLE;
				_linearLayoutButtonSensors.setVisibility(_sensorVisibility);
			}
		});

		_buttonShoppingList = (Button) ((Activity) _context).findViewById(R.id.buttonShoppingList);
		_buttonShoppingList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(ShoppingListView.class, true);
			}
		});

		_buttonSocial = (Button) ((Activity) _context).findViewById(R.id.buttonSocial);
		_buttonSocial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_socialSelectionActive) {
					_socialVisibility = View.GONE;
				} else {
					_socialVisibility = View.VISIBLE;
				}
				_socialSelectionActive = _socialVisibility == View.VISIBLE;
				_linearLayoutButtonSocial.setVisibility(_socialVisibility);
			}
		});
	}

	private void initializeButtonControl() {
		_logger.Debug("initializeButtonControl");

		_linearLayoutButtonControl = (LinearLayout) ((Activity) _context).findViewById(R.id.linearLayoutButtonControl);
		_linearLayoutButtonControl.setVisibility(_controlVisibility);

		_buttonSockets = (Button) ((Activity) _context).findViewById(R.id.buttonSockets);
		_buttonSockets.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(SocketView.class, true);
			}
		});

		_buttonSchedules = (Button) ((Activity) _context).findViewById(R.id.buttonSchedules);
		_buttonSchedules.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(ScheduleView.class, true);
			}
		});

		_buttonTimer = (Button) ((Activity) _context).findViewById(R.id.buttonTimer);
		_buttonTimer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(TimerView.class, true);
			}
		});
	}

	private void initializeButtonMedia() {
		_logger.Debug("initializeButtonMedia");

		_linearLayoutButtonMedia = (LinearLayout) ((Activity) _context).findViewById(R.id.linearLayoutButtonMedia);
		_linearLayoutButtonMedia.setVisibility(_mediaVisibility);

		_buttonMovies = (Button) ((Activity) _context).findViewById(R.id.buttonMovies);
		_buttonMovies.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(MovieView.class, true);
			}
		});

		_buttonSound = (Button) ((Activity) _context).findViewById(R.id.buttonSound);
		_buttonSound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(SoundView.class, true);
			}
		});

		_buttonMediaServer = (Button) ((Activity) _context).findViewById(R.id.buttonMediaServer);
		_buttonMediaServer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(MediaMirrorView.class, true);
			}
		});
	}

	private void initializeButtonSensor() {
		_logger.Debug("initializeButtonSensor");

		_linearLayoutButtonSensors = (LinearLayout) ((Activity) _context).findViewById(R.id.linearLayoutButtonSensors);
		_linearLayoutButtonSensors.setVisibility(_sensorVisibility);

		_buttonTemperature = (Button) ((Activity) _context).findViewById(R.id.buttonTemperature);
		_buttonTemperature.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(SensorTemperatureView.class, true);
			}
		});

		_buttonHumidity = (Button) ((Activity) _context).findViewById(R.id.buttonHumidity);
		_buttonHumidity.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(SensorHumidityView.class, true);
			}
		});

		_buttonAirPressure = (Button) ((Activity) _context).findViewById(R.id.buttonAirPressure);
		_buttonAirPressure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(SensorAirPressureView.class, true);
			}
		});
	}

	private void initializeButtonSocial() {
		_logger.Debug("initializeButtonSocial");

		_linearLayoutButtonSocial = (LinearLayout) ((Activity) _context).findViewById(R.id.linearLayoutButtonSocial);
		_linearLayoutButtonSocial.setVisibility(_socialVisibility);

		_buttonBirthdays = (Button) ((Activity) _context).findViewById(R.id.buttonBirthdays);
		_buttonBirthdays.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(BirthdayView.class, true);
			}
		});

		_buttonGames = (Button) ((Activity) _context).findViewById(R.id.buttonGames);
		_buttonGames.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(GameView.class, true);
			}
		});
	}
}
