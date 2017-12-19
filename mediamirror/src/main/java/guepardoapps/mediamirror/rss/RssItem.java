package guepardoapps.mediamirror.rss;

import android.support.annotation.NonNull;

public class RssItem {

    private static final String TAG = RssItem.class.getSimpleName();

    private final String _title;
    private final String _description;
    private final String _link;
    private final String _gUid;

    public RssItem(
            @NonNull String title,
            @NonNull String description,
            @NonNull String link,
            @NonNull String gUid) {
        _title = title;
        _description = description;
        _link = link;
        _gUid = gUid;
    }

    public String GetTitle() {
        return _title;
    }

    public String GetDescription() {
        return _description;
    }

    public String GetLink() {
        return _link;
    }

    public String GetGuid() {
        return _gUid;
    }

    @Override
    public String toString() {
        return "{" + TAG + ": {Title: " + _title
                + "}{Description: " + _description
                + "}{Link: " + _link
                + "}{GUid: " + _gUid + "}}";
    }
}
