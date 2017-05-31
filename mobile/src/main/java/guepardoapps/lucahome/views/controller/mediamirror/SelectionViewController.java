package guepardoapps.lucahome.views.controller.mediamirror;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import de.mateware.snacky.Snacky;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;
import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.lucahome.R;

public class SelectionViewController {

    private static final String TAG = SelectionViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private MediaMirrorController _mediaMirrorController;
    private ReceiverController _receiverController;

    private boolean _initialized;
    private MediaMirrorViewDto _mediaMirrorViewDto;

    private static final int UPDATE_TIMEOUT = 5 * 1000;
    private Handler _updateInfoHandler = new Handler();
    private Runnable _updateInfoRunnable = new Runnable() {
        @Override
        public void run() {
            if (_mediaMirrorViewDto != null) {
                _mediaMirrorController.SendCommand(
                        _mediaMirrorViewDto.GetMediaServerSelection().GetIp(),
                        MediaServerAction.GET_MEDIAMIRROR_DTO.toString(),
                        "");
            }
        }
    };

    @SuppressLint("DefaultLocale")
    private BroadcastReceiver _mediaMirrorViewDtoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaMirrorViewDto mediaMirrorViewDto = (MediaMirrorViewDto) intent.getSerializableExtra(Bundles.MEDIAMIRROR_VIEW_DTO);

            _updateInfoHandler.removeCallbacks(_updateInfoRunnable);

            if (mediaMirrorViewDto != null) {
                _logger.Debug("New Dto is: " + mediaMirrorViewDto.toString());
                _mediaMirrorViewDto = mediaMirrorViewDto;
            } else {
                _logger.Warn("Received NULL MediaMirrorViewDto!");
                Snacky.builder()
                        .setActivty((Activity) _context)
                        .setText("Received NULL MediaMirrorViewDto!")
                        .setDuration(Snacky.LENGTH_LONG)
                        .warning()
                        .show();
            }

            _updateInfoHandler.postDelayed(_updateInfoRunnable, UPDATE_TIMEOUT);
        }
    };

    public SelectionViewController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _mediaMirrorController = new MediaMirrorController(_context);
        _mediaMirrorController.Initialize();
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        Spinner mediaMirrorSelectionSpinner = (Spinner) ((Activity) _context).findViewById(R.id.mediaMirrorSelectionSpinner);
        final ArrayList<String> serverLocations = new ArrayList<>();
        for (MediaServerSelection entry : MediaServerSelection.values()) {
            if (entry.GetId() > 0) {
                serverLocations.add(entry.GetLocation());
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                _context,
                android.R.layout.simple_spinner_item,
                serverLocations);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mediaMirrorSelectionSpinner.setAdapter(dataAdapter);
        mediaMirrorSelectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = serverLocations.get(position);
                _logger.Debug(String.format("Selected location %s", selectedLocation));

                String selectedIp = MediaServerSelection.GetByLocation(selectedLocation).GetIp();
                _mediaMirrorController.SendCommand(selectedIp, MediaServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
        _updateInfoHandler.removeCallbacks(_updateInfoRunnable);
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _initialized = false;
        _mediaMirrorController.Dispose();
        _receiverController.Dispose();
        _updateInfoHandler.removeCallbacks(_updateInfoRunnable);
    }
}
