package guepardoapps.lucahome.view.controller.mediamirror;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class UpdateViewController {

	private static final String TAG = UpdateViewController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;
	private MediaMirrorController _mediaMirrorController;
	private ReceiverController _receiverController;

	private boolean _initialized;
	private MediaMirrorViewDto _mediaMirrorViewDto;

	private boolean _contentVisible;
	private ImageButton _showContent;

	private TextView _updateDividerTextView;
	private LinearLayout _updateLinearLayoutI;
	private LinearLayout _updateLinearLayoutII;
	private Button _buttonUpdateCurrentWeather;
	private Button _buttonUpdateForecastWeather;
	private Button _buttonUpdateRaspberryTemperature;
	private Button _buttonUpdateIp;
	private Button _buttonUpdateBirthdays;
	private Button _buttonUpdateCalendar;

	private BroadcastReceiver _mediaMirrorViewDtoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			MediaMirrorViewDto mediaMirrorViewDto = (MediaMirrorViewDto) intent
					.getSerializableExtra(Bundles.MEDIAMIRROR_VIEW_DTO);
			if (mediaMirrorViewDto != null) {
				_logger.Debug("New Dto is: " + mediaMirrorViewDto.toString());
				_mediaMirrorViewDto = mediaMirrorViewDto;
			} else {
				_logger.Warn("Received null MediaMirrorViewDto...!");
			}
		}
	};

	public UpdateViewController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_mediaMirrorController = new MediaMirrorController(_context);
		_mediaMirrorController.Initialize();
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_showContent = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonShowUpdate);
		_showContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_contentVisible) {
					_showContent.setImageResource(android.R.drawable.arrow_down_float);
					_updateDividerTextView.setVisibility(View.GONE);
					_updateLinearLayoutI.setVisibility(View.GONE);
					_updateLinearLayoutII.setVisibility(View.GONE);
				} else {
					_showContent.setImageResource(android.R.drawable.arrow_up_float);
					_updateDividerTextView.setVisibility(View.VISIBLE);
					_updateLinearLayoutI.setVisibility(View.VISIBLE);
					_updateLinearLayoutII.setVisibility(View.VISIBLE);
				}
				_contentVisible = !_contentVisible;
			}
		});

		_updateDividerTextView = (TextView) ((Activity) _context).findViewById(R.id.dividerUpdate);
		_updateLinearLayoutI = (LinearLayout) ((Activity) _context).findViewById(R.id.linearLayoutUpdateI);
		_updateLinearLayoutII = (LinearLayout) ((Activity) _context).findViewById(R.id.linearLayoutUpdateII);

		_buttonUpdateCurrentWeather = (Button) ((Activity) _context).findViewById(R.id.buttonUpdateCurrentWeather);
		_buttonUpdateCurrentWeather.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonUpdateCurrentWeather onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}
				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.UPDATE_CURRENT_WEATHER.toString(), "");
			}
		});

		_buttonUpdateForecastWeather = (Button) ((Activity) _context).findViewById(R.id.buttonUpdateForecastWeather);
		_buttonUpdateForecastWeather.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonUpdateForecastWeather onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}
				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.UPDATE_FORECAST_WEATHER.toString(), "");
			}
		});

		_buttonUpdateRaspberryTemperature = (Button) ((Activity) _context)
				.findViewById(R.id.buttonUpdateRaspberryTemperature);
		_buttonUpdateRaspberryTemperature.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonUpdateRaspberryTemperature onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}
				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.UPDATE_RASPBERRY_TEMPERATURE.toString(), "");
			}
		});

		_buttonUpdateIp = (Button) ((Activity) _context).findViewById(R.id.buttonUpdateIp);
		_buttonUpdateIp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonUpdateIp onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}
				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.UPDATE_IP_ADDRESS.toString(), "");
			}
		});

		_buttonUpdateBirthdays = (Button) ((Activity) _context).findViewById(R.id.buttonUpdateBirthdays);
		_buttonUpdateBirthdays.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonUpdateBirthdays onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}
				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.UPDATE_BIRTHDAY_ALARM.toString(), "");
			}
		});

		_buttonUpdateCalendar = (Button) ((Activity) _context).findViewById(R.id.buttonUpdateCalendar);
		_buttonUpdateCalendar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonUpdateCalendar onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}
				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.UPDATE_CALENDAR_ALARM.toString(), "");
			}
		});
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_initialized) {
			_receiverController.RegisterReceiver(_mediaMirrorViewDtoReceiver,
					new String[] { Broadcasts.MEDIAMIRROR_VIEW_DTO });
			_initialized = true;
		}
	}

	public void onPause() {
		_logger.Debug("onPause");
		_initialized = false;
		_receiverController.UnregisterReceiver(_mediaMirrorViewDtoReceiver);
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_initialized = false;
		_mediaMirrorController.Dispose();
		_receiverController.UnregisterReceiver(_mediaMirrorViewDtoReceiver);
	}
}
