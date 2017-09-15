package guepardoapps.mediamirrorv2.rss;

import android.support.annotation.NonNull;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import guepardoapps.lucahome.basic.utils.Logger;

public class RssParser {

    private static final String TAG = RssParser.class.getSimpleName();

    private static final int MAX_ITEM_COUNT = 100;
    private static final String NAME_SPACE = null;

    public List<RssItem> Parse(@NonNull InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();

            return readFeed(parser);
        } catch (Exception exception) {
            new Logger(TAG).Error(exception.getMessage());
        } finally {
            inputStream.close();
        }

        return null;
    }

    private List<RssItem> readFeed(@NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        int itemCount = 0;
        parser.require(XmlPullParser.START_TAG, null, "rss");

        String title = null;
        String description = null;
        String link = null;
        String gUid = null;

        List<RssItem> items = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_DOCUMENT && itemCount < MAX_ITEM_COUNT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    title = readElement(parser, "title");
                    break;
                case "description":
                    description = readElement(parser, "description");
                    break;
                case "link":
                    link = readElement(parser, "link");
                    break;
                case "guid":
                    gUid = readElement(parser, "guid");
                    break;
                default:
                    break;
            }

            if (title != null && description != null && link != null && gUid != null) {
                RssItem item = new RssItem(title, description, link, gUid);
                items.add(item);

                title = null;
                description = null;
                link = null;
                gUid = null;
            }

            itemCount++;
        }

        return items;
    }

    private String readElement(
            @NonNull XmlPullParser parser,
            @NonNull String element) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, NAME_SPACE, element);
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, NAME_SPACE, element);

        return value;
    }

    private String readText(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }

        return result;
    }
}
