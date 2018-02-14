package guepardoapps.lucahome.common.rss;

import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IRssParser {
    List<RssItem> Parse(@NonNull InputStream inputStream) throws XmlPullParserException, IOException;
}
