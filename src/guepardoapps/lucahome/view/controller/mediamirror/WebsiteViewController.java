package guepardoapps.lucahome.view.controller.mediamirror;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class WebsiteViewController {

	private static final String TAG = WebsiteViewController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;
	private MediaMirrorController _mediaMirrorController;
	private ReceiverController _receiverController;

	private boolean _initialized;
	private MediaMirrorViewDto _mediaMirrorViewDto;

	private boolean _contentVisible;
	private ImageButton _showContent;

	private TextView _websiteDividerTextView;
	private EditText _editTextWebsite;
	private Button _buttonSendWebsite;

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

	public WebsiteViewController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_mediaMirrorController = new MediaMirrorController(_context);
		_mediaMirrorController.Initialize();
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_showContent = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonShowWebsite);
		_showContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_contentVisible) {
					_showContent.setImageResource(android.R.drawable.arrow_down_float);
					_websiteDividerTextView.setVisibility(View.GONE);
					_editTextWebsite.setVisibility(View.GONE);
					_buttonSendWebsite.setVisibility(View.GONE);
				} else {
					_showContent.setImageResource(android.R.drawable.arrow_up_float);
					_websiteDividerTextView.setVisibility(View.VISIBLE);
					_editTextWebsite.setVisibility(View.VISIBLE);
					_buttonSendWebsite.setVisibility(View.VISIBLE);
				}
				_contentVisible = !_contentVisible;
			}
		});

		_websiteDividerTextView = (TextView) ((Activity) _context).findViewById(R.id.dividerWebsite);
		_editTextWebsite = (EditText) ((Activity) _context).findViewById(R.id.editTextSendWebsite);
		_buttonSendWebsite = (Button) ((Activity) _context).findViewById(R.id.buttonSendWebsite);
		_buttonSendWebsite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonSendText onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				String url = _editTextWebsite.getText().toString();
				if (url == null) {
					_logger.Error("Url is null!");
					ToastView.error(_context, "Url is null!", Toast.LENGTH_LONG).show();
					return;
				}
				if (url.length() < 15 || url.length() > 50) {
					_logger.Error("Url has invalid length!");
					ToastView.error(_context, "Url has invalid length!", Toast.LENGTH_LONG).show();
					return;
				}
				if (!url.startsWith("http") || !url.startsWith("www")) {
					_logger.Error("Url has invalid format!");
					ToastView.error(_context, "Url has invalid format!", Toast.LENGTH_LONG).show();
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.SHOW_WEBVIEW.toString(), url);
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
