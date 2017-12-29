package guepardoapps.mediamirror.common.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.common.enums.RSSFeed;

public class RSSModel implements Serializable {
    private static final long serialVersionUID = -7114381765862344762L;

    private static final String TAG = RSSModel.class.getSimpleName();

    private RSSFeed _rssFeed;
    private boolean _visibility;

    public RSSModel(
            @NonNull RSSFeed rssFeed,
            boolean visibility) {
        _rssFeed = rssFeed;
        _visibility = visibility;
    }

    public RSSFeed GetRSSFeed() {
        return _rssFeed;
    }

    public boolean GetVisibility() {
        return _visibility;
    }

    @Override
    public String toString() {
        return TAG + ":{RSSFeed:" + _rssFeed.toString() + ";Visibility:" + String.valueOf(_visibility) + "}";
    }
}
