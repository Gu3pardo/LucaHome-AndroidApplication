package guepardoapps.mediamirror.rss;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.enums.RSSFeed;

public class RssService extends IntentService {
    private static final String TAG = RssService.class.getSimpleName();

    public static final String ITEMS = "items";
    public static final String TITLE = "title";
    public static final String RECEIVER = "receiver";
    public static final String FEED = "feed";

    public RssService() {
        super(RssService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        RSSFeed feed = (RSSFeed) intent.getSerializableExtra(FEED);
        if (feed == null) {
            feed = RSSFeed.DEFAULT;
        }

        List<RssItem> rssItems = null;
        try {
            RssParser parser = new RssParser();
            rssItems = parser.Parse(GetInputStream(feed.GetUrl()));
        } catch (XmlPullParserException e) {
            Logger.getInstance().Error(TAG, e.getMessage());
        } catch (IOException e) {
            Logger.getInstance().Warning(TAG, e.getMessage());
        }

        Bundle bundle = new Bundle();
        bundle.putString(TITLE, feed.GetTitle());
        bundle.putSerializable(ITEMS, (Serializable) rssItems);

        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
        receiver.send(0, bundle);
    }

    public InputStream GetInputStream(@NonNull String link) {
        try {
            URL url = new URL(link);
            return url.openConnection().getInputStream();
        } catch (IOException exception) {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Exception while retrieving the input stream %s", exception));
            return null;
        }
    }
}
