package guepardoapps.lucahome.common.rss;

import java.util.UUID;

@SuppressWarnings({"unused"})
public interface IRssItem {
    UUID GetUuid();

    String GetTitle();

    String GetDescription();

    String GetLink();
}
