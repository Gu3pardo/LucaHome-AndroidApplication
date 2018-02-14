package guepardoapps.lucahome.common.rss;

import android.support.annotation.NonNull;

import java.io.InputStream;

public interface IRssService {
    String BundleItems = "items";
    String BundleTitle = "title";
    String BundleReceiver = "receiver";
    String BundleFeed = "feed";

    InputStream GetInputStream(@NonNull String link);
}
