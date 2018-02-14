package guepardoapps.lucahome.common.rss;

import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess"})
public class RssItem implements IRssItem {
    private static final String Tag = RssItem.class.getSimpleName();

    private final UUID _uuid;
    private final String _title;
    private final String _description;
    private final String _link;

    public RssItem(
            @NonNull UUID uuid,
            @NonNull String title,
            @NonNull String description,
            @NonNull String link) {
        _uuid = uuid;
        _title = title;
        _description = description;
        _link = link;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public String GetTitle() {
        return _title;
    }

    @Override
    public String GetDescription() {
        return _description;
    }

    @Override
    public String GetLink() {
        return _link;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"Title\":\"%s\",\"Description\":\"%s\",\"Link\":\"%s\"}",
                Tag, _uuid, _title, _description, _link);
    }
}
