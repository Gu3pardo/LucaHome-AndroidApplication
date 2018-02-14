package guepardoapps.lucahome.common.rss;

import android.support.annotation.NonNull;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class RssParser implements IRssParser {
    private static final String Tag = RssParser.class.getSimpleName();

    private static final int MaxItemCount = 100;
    private static final String NameSpace = null;

    @Override
    public List<RssItem> Parse(@NonNull InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();

            return readFeed(parser);
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
        } finally {
            inputStream.close();
        }

        return new ArrayList<>();
    }

    private List<RssItem> readFeed(@NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        int itemCount = 0;
        parser.require(XmlPullParser.START_TAG, null, "rss");

        UUID uuid = null;
        String title = null;
        String description = null;
        String link = null;

        List<RssItem> items = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_DOCUMENT && itemCount < MaxItemCount) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            switch (name) {
                case "guid":
                    uuid = UUID.fromString(readElement(parser, "guid"));
                    break;
                case "title":
                    title = readElement(parser, "title");
                    break;
                case "description":
                    description = readElement(parser, "description");
                    break;
                case "link":
                    link = readElement(parser, "link");
                    break;
                default:
                    break;
            }

            if (uuid != null && title != null && description != null && link != null) {
                items.add(new RssItem(uuid, title, description, link));

                uuid = null;
                title = null;
                description = null;
                link = null;
            }

            itemCount++;
        }

        return items;
    }

    private String readElement(@NonNull XmlPullParser parser, @NonNull String element) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, NameSpace, element);
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, NameSpace, element);

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
