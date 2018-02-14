package guepardoapps.lucahome.common.constants;

public class Constants {
    // ========== RASPBERRY CONNECTION ==========
    public static final String ActionPath = "/lib/lucahome.php?user=";
    public static final String StateOn = "&state=1";
    public static final String StateOff = "&state=0";

    // ========== MEDIA SERVER DATA ==========
    public static final int MediaServerPort = 8080;

    // ========== YOUTUBE DATA ==========
    public static final String YoutubeSearch = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=%d&q=%s&key=%s";
    public static final int YoutubeMaxResults = 25;
}