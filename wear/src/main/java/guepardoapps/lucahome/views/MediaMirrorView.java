package guepardoapps.lucahome.views;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.helper.MessageReceiveHelper;
import guepardoapps.library.lucahome.services.helper.MessageSendHelper;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.services.PhoneMessageService;

public class MediaMirrorView extends Activity {

    private static final String TAG = MediaMirrorView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String COMMAND = "ACTION:GET:MEDIA_MIRROR";

    private Context _context;
    private MessageSendHelper _messageSendHelper;
    private ReceiverController _receiverController;

    private boolean _isInitialized;
    private ArrayList<MediaMirrorViewDto> _mediaMirrorDtoList = new ArrayList<>();
    private int _mediaMirrorIndex = 0;

    private TextView _mediaMirrorYoutubeId;
    private TextView _mediaMirrorVolume;
    private TextView _mediaMirrorBattery;
    private TextView _mediaMirrorVersion;

    private Button _mediaMirrorIp;

    private ImageButton _youtubePlay;
    private ImageButton _youtubePause;
    private ImageButton _youtubeStop;

    private Button _volumeIncrease;
    private Button _volumeDecrease;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
            @SuppressWarnings("unchecked")
            ArrayList<MediaMirrorViewDto> mediaMirrorDtoList = (ArrayList<MediaMirrorViewDto>) intent
                    .getSerializableExtra(Bundles.MEDIAMIRROR);
            if (mediaMirrorDtoList != null) {
                if (mediaMirrorDtoList.size() > 0) {
                    _mediaMirrorDtoList = mediaMirrorDtoList;

                    setVisibility(View.VISIBLE);
                    _mediaMirrorIndex = 0;
                    displayData();

                    return;
                }
            }

            _mediaMirrorYoutubeId.setText("No Data!");
            setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_mediamirror);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;
        _messageSendHelper = new MessageSendHelper(_context, PhoneMessageService.class);
        _receiverController = new ReceiverController(_context);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.basicMediaMirrorWatchViewStub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub watchViewStub) {
                _mediaMirrorYoutubeId = (TextView) findViewById(R.id.textViewMediaMirrorYoutubeId);
                _mediaMirrorVolume = (TextView) findViewById(R.id.textViewMediaMirrorVolume);
                _mediaMirrorBattery = (TextView) findViewById(R.id.textViewMediaMirrorBattery);
                _mediaMirrorVersion = (TextView) findViewById(R.id.textViewMediaMirrorVersion);

                _mediaMirrorIp = (Button) findViewById(R.id.buttonMediaMirrorIp);
                _mediaMirrorIp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        _mediaMirrorIndex++;

                        if (_mediaMirrorIndex >= _mediaMirrorDtoList.size()) {
                            _mediaMirrorIndex = 0;
                        }

                        displayData();
                    }
                });

                _youtubePlay = (ImageButton) findViewById(R.id.imageButtonVideoPlay);
                _youtubePlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validateList()) {
                            return;
                        }

                        if (_messageSendHelper != null) {
                            String mediaMirrorIp = _mediaMirrorDtoList.get(_mediaMirrorIndex).GetMediaServerSelection().GetIp();
                            String message = String.format("ACTION:%s:%s:YOUTUBE:PLAY", MessageReceiveHelper.MEDIA_MIRROR_DATA, mediaMirrorIp);
                            _messageSendHelper.SendMessage(message);
                        }
                    }
                });
                _youtubePause = (ImageButton) findViewById(R.id.imageButtonVideoPause);
                _youtubePause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validateList()) {
                            return;
                        }

                        if (_messageSendHelper != null) {
                            String mediaMirrorIp = _mediaMirrorDtoList.get(_mediaMirrorIndex).GetMediaServerSelection().GetIp();
                            String message = String.format("ACTION:%s:%s:YOUTUBE:PAUSE", MessageReceiveHelper.MEDIA_MIRROR_DATA, mediaMirrorIp);
                            _messageSendHelper.SendMessage(message);
                        }
                    }
                });
                _youtubeStop = (ImageButton) findViewById(R.id.imageButtonVideoStop);
                _youtubeStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validateList()) {
                            return;
                        }

                        if (_messageSendHelper != null) {
                            String mediaMirrorIp = _mediaMirrorDtoList.get(_mediaMirrorIndex).GetMediaServerSelection()
                                    .GetIp();
                            String message = String.format("ACTION:%s:%s:YOUTUBE:STOP", MessageReceiveHelper.MEDIA_MIRROR_DATA, mediaMirrorIp);
                            _messageSendHelper.SendMessage(message);
                        }
                    }
                });

                _volumeIncrease = (Button) findViewById(R.id.buttonVolumeIncrease);
                _volumeIncrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validateList()) {
                            return;
                        }

                        if (_messageSendHelper != null) {
                            String mediaMirrorIp = _mediaMirrorDtoList.get(_mediaMirrorIndex).GetMediaServerSelection()
                                    .GetIp();
                            String message = String.format("ACTION:%s:%s:VOLUME:INCREASE", MessageReceiveHelper.MEDIA_MIRROR_DATA, mediaMirrorIp);
                            _messageSendHelper.SendMessage(message);
                        }
                    }
                });
                _volumeDecrease = (Button) findViewById(R.id.buttonVolumeDecrease);
                _volumeDecrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validateList()) {
                            return;
                        }

                        if (_messageSendHelper != null) {
                            String mediaMirrorIp = _mediaMirrorDtoList.get(_mediaMirrorIndex).GetMediaServerSelection()
                                    .GetIp();
                            String message = String.format("ACTION:%s:%s:VOLUME:DECREASE", MessageReceiveHelper.MEDIA_MIRROR_DATA, mediaMirrorIp);
                            _messageSendHelper.SendMessage(message);
                        }
                    }
                });

                if (!_isInitialized) {
                    _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_MEDIAMIRROR});
                    _messageSendHelper.SendMessage(COMMAND);
                    _isInitialized = true;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _logger.Debug("onResume");
    }

    @Override
    protected void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onDestroy();
    }

    private void setVisibility(int visibility) {
        _mediaMirrorVolume.setVisibility(visibility);
        _mediaMirrorBattery.setVisibility(visibility);
        _mediaMirrorIp.setVisibility(visibility);
        _mediaMirrorVersion.setVisibility(visibility);

        _youtubePlay.setVisibility(visibility);
        _youtubePause.setVisibility(visibility);
        _youtubeStop.setVisibility(visibility);
        _volumeIncrease.setVisibility(visibility);
        _volumeDecrease.setVisibility(visibility);
    }

    private void displayData() {
        _mediaMirrorYoutubeId.setText(_mediaMirrorDtoList.get(_mediaMirrorIndex).GetYoutubeId());
        _mediaMirrorVolume.setText(String.format(Locale.getDefault(), "Vol.: %d", _mediaMirrorDtoList.get(_mediaMirrorIndex).GetVolume()));
        _mediaMirrorBattery.setText(String.format(Locale.getDefault(), "Bat.: %d%%", _mediaMirrorDtoList.get(_mediaMirrorIndex).GetBatteryLevel()));
        _mediaMirrorIp.setText(_mediaMirrorDtoList.get(_mediaMirrorIndex).GetMediaServerSelection().GetIp());
        _mediaMirrorVersion.setText(_mediaMirrorDtoList.get(_mediaMirrorIndex).GetServerVersion());
    }

    private boolean validateList() {
        if (_mediaMirrorDtoList == null) {
            _logger.Error("_mediaMirrorDtoList is null!");
            Toasty.error(_context, "_mediaMirrorDtoList is null!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (_mediaMirrorDtoList.size() == 0) {
            _logger.Error("_mediaMirrorDtoList has size 0!");
            Toasty.error(_context, "_mediaMirrorDtoList has size 0!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}