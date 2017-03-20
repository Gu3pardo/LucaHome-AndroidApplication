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

import guepardoapps.lucahome.R;

import guepardoapps.toolset.controller.ReceiverController;

public class TextViewController {

	private static final String TAG = TextViewController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;
	private MediaMirrorController _mediaMirrorController;
	private ReceiverController _receiverController;

	private boolean _initialized;
	private MediaMirrorViewDto _mediaMirrorViewDto;

	private boolean _contentVisible;
	private ImageButton _showContent;

	private TextView _textDividerTextView;
	private EditText _editTextSendText;
	private Button _buttonSendText;

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

	public TextViewController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_mediaMirrorController = new MediaMirrorController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_showContent = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonShowText);
		_showContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_contentVisible) {
					_showContent.setImageResource(android.R.drawable.arrow_down_float);
					_textDividerTextView.setVisibility(View.GONE);
					_editTextSendText.setVisibility(View.GONE);
					_buttonSendText.setVisibility(View.GONE);
				} else {
					_showContent.setImageResource(android.R.drawable.arrow_up_float);
					_textDividerTextView.setVisibility(View.VISIBLE);
					_editTextSendText.setVisibility(View.VISIBLE);
					_buttonSendText.setVisibility(View.VISIBLE);
				}
				_contentVisible = !_contentVisible;
			}
		});

		_textDividerTextView = (TextView) ((Activity) _context).findViewById(R.id.dividerText);
		_editTextSendText = (EditText) ((Activity) _context).findViewById(R.id.editTextSendText);
		_buttonSendText = (Button) ((Activity) _context).findViewById(R.id.buttonSendText);
		_buttonSendText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonSendText onClick");
				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				String text = _editTextSendText.getText().toString();
				if (text == null) {
					_logger.Error("Text is null!");
					ToastView.error(_context, "Text is null!", Toast.LENGTH_LONG).show();
					return;
				}
				if (text.length() < 3 || text.length() > 500) {
					_logger.Error("Text has invalid length!");
					ToastView.error(_context, "Text has invalid length!", Toast.LENGTH_LONG).show();
					return;
				}

				_mediaMirrorController.SendServerCommand(_mediaMirrorViewDto.GetServerIp(),
						ServerAction.SHOW_CENTER_TEXT.toString(), text);
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
		_receiverController.UnregisterReceiver(_mediaMirrorViewDtoReceiver);
	}
}
