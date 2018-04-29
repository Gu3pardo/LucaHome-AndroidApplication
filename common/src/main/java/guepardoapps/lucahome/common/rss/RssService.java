package guepardoapps.lucahome.common.rss;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import guepardoapps.lucahome.common.enums.RSSFeedType;
import guepardoapps.lucahome.common.utils.Logger;

public class RssService extends IntentService implements IRssService {
    private static final String Tag = RssService.class.getSimpleName();

    public RssService() {
        super(RssService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        RSSFeedType feed = (RSSFeedType) intent.getSerializableExtra(BundleFeed);
        if (feed == null) {
            feed = RSSFeedType.SPEKTRUM_DER_WISSENSCHAFT;
        }

        List<RssItem> rssItems = null;
        try {
            RssParser parser = new RssParser();
            InputStream inputStream = GetInputStream(feed.GetUrl());
            if (inputStream == null) {
                Logger.getInstance().Error(Tag, "InputStream is null!");
                return;
            }
            rssItems = parser.Parse(GetInputStream(feed.GetUrl()));
        } catch (IOException exception) {
            Logger.getInstance().Warning(Tag, exception.toString());
        }

        Bundle bundle = new Bundle();
        bundle.putString(BundleTitle, feed.GetTitle());
        bundle.putSerializable(BundleItems, (Serializable) rssItems);

        ResultReceiver receiver = intent.getParcelableExtra(BundleReceiver);
        receiver.send(0, bundle);
    }

    @Override
    public InputStream GetInputStream(@NonNull String link) {
        try {
            URL url = new URL(link);
            return url.openConnection().getInputStream();
        } catch (IOException exception) {
            Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Exception while retrieving the input stream %s", exception.toString()));
            return null;
        }
    }
}
