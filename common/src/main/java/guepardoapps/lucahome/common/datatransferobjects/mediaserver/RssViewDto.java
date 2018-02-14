package guepardoapps.lucahome.common.datatransferobjects.mediaserver;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.common.enums.RSSFeedType;

public class RssViewDto implements IRssViewDto {
    private static final String Tag = RssViewDto.class.getSimpleName();

    private RSSFeedType _rssFeed;
    private boolean _visibility;

    public RssViewDto(@NonNull RSSFeedType rssFeed, boolean visibility) {
        _rssFeed = rssFeed;
        _visibility = visibility;
    }

    @Override
    public RSSFeedType GetRssFeed() {
        return _rssFeed;
    }

    @Override
    public boolean GetVisibility() {
        return _visibility;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"RssFeed\":\"%s\",\"Visibility\":\"%s\"}",
                Tag, _rssFeed, _visibility);
    }
}
