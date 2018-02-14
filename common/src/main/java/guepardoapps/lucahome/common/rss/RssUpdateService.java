package guepardoapps.lucahome.common.rss;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.ReceiverController;
import guepardoapps.lucahome.common.datatransferobjects.mediaserver.RssViewDto;
import guepardoapps.lucahome.common.enums.RSSFeedType;
import guepardoapps.lucahome.common.utils.Logger;

public class RssUpdateService implements IRssUpdateService {
    private static final String Tag = RssUpdateService.class.getSimpleName();

    private static final RssUpdateService Singleton = new RssUpdateService();

    private static final int MinTimeoutMin = 5;
    private static final int MaxTimeoutMin = 30;

    private Context _context;

    private BroadcastController _broadcastController;
    private ReceiverController _receiverController;

    private RSSFeedType _activeRssFeed = RSSFeedType.SPEKTRUM_DER_WISSENSCHAFT;
    private List<RssItem> _itemList = new ArrayList<>();

    private boolean _isInitialized;

    private boolean _reloadEnabled;
    private int _reloadTimeout;
    private Handler _reloadHandler = new Handler();
    private Runnable _reloadListRunnable = new Runnable() {
        @Override
        public void run() {
            LoadData(_activeRssFeed);
            if (_reloadEnabled) {
                _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
            }
        }
    };

    private final ResultReceiver _resultReceiver = new ResultReceiver(new Handler()) {
        @SuppressWarnings("unchecked")
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                Logger.getInstance().Error(Tag, "resultData is  null!");
                return;
            }

            List<RssItem> itemList = (List<RssItem>) resultData.getSerializable(RssService.BundleItems);
            if (itemList == null) {
                Logger.getInstance().Error(Tag, "itemList is  null!");
                return;
            }

            _itemList = itemList;
            _broadcastController.SendSimpleBroadcast(BroadcastRssLoadFinished);
        }
    };

    private RssUpdateService() {
    }

    public static RssUpdateService getInstance() {
        return Singleton;
    }

    @Override
    public void Initialize(@NonNull Context context, boolean reloadEnabled, int reloadTimeout, boolean displayNotification, Class<?> receiverActivity) {
        if (_isInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _reloadEnabled = reloadEnabled;

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _receiverController = new ReceiverController(_context);

        SetReloadTimeout(reloadTimeout);

        _isInitialized = true;
    }

    @Override
    public void Dispose() {
        _reloadHandler.removeCallbacks(_reloadListRunnable);
        _receiverController.Dispose();
        _isInitialized = false;
    }

    @Override
    public ArrayList<RssViewDto> GetDataList() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetDataList not implemented for " + Tag);
    }

    @Override
    public List<RssItem> GetRssList() {
        return _itemList;
    }

    @Override
    public RssViewDto GetByUuid(@NonNull UUID uuid) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetByUuid not implemented for " + Tag);
    }

    @Override
    public ArrayList<RssViewDto> SearchDataList(@NonNull String searchKey) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SearchDataList not implemented for " + Tag);
    }

    @Override
    public void LoadData() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method LoadData not implemented for " + Tag);
    }

    @Override
    public void LoadData(@NonNull RSSFeedType rssFeed) {
        Intent intent = new Intent(_context, RssService.class);
        intent.putExtra(RssService.BundleReceiver, _resultReceiver);
        intent.putExtra(RssService.BundleFeed, rssFeed);
        _context.startService(intent);

    }

    @Override
    public void AddEntry(@NonNull RssViewDto newEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method AddEntry not implemented for " + Tag);
    }

    @Override
    public void UpdateEntry(@NonNull RssViewDto updateEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method UpdateEntry not implemented for " + Tag);
    }

    @Override
    public void DeleteEntry(@NonNull RssViewDto deleteEntry) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method DeleteEntry not implemented for " + Tag);
    }

    @Override
    public void SetReloadEnabled(boolean reloadEnabled) {
        _reloadEnabled = reloadEnabled;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    @Override
    public boolean GetReloadEnabled() {
        return _reloadEnabled;
    }

    @Override
    public void SetReloadTimeout(int reloadTimeout) {
        if (reloadTimeout < MinTimeoutMin) {
            reloadTimeout = MinTimeoutMin;
        }
        if (reloadTimeout > MaxTimeoutMin) {
            reloadTimeout = MaxTimeoutMin;
        }

        _reloadTimeout = reloadTimeout * 60 * 1000;
        if (_reloadEnabled) {
            _reloadHandler.removeCallbacks(_reloadListRunnable);
            _reloadHandler.postDelayed(_reloadListRunnable, _reloadTimeout);
        }
    }

    @Override
    public int GetReloadTimeout() {
        return _reloadTimeout;
    }

    @Override
    public void SetActiveRssFeedType(@NonNull RSSFeedType rssFeedType) {
        _activeRssFeed = rssFeedType;
    }

    @Override
    public RSSFeedType GetActiveRssFeedType() {
        return _activeRssFeed;
    }

    @Override
    public Calendar GetLastUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetLastUpdate not implemented for " + Tag);
    }

    @Override
    public void ShowNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method ShowNotification not implemented for " + Tag);
    }

    @Override
    public void CloseNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method CloseNotification not implemented for " + Tag);
    }

    @Override
    public void SetDisplayNotification(boolean displayNotification) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public boolean GetDisplayNotification() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetDisplayNotification not implemented for " + Tag);
    }

    @Override
    public void SetReceiverActivity(Class<?> receiverActivity) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetReceiverActivity not implemented for " + Tag);
    }

    @Override
    public Class<?> GetReceiverActivity() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetReceiverActivity not implemented for " + Tag);
    }
}
