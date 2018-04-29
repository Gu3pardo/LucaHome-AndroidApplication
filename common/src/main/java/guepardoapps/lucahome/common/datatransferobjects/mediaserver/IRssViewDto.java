package guepardoapps.lucahome.common.datatransferobjects.mediaserver;

import guepardoapps.lucahome.common.enums.RSSFeedType;

@SuppressWarnings({"unused"})
public interface IRssViewDto {
    RSSFeedType GetRssFeed();

    boolean GetVisibility();
}
