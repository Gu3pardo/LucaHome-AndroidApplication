package guepardoapps.mediamirrorv2.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.RSSFeed;
import guepardoapps.mediamirrorv2.R;
import guepardoapps.mediamirrorv2.common.constants.Broadcasts;
import guepardoapps.mediamirrorv2.common.constants.Bundles;
import guepardoapps.mediamirrorv2.common.models.RSSModel;
import guepardoapps.mediamirrorv2.interfaces.IViewController;
import guepardoapps.mediamirrorv2.rss.RssItem;
import guepardoapps.mediamirrorv2.rss.RssService;

public class RssViewController implements IViewController {
    private static final String TAG = RssViewController.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private ReceiverController _receiverController;

    private TextView _rssTitleTextView;
    private TextView _rssHeader1TextView;
    private TextView _rssDescription1TextView;
    private TextView _rssHeader2TextView;
    private TextView _rssDescription2TextView;
    private TextView _rssHeader3TextView;
    private TextView _rssDescription3TextView;

    private static final int CHANGE_TEXT_TIME = 15 * 1000;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private int _index;
    private List<RssItem> _items;

    private Handler _changeTextHandler = new Handler();

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCreate();
        }
    };

    private BroadcastReceiver _updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("_updateViewReceiver onReceive");
            RSSModel model = (RSSModel) intent.getSerializableExtra(Bundles.RSS_DATA_MODEL);

            if (model != null) {
                _logger.Debug(model.toString());

                if (model.GetVisibility()) {
                    startService(model.GetRSSFeed());
                } else {
                    setVisibility(View.GONE);
                    _changeTextHandler.removeCallbacks(_updateRSSTextViewRunnable);
                }
            }
        }
    };

    private final ResultReceiver _resultReceiver = new ResultReceiver(new Handler()) {
        @SuppressWarnings("unchecked")
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _index = 0;
            _items = (List<RssItem>) resultData.getSerializable(RssService.ITEMS);
            if (_items != null) {
                _changeTextHandler.removeCallbacks(_updateRSSTextViewRunnable);
                _updateRSSTextViewRunnable.run();

                _rssTitleTextView.setText(resultData.getString(RssService.TITLE));
                setVisibility(View.VISIBLE);
            } else {
                _changeTextHandler.removeCallbacks(_updateRSSTextViewRunnable);
                setVisibility(View.GONE);

                Toasty.error(_context, "An error appeared while downloading the rss feed.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private Runnable _updateRSSTextViewRunnable = new Runnable() {
        public void run() {
            if (!_screenEnabled) {
                _logger.Debug("Screen is not enabled!");
                return;
            }

            _logger.Debug("Update RSS text view!");
            _logger.Debug("RSS List size is: " + String.valueOf(_items.size()));
            _logger.Debug("Index is: " + String.valueOf(_index));

            if (_index >= _items.size()) {
                _index = 0;
            }

            _rssHeader1TextView.setText(_items.get(_index).GetTitle());
            _rssDescription1TextView.setText(_items.get(_index).GetDescription());

            if (_index + 2 >= _items.size()) {
                if (_index + 1 >= _items.size()) {
                    _rssHeader2TextView.setText(_items.get(2).GetTitle());
                    _rssDescription2TextView.setText(_items.get(2).GetDescription());
                    _rssHeader3TextView.setText(_items.get(3).GetTitle());
                    _rssDescription3TextView.setText(_items.get(3).GetDescription());
                } else {
                    _rssHeader2TextView.setText(_items.get(_index + 1).GetTitle());
                    _rssDescription2TextView.setText(_items.get(_index + 1).GetDescription());
                    _rssHeader3TextView.setText(_items.get(2).GetTitle());
                    _rssDescription3TextView.setText(_items.get(2).GetDescription());
                }
            } else {
                _rssHeader2TextView.setText(_items.get(_index + 1).GetTitle());
                _rssDescription2TextView.setText(_items.get(_index + 1).GetDescription());
                _rssHeader3TextView.setText(_items.get(_index + 2).GetTitle());
                _rssDescription3TextView.setText(_items.get(_index + 2).GetDescription());
            }
            _index++;

            _changeTextHandler.postDelayed(this, CHANGE_TEXT_TIME);
        }
    };

    public RssViewController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _logger.Debug("onCreate");

        _screenEnabled = true;

        _rssTitleTextView = ((Activity) _context).findViewById(R.id.rssTitleTextView);
        _rssHeader1TextView = ((Activity) _context).findViewById(R.id.rssTextView1);
        _rssDescription1TextView = ((Activity) _context).findViewById(R.id.rssDescriptionTextView1);
        _rssHeader2TextView = ((Activity) _context).findViewById(R.id.rssTextView2);
        _rssDescription2TextView = ((Activity) _context).findViewById(R.id.rssDescriptionTextView2);
        _rssHeader3TextView = ((Activity) _context).findViewById(R.id.rssTextView3);
        _rssDescription3TextView = ((Activity) _context).findViewById(R.id.rssDescriptionTextView3);
    }

    @Override
    public void onStart() {
        _logger.Debug("onStart");
    }

    @Override
    public void onResume() {
        _logger.Debug("onResume");
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateViewReceiver, new String[]{Broadcasts.SHOW_RSS_DATA_MODEL});
            _isInitialized = true;
            _logger.Debug("Initializing!");
        } else {
            _logger.Warning("Is ALREADY initialized!");
        }
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
    }

    private void startService(@NonNull RSSFeed rssFeed) {
        Intent intent = new Intent(_context, RssService.class);
        intent.putExtra(RssService.RECEIVER, _resultReceiver);
        intent.putExtra(RssService.FEED, rssFeed);
        _context.startService(intent);
    }

    private void setVisibility(int visibility) {
        _rssTitleTextView.setVisibility(visibility);
        _rssHeader1TextView.setVisibility(visibility);
        _rssDescription1TextView.setVisibility(visibility);
        _rssHeader2TextView.setVisibility(visibility);
        _rssDescription2TextView.setVisibility(visibility);
        _rssHeader3TextView.setVisibility(visibility);
        _rssDescription3TextView.setVisibility(visibility);
    }
}
