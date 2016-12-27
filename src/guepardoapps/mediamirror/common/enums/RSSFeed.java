package guepardoapps.mediamirror.common.enums;

import java.io.Serializable;

public enum RSSFeed implements Serializable {

	DEFAULT(0, "Spektrum der Wissenschaft", "http://www.spektrum.de/alias/rss/spektrum-de-rss-feed/996406"),
	SPIEGEL_SCHLAGZEILEN(1, "Spiegel Schlagzeilen", "http://www.spiegel.de/schlagzeilen/tops/index.rss"),
	SPIEGEL_EILMELDUNGEN(2, "Spiegel Eilmeldungen", "http://www.spiegel.de/schlagzeilen/eilmeldungen/index.rss"),
	SPIEGEL_NETZWELT(3, "Spiegel Netzwelt", "http://www.spiegel.de/netzwelt/index.rss"),
	SPIEGEL_WISSENSCHAFT(4, "Spiegel Wissenschaft", "http://www.spiegel.de/wissenschaft/index.rss"),
	SPIEGEL_GESUNDHEIT(5, "Spiegel Gesundheit", "http://www.spiegel.de/gesundheit/index.rss"),
	HEISE(6, "Heise", "https://www.heise.de/newsticker/heise-atom.xml"),
	HEISE_DEVELOPER(7, "Heise Developer", "https://www.heise.de/developer/rss/news-atom.xml"),
	ZEIT_SCHLAGZEILEN(8, "ZEIT Schlagzeilen", "http://newsfeed.zeit.de/index"),
	ZEIT_WISSEN(9, "ZEIT Wissen", "http://newsfeed.zeit.de/wissen/index"),
	ZEIT_DIGITAL(10, "ZEIT Digital", "http://newsfeed.zeit.de/digital/index"),
	ZEIT_ENTDECKEN(11, "ZEIT Entdecken", "http://newsfeed.zeit.de/entdecken/index"),
	ZEIT_POLITIK(12, "ZEIT Politik", "http://newsfeed.zeit.de/politik/index"),
	ZEIT_GESELLSCHAFT(13, "ZEIT Gesellschaft", "http://newsfeed.zeit.de/gesellschaft/index"),
	SPEKTRUM_DER_WISSENSCHAFT(14, "Spektrum der Wissenschaft", "http://www.spektrum.de/alias/rss/spektrum-de-rss-feed/996406"),
	STERNE_UND_WELTRAUM(15, "Sterne und Weltraum", "http://www.sterne-und-weltraum.de/alias/rss/sterne-und-weltraum-rss-feed/865248"),
	GEHIRN_UND_GEIST(16, "Gehirn und Geist", "http://www.gehirn-und-geist.de/alias/rss/gehirn-und-geist-rss-feed/982626");

	private int _id;
	private String _title;
	private String _url;

	private RSSFeed(int id, String title, String url) {
		_id = id;
		_title = title;
		_url = url;
	}

	public int GetId() {
		return _id;
	}
	
	public String GetTitle() {
		return _title;
	}
	
	public String GetUrl() {
		return _url;
	}

	public static RSSFeed GetById(int id) {
		for (RSSFeed e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return null;
	}

	public static RSSFeed GetByTitle(String title) {
		for (RSSFeed e : values()) {
			if (e._title.contains(title)) {
				return e;
			}
		}
		return null;
	}
}
