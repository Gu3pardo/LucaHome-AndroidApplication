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
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class AdvancedViewController {

	private static final String TAG = AdvancedViewController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;
	private MediaMirrorController _mediaMirrorController;
	private ReceiverController _receiverController;

	private boolean _initialized;
	private MediaMirrorViewDto _mediaMirrorViewDto;

	private boolean _contentVisible;
	private ImageButton _showContent;

	private TextView _advancedDividerTextView;
	private LinearLayout _advancedLinearLayout;
	private Button _buttonReboot;
	private Button _buttonShutdown;

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

	public AdvancedViewController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_mediaMirrorController = new MediaMirrorController(_context);
		_mediaMirrorController.Initialize();
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_showContent = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonShowAdvanced);
		_showContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_contentVisible) {
					_showContent.setImageResource(android.R.drawable.arrow_down_float);
					_advancedDividerTextView.setVisibility(View.GONE);
					_advancedLinearLayout.setVisibility(View.GONE);
				} else {
					_showContent.setImageResource(android.R.drawable.arrow_up_float);
					_advancedDividerTextView.setVisibility(View.VISIBLE);
					_advancedLinearLayout.setVisibility(View.VISIBLE);
				}
				_contentVisible = !_contentVisible;
			}
		});

		_advancedDividerTextView = (TextView) ((Activity) _context).findViewById(R.id.dividerAdvanced);
		_advancedLinearLayout = (LinearLayout) ((Activity) _context).findViewById(R.id.advancedLinearLayout);

		_buttonReboot = (Button) ((Activity) _context).findViewById(R.id.buttonReboot);
		_buttonReboot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonReboot onClick");

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.SYSTEM_REBOOT.toString(), "");
			}
		});

		_buttonShutdown = (Button) ((Activity) _context).findViewById(R.id.buttonShutdown);
		_buttonShutdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonShutdown onClick");

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.SYSTEM_SHUTDOWN.toString(), "");
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
