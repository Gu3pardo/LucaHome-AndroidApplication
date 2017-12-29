package guepardoapps.mediamirror.updater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.RSSFeed;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.common.models.RSSModel;

public class RSSViewUpdater {
    private static final String TAG = RSSViewUpdater.class.getSimpleName();

    private int _updateTime;
    private boolean _isRunning;
    private RSSFeed _rssFeed;

    private Handler _updater;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private Runnable _updateRunnable = new Runnable() {
        public void run() {
            LoadRss();
            _updater.postDelayed(_updateRunnable, _updateTime);
        }
    };

    private BroadcastReceiver _resetRSSFeedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _rssFeed = RSSFeed.DEFAULT;
            _updater.removeCallbacks(_updateRunnable);
            _updateRunnable.run();
        }
    };

    private BroadcastReceiver _updateRSSFeedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RSSModel newRSSModel = (RSSModel) intent.getSerializableExtra(Bundles.RSS_MODEL);
            if (newRSSModel != null) {
                _rssFeed = newRSSModel.GetRSSFeed();
                _updater.removeCallbacks(_updateRunnable);
                _updateRunnable.run();
            }
        }
    };

    public RSSViewUpdater(@NonNull Context context) {
        _updater = new Handler();
        _broadcastController = new BroadcastController(context);
        _receiverController = new ReceiverController(context);
    }

    public void Start(int updateTime) {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }

        _updateTime = updateTime;
        _rssFeed = RSSFeed.DEFAULT;

        _updateRunnable.run();

        _receiverController.RegisterReceiver(_resetRSSFeedReceiver, new String[]{Broadcasts.RESET_RSS_FEED});
        _receiverController.RegisterReceiver(_updateRSSFeedReceiver, new String[]{Broadcasts.PERFORM_RSS_UPDATE});

        _isRunning = true;
        LoadRss();
    }

    public void Dispose() {
        _updater.removeCallbacks(_updateRunnable);
        _receiverController.Dispose();
        _isRunning = false;
    }

    public void LoadRss() {
        _broadcastController.SendSerializableBroadcast(
                Broadcasts.SHOW_RSS_DATA_MODEL,
                Bundles.RSS_DATA_MODEL,
                new RSSModel(_rssFeed, true));
    }
}
