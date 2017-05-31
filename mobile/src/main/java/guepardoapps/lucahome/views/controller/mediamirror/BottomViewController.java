package guepardoapps.lucahome.views.controller.mediamirror;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.lucahome.R;

public class BottomViewController {

    private static final String TAG = BottomViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private ReceiverController _receiverController;
    private ServiceController _serviceController;

    private boolean _initialized;
    private MediaMirrorViewDto _mediaMirrorViewDto;

    private TextView _mediaMirrorBatteryTextView;
    private Switch _mediaMirrorSocketSwitch;
    private boolean _switchEnabled = true;

    private TextView _versionTextView;

    @SuppressLint("DefaultLocale")
    private BroadcastReceiver _mediaMirrorViewDtoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaMirrorViewDto mediaMirrorViewDto = (MediaMirrorViewDto) intent.getSerializableExtra(Bundles.MEDIAMIRROR_VIEW_DTO);

            if (mediaMirrorViewDto != null) {
                _logger.Debug("New Dto is: " + mediaMirrorViewDto.toString());
                _mediaMirrorViewDto = mediaMirrorViewDto;

                _mediaMirrorBatteryTextView.setText(String.valueOf(_mediaMirrorViewDto.GetBatteryLevel()));

                _switchEnabled = false;
                _mediaMirrorSocketSwitch.setChecked(_mediaMirrorViewDto.GetSocketState());
                _switchEnabled = true;

                _versionTextView.setText(_mediaMirrorViewDto.GetServerVersion());
            } else {
                _logger.Warn("Received null MediaMirrorViewDto...!");
            }
        }
    };

    public BottomViewController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _receiverController = new ReceiverController(_context);
        _serviceController = new ServiceController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _mediaMirrorBatteryTextView = (TextView) ((Activity) _context).findViewById(R.id.mediaMirrorBatteryTextView);

        _mediaMirrorSocketSwitch = (Switch) ((Activity) _context).findViewById(R.id.mediaMirrorSocketSwitch);
        _mediaMirrorSocketSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _logger.Debug("_mediaMirrorSocketSwitch onCheckedChanged to " + String.valueOf(isChecked));

                if (_mediaMirrorViewDto == null) {
                    _logger.Error("_mediaMirrorViewDto is null!");
                    return;
                }

                if (!_switchEnabled) {
                    _logger.Warn("Switch disabled!");
                    return;
                }

                String socketName = _mediaMirrorViewDto.GetSocketName();
                String command = LucaServerAction.SET_SOCKET.toString() + socketName + ((isChecked) ? Constants.STATE_ON : Constants.STATE_OFF);

                _serviceController.StartRestService(
                        socketName,
                        command,
                        Broadcasts.RELOAD_SOCKETS);
            }
        });

        _versionTextView = (TextView) ((Activity) _context).findViewById(R.id.versionTextView);
    }

    public void onResume() {
        _logger.Debug("onResume");
        if (!_initialized) {
            _receiverController.RegisterReceiver(_mediaMirrorViewDtoReceiver, new String[]{Broadcasts.MEDIAMIRROR_VIEW_DTO});
            _initialized = true;
        }
    }

    public void onPause() {
        _logger.Debug("onPause");
        _initialized = false;
        _receiverController.Dispose();
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _initialized = false;
        _receiverController.Dispose();
    }
}
