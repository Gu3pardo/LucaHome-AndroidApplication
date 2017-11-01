package guepardoapps.lucahome.common.constants;

public class Constants {
    // ========== DOWNLOADS ==========
    public static final int DOWNLOAD_STEPS = 14;

    // ========== RASPBERRY CONNECTION ==========
    public static final String ACTION_PATH = "/lib/lucahome.php?user=";
    // public static final String REST_PASSWORD = "&password=";
    // public static final String REST_ACTION = "&action=";
    public static final String STATE_ON = "&state=1";
    public static final String STATE_OFF = "&state=0";

    // ========== MEDIAMIRROR DATA ==========
    public static final int MEDIAMIRROR_SERVERPORT = 8080;

    // ========== YOUTUBE DATA ==========
    public static final String YOUTUBE_SEARCH = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=%d&q=%s&key=%s";
    public static final int YOUTUBE_MAX_RESULTS = 15;
}