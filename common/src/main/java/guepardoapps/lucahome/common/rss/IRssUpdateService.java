package guepardoapps.lucahome.common.rss;

import android.support.annotation.NonNull;

import java.util.List;

import guepardoapps.lucahome.common.datatransferobjects.mediaserver.RssViewDto;
import guepardoapps.lucahome.common.enums.RSSFeedType;
import guepardoapps.lucahome.common.services.ILucaService;

@SuppressWarnings({"unused"})
public interface IRssUpdateService extends ILucaService<RssViewDto> {
    String BroadcastRssLoadFinished = "guepardoapps.lucahome.common.rss.load.finished";

    void LoadData(@NonNull RSSFeedType rssFeed);

    List<RssItem> GetRssList();

    void SetActiveRssFeedType(@NonNull RSSFeedType rssFeedType);

    RSSFeedType GetActiveRssFeedType();
}
