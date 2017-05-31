package guepardoapps.lucahome.views.controller.mediamirror;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class ScreenViewController {

    private static final String TAG = ScreenViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _contentVisible;
    private ImageButton _showContent;

    private Context _context;
    private MediaMirrorController _mediaMirrorController;
    private ReceiverController _receiverController;

    private boolean _initialized;
    private MediaMirrorViewDto _mediaMirrorViewDto;

    private TextView _screenBrightnessTextView;

    private TextView _dividerBrightnessTextView;
    private LinearLayout _brightnessLinearLayout;

    private BroadcastReceiver _mediaMirrorViewDtoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaMirrorViewDto mediaMirrorViewDto = (MediaMirrorViewDto) intent
                    .getSerializableExtra(Bundles.MEDIAMIRROR_VIEW_DTO);
            if (mediaMirrorViewDto != null) {
                _logger.Debug("New Dto is: " + mediaMirrorViewDto.toString());
                _mediaMirrorViewDto = mediaMirrorViewDto;
                int screenBrightness = (_mediaMirrorViewDto.GetScreenBrightness() * 100) / 255;
                _screenBrightnessTextView.setText(String.format(Locale.getDefault(), "%d%%", screenBrightness));
            } else {
                _logger.Warn("Received null MediaMirrorViewDto...!");
            }
        }
    };

    public ScreenViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _mediaMirrorController = new MediaMirrorController(_context);
        _mediaMirrorController.Initialize();
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _showContent = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonShowScreen);
        _showContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_contentVisible) {
                    _showContent.setImageResource(android.R.drawable.arrow_down_float);
                    _dividerBrightnessTextView.setVisibility(View.GONE);
                    _brightnessLinearLayout.setVisibility(View.GONE);
                } else {
                    _showContent.setImageResource(android.R.drawable.arrow_up_float);
                    _dividerBrightnessTextView.setVisibility(View.VISIBLE);
                    _brightnessLinearLayout.setVisibility(View.VISIBLE);
                }
                _contentVisible = !_contentVisible;
            }
        });

        _screenBrightnessTextView = (TextView) ((Activity) _context).findViewById(R.id.textViewBrightnessPercent);

        _dividerBrightnessTextView = (TextView) ((Activity) _context).findViewById(R.id.dividerScreen);
        _brightnessLinearLayout = (LinearLayout) ((Activity) _context).findViewById(R.id.brightnessLinearLayout);

        Button buttonScreenBrightnessIncrease = (Button) ((Activity) _context)
                .findViewById(R.id.buttonScreenBrightnessIncrease);
        buttonScreenBrightnessIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("buttonScreenBrightnessIncrease onClick");
                if (_mediaMirrorViewDto == null) {
                    _logger.Error("_mediaMirrorViewDto is null!");
                    return;
                }

                _mediaMirrorController.SendCommand(
                        _mediaMirrorViewDto.GetMediaServerSelection().GetIp(),
                        MediaServerAction.INCREASE_SCREEN_BRIGHTNESS.toString(),
                        "");

                _mediaMirrorController.SendCommand(
                        _mediaMirrorViewDto.GetMediaServerSelection().GetIp(),
                        MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                        "");
            }
        });

        Button buttonScreenBrightnessDecrease = (Button) ((Activity) _context)
                .findViewById(R.id.buttonScreenBrightnessDecrease);
        buttonScreenBrightnessDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("buttonScreenBrightnessDecrease onClick");
                if (_mediaMirrorViewDto == null) {
                    _logger.Error("_mediaMirrorViewDto is null!");
                    return;
                }

                _mediaMirrorController.SendCommand(
                        _mediaMirrorViewDto.GetMediaServerSelection().GetIp(),
                        MediaServerAction.DECREASE_SCREEN_BRIGHTNESS.toString(),
                        "");

                _mediaMirrorController.SendCommand(
                        _mediaMirrorViewDto.GetMediaServerSelection().GetIp(),
                        MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                        "");
            }
        });
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_initialized) {
            _receiverController.RegisterReceiver(_mediaMirrorViewDtoReceiver,
                    new String[]{Broadcasts.MEDIAMIRROR_VIEW_DTO});
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
