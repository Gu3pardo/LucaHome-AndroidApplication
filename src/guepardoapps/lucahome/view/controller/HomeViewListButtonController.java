package guepardoapps.lucahome.view.controller;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.NavigationService;
import guepardoapps.lucahome.view.BirthdayView;
import guepardoapps.lucahome.view.GameView;
import guepardoapps.lucahome.view.MediaMirrorView;
import guepardoapps.lucahome.view.MovieView;
import guepardoapps.lucahome.view.ScheduleView;
import guepardoapps.lucahome.view.SensorAirPressureView;
import guepardoapps.lucahome.view.SensorHumidityView;
import guepardoapps.lucahome.view.SocketView;
import guepardoapps.lucahome.view.SoundView;
import guepardoapps.lucahome.view.SensorTemperatureView;
import guepardoapps.lucahome.view.TimerView;

public class HomeViewListButtonController {

	private static final String TAG = HomeViewListButtonController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private NavigationService _navigationService;

	private LinearLayout _linearLayoutButtonMain;
	private Button _buttonControl;
	private Button _buttonMedia;
	private Button _buttonSensors;
	private Button _buttonSecurity;
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

	private int _mainVisibility = View.VISIBLE;
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

	public boolean onBackKeyDown() {
		if (_controlSelectionActive) {
			_logger.Debug("_controlSelectionActive");

			_mainVisibility = View.VISIBLE;
			_controlVisibility = View.GONE;

			_controlSelectionActive = _controlVisibility == View.VISIBLE;

			_linearLayoutButtonMain.setVisibility(_mainVisibility);
			_linearLayoutButtonControl.setVisibility(_controlVisibility);

			return true;
		} else if (_mediaSelectionActive) {
			_logger.Debug("_mediaSelectionActive");

			_mainVisibility = View.VISIBLE;
			_mediaVisibility = View.GONE;

			_mediaSelectionActive = _mediaVisibility == View.VISIBLE;

			_linearLayoutButtonMain.setVisibility(_mainVisibility);
			_linearLayoutButtonMedia.setVisibility(_mediaVisibility);

			return true;
		} else if (_sensorSelectionActive) {
			_logger.Debug("_sensorSelectionActive");

			_mainVisibility = View.VISIBLE;
			_sensorVisibility = View.GONE;

			_sensorSelectionActive = _sensorVisibility == View.VISIBLE;

			_linearLayoutButtonMain.setVisibility(_mainVisibility);
			_linearLayoutButtonSensors.setVisibility(_sensorVisibility);

			return true;
		} else if (_socialSelectionActive) {
			_logger.Debug("_socialSelectionActive");

			_mainVisibility = View.VISIBLE;
			_socialVisibility = View.GONE;

			_socialSelectionActive = _socialVisibility == View.VISIBLE;

			_linearLayoutButtonMain.setVisibility(_mainVisibility);
			_linearLayoutButtonSocial.setVisibility(_socialVisibility);

			return true;
		}

		return false;
	}

	private void initializeButtonMain() {
		_logger.Debug("initializeButtonMain");

		_linearLayoutButtonMain = (LinearLayout) ((Activity) _context).findViewById(R.id.linearLayoutButtonMain);
		_linearLayoutButtonMain.setVisibility(_mainVisibility);

		_buttonControl = (Button) ((Activity) _context).findViewById(R.id.buttonControl);
		_buttonControl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mainVisibility = View.GONE;
				_controlVisibility = View.VISIBLE;

				_controlSelectionActive = _controlVisibility == View.VISIBLE;

				_linearLayoutButtonMain.setVisibility(_mainVisibility);
				_linearLayoutButtonControl.setVisibility(_controlVisibility);
			}
		});

		_buttonMedia = (Button) ((Activity) _context).findViewById(R.id.buttonMedia);
		_buttonMedia.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mainVisibility = View.GONE;
				_mediaVisibility = View.VISIBLE;

				_mediaSelectionActive = _mediaVisibility == View.VISIBLE;

				_linearLayoutButtonMain.setVisibility(_mainVisibility);
				_linearLayoutButtonMedia.setVisibility(_mediaVisibility);
			}
		});

		_buttonSensors = (Button) ((Activity) _context).findViewById(R.id.buttonSensors);
		_buttonSensors.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mainVisibility = View.GONE;
				_sensorVisibility = View.VISIBLE;

				_sensorSelectionActive = _sensorVisibility == View.VISIBLE;

				_linearLayoutButtonMain.setVisibility(_mainVisibility);
				_linearLayoutButtonSensors.setVisibility(_sensorVisibility);
			}
		});

		_buttonSecurity = (Button) ((Activity) _context).findViewById(R.id.buttonSecurity);
		_buttonSecurity.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Warn("Security not yet implemented!");
				Toast.makeText(_context, "Security not yet implemented!", Toast.LENGTH_SHORT).show();
			}
		});

		_buttonSocial = (Button) ((Activity) _context).findViewById(R.id.buttonSocial);
		_buttonSocial.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mainVisibility = View.GONE;
				_socialVisibility = View.VISIBLE;

				_socialSelectionActive = _socialVisibility == View.VISIBLE;

				_linearLayoutButtonMain.setVisibility(_mainVisibility);
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
