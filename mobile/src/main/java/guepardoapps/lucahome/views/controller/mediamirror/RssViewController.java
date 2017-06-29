package guepardoapps.lucahome.views.controller.mediamirror;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.enums.RSSFeed;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class RssViewController {

    private static final String TAG = RssViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private MediaMirrorController _mediaMirrorController;
    private ReceiverController _receiverController;

    private boolean _initialized;
    private MediaMirrorViewDto _mediaMirrorViewDto;

    private boolean _contentVisible;
    private ImageButton _showContent;

    private TextView _dividerRSSTextView;
    private Spinner _rssSelectionSpinner;

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

    public RssViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _mediaMirrorController = new MediaMirrorController(_context);
        _mediaMirrorController.Initialize();
        _receiverController = new ReceiverController(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        _showContent = ((Activity) _context).findViewById(R.id.imageButtonShowRSS);
        _showContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_contentVisible) {
                    _showContent.setImageResource(android.R.drawable.arrow_down_float);
                    _dividerRSSTextView.setVisibility(View.GONE);
                    _rssSelectionSpinner.setVisibility(View.GONE);
                } else {
                    _showContent.setImageResource(android.R.drawable.arrow_up_float);
                    _dividerRSSTextView.setVisibility(View.VISIBLE);
                    _rssSelectionSpinner.setVisibility(View.VISIBLE);
                }
                _contentVisible = !_contentVisible;
            }
        });

        _dividerRSSTextView = ((Activity) _context).findViewById(R.id.dividerRSS);

        _rssSelectionSpinner = ((Activity) _context).findViewById(R.id.rssSelectionSpinner);
        List<String> rssFeeds = new ArrayList<>();
        for (int index = 0; index < RSSFeed.values().length; index++) {
            rssFeeds.add(RSSFeed.values()[index].GetTitle());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(_context, android.R.layout.simple_spinner_item,
                rssFeeds);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _rssSelectionSpinner.setAdapter(dataAdapter);
        _rssSelectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RSSFeed selectedRSSFeed = RSSFeed.GetById(position);
                _logger.Debug(String.format("selected RSSFeed %s", selectedRSSFeed));

                if (_mediaMirrorViewDto == null) {
                    _logger.Error("_mediaMirrorViewDto is null!");
                    Toasty.error(_context, "Cannot send new feed!", Toast.LENGTH_LONG).show();
                    return;
                }

                _mediaMirrorController.SendCommand(
                        _mediaMirrorViewDto.GetMediaServerSelection().GetIp(),
                        MediaServerAction.SET_RSS_FEED.toString(),
                        String.valueOf(selectedRSSFeed.GetId()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
