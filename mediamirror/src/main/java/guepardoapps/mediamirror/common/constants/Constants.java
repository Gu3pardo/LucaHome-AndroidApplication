package guepardoapps.mediamirror.common.constants;

public class Constants {
    // CURRENT WEATHER VIEW
    public static final String CITY = "Nuremberg, DE";
    // SERVER
    public static final int SERVER_PORT = 8080;
    // NAS
    public static final String USER = "mediamirror";
    public static final String PASSPHRASE = "mediamirror";

    public static final String NAS_1 = "192.168.178.21";
    public static final String NAS_2 = "192.168.178.0";

    public static final String NAS_GALLERY = "ftp://" + NAS_1 + "/Galerie/";
    public static final String NAS_LIBRARY = "ftp://" + NAS_1 + "/Bibliothek/";
    public static final String NAS_MOVIES = "ftp://" + NAS_1 + "/Filme&Serien/";
    public static final String NAS_MUSIC = "ftp://" + NAS_1 + "/Musik/";
    public static final String NAS_PUBLIC = "ftp://" + NAS_1 + "/Public/";
    // PERMISSIONS
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE_ID = 24565730;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_ID = 24565731;
    public static final int PERMISSION_REQUEST_WRITE_SETTINGS_ID = 24565732;
    public static final int PERMISSION_REQUEST_READ_CALENDAR = 24561123;
    public static final int PERMISSION_REQUEST_READ_CONTACTS = 58293023;
}
