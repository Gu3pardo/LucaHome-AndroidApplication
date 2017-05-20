package guepardoapps.library.lucahome.common.constants;

import java.util.Arrays;
import java.util.List;

public class Constants {
	// ========== DOWNLOADS ==========
	public static final int DOWNLOAD_STEPS = 15;
	// ========== RASPBERRY CONNECTION ==========
	public static final String[] SERVER_URLs = new String[] { "http://192.168.178.22" };
	public static final String ACTION_PATH = "/lib/lucahome.php?user=";
	public static final String REST_PASSWORD = "&password=";
	public static final String REST_ACTION = "&action=";
	public static final String STATE_ON = "&state=1";
	public static final String STATE_OFF = "&state=0";
	// ========== FURTHER DATA ==========
	public static final String CITY = "Munich, DE";
	public static final String LUCAHOME_SSID = "BellaPeca";
	public static final String ACTIVATED = "Activated";
	public static final String DEACTIVATED = "Deactivated";
	public static final String ACTIVE = "Active";
	public static final String INACTIVE = "Inactive";
	// ========== MEDIAMIRROR DATA ==========
	public static final List<String> MEDIAMIRROR_PLAYER = Arrays.asList("1", "2");
	public static final int MEDIAMIRROR_SERVERPORT = 8080;
	public static final String DIR_UP = "UP";
	public static final String DIR_DOWN = "DOWN";
	public static final String DIR_RIGHT = "RIGHT";
	public static final String DIR_LEFT = "LEFT";
	public static final String DIR_ROTATE = "ROTATE";
}