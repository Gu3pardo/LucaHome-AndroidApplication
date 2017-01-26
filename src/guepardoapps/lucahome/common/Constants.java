package guepardoapps.lucahome.common;

public class Constants {
	// ========== LOGGER ==========
	public static final boolean DEBUGGING_ENABLED = true;
	// ========== TESTING ==========
	public static final boolean TESTING_ENABLED = false;
	// ========== NOTIFICATION ==========
	public static final int ID_NOTIFICATION_WEAR = 120288;
	public static final int ID_NOTIFICATION_BIRTHDAY = 219000;
	public static final int ID_NOTIFICATION_TEMPERATURE = 111293;
	public static final int ID_NOTIFICATION_SONG = 234854;
	// ========== BUNDLE KEYS ==========
	public static final String BUNDLE_COMMAND = "BUNDLE_COMMAND";
	public static final String BUNDLE_NAVIGATE_DATA = "BUNDLE_NAVIGATE_DATA";
	public static final String BUNDLE_MAIN_SERVICE_ACTION = "BUNDLE_MAIN_SERVICE_ACTION";
	public static final String BUNDLE_VIEW_PROGRESS = "BUNDLE_VIEW_PROGRESS";
	public static final String BUNDLE_WEATHER_VIEW_DATA = "BUNDLE_WEATHER_VIEW_DATA";
	public static final String BUNDLE_SOCKETLIST_CHECKBOX_VIEW_DATA = "BUNDLE_SOCKETLIST_CHECKBOX_VIEW_DATA";
	public static final String BUNDLE_SOUND_MODEL = "BUNDLE_SOUND_MODEL";
	public static final String BUNDLE_RASPBERRY_SELECTION = "RASPBERRY_SELETION";
	public static final String BUNDLE_ACTION = "ACTION";
	public static final String BUNDLE_USER = "USER";
	public static final String BUNDLE_PASSPHRASE = "PASSPHRASE";
	public static final String BUNDLE_BROADCAST = "BROADCAST";
	public static final String BUNDLE_NAME = "NAME";
	public static final String BUNDLE_LUCA_OBJECT = "LUCA_OBJECT";
	public static final String BUNDLE_SEND_ACTION = "SEND_ACTION";
	public static final String BUNDLE_SOCKET_DATA = "SOCKET_DATA";
	public static final String BUNDLE_NOTIFICATION_TITLE = "BUNDLE_NOTIFICATION_TITLE";
	public static final String BUNDLE_NOTIFICATION_BODY = "BUNDLE_NOTIFICATION_BODY";
	public static final String BUNDLE_NOTIFICATION_ID = "BUNDLE_NOTIFICATION_ID";
	public static final String BUNDLE_NOTIFICATION_ICON = "BUNDLE_NOTIFICATION_ICON";
	public static final String BUNDLE_LOGGED_IN_USER = "LOGGED_IN_USER";
	public static final String BUNDLE_VALIDATE_USER = "VALIDATE_USER";
	public static final String BUNDLE_SOCKET_LIST = "SOCKET_LIST";
	public static final String BUNDLE_SOCKET_SINGLE = "SOCKET_SINGLE";
	public static final String BUNDLE_SCHEDULE_LIST = "SCHEDULE_LIST";
	public static final String BUNDLE_SCHEDULE_SINGLE = "SCHEDULE_SINGLE";
	public static final String BUNDLE_TIMER_LIST = "TIMER_LIST";
	public static final String BUNDLE_TIMER_SINGLE = "TIMER_SINGLE";
	public static final String BUNDLE_BIRTHDAY_LIST = "BIRTHDAY_LIST";
	public static final String BUNDLE_BIRTHDAY_SINGLE = "BIRTHDAY_SINGLE";
	public static final String BUNDLE_MAP_CONTENT_LIST = "BUNDLE_MAP_CONTENT_LIST";
	public static final String BUNDLE_MAP_CONTENT_SINGLE = "MAP_CONTENT_SINGLE";
	public static final String BUNDLE_MOVIE_LIST = "MOVIE_LIST";
	public static final String BUNDLE_MOVIE_SINGLE = "MOVIE_SINGLE";
	public static final String BUNDLE_TEMPERATURE_LIST = "TEMPERATURE_LIST";
	public static final String BUNDLE_TEMPERATURE_SINGLE = "TEMPERATURE_SINGLE";
	public static final String BUNDLE_TEMPERATURE_ID = "TEMPERATURE_ID";
	public static final String BUNDLE_TEMPERATURE_TYPE = "TEMPERATURE_TYPE";
	public static final String BUNDLE_INFORMATION_LIST = "INFORMATION_LIST";
	public static final String BUNDLE_INFORMATION_SINGLE = "INFORMATION_SINGLE";
	public static final String BUNDLE_CHANGE_LIST = "CHANGE_LIST";
	public static final String BUNDLE_CHANGE_SINGLE = "CHANGE_SINGLE";
	public static final String BUNDLE_WEATHER_CURRENT = "WEATHER_CURRENT";
	public static final String BUNDLE_WEATHER_FORECAST = "WEATHER_FORECAST";
	public static final String BUNDLE_WEAR_MESSAGE_TEXT = "BUNDLE_WEAR_MESSAGE_TEXT";
	public static final String BUNDLE_AIR_PRESSURE_LIST = "AIR_PRESSURE_LIST";
	public static final String BUNDLE_AIR_PRESSURE_SINGLE = "AIR_PRESSURE_SINGLE";
	public static final String BUNDLE_HUMIDITY_LIST = "HUMIDITY_LIST";
	public static final String BUNDLE_HUMIDITY_SINGLE = "HUMIDITY_SINGLE";
	// ========== COLORS ==========
	public static final int ACTION_BAR_COLOR = 0xff0097A7;
	public static final int BIRTHDAY_BACKGROUND_COLOR = 0xFFD32F2F;
	// ========== BROADCASTS ==========
	public static final String BROADCAST_COMMAND = "guepardoapps.lucahome.broadcast.command";
	public static final String BROADCAST_MAIN_SERVICE_COMMAND = "guepardoapps.lucahome.broadcast.main_service.command";
	// Get
	public static final String BROADCAST_GET_ALL = "guepardoapps.lucahome.broadcast.get.all";
	// User
	public static final String BROADCAST_VALIDATE_USER = "guepardoapps.lucahome.broadcast.validate.user";
	// Views
	public static final String BROADCAST_UPDATE_PROGRESSBAR = "guepardoapps.lucahome.broadcast.update.view.progressbar";
	public static final String BROADCAST_UPDATE_WEATHER_VIEW = "guepardoapps.lucahome.broadcast.update.view.weather";
	public static final String BROADCAST_UPDATE_FORECAST_VIEW = "guepardoapps.lucahome.broadcast.update.view.forecast";
	public static final String BROADCAST_UPDATE_SOCKETLIST_CHECKBOX_VIEW = "guepardoapps.lucahome.broadcast.update.view.socketlist.checkboxes";
	// Sound View
	public static final String BROADCAST_UPDATE_SONG_VIEW = "guepardoapps.lucahome.broadcast.update.view.song.details";
	public static final String BROADCAST_IS_SOUND_PLAYING = "guepardoapps.lucahome.broadcast.get.isplaying";
	public static final String BROADCAST_PLAYING_FILE = "guepardoapps.lucahome.broadcast.get.playingfile";
	public static final String BROADCAST_GET_SOUNDS = "guepardoapps.lucahome.broadcast.get.sounds";
	public static final String BROADCAST_GET_VOLUME = "guepardoapps.lucahome.broadcast.get.volume";
	public static final String BROADCAST_START_SOUND = "guepardoapps.lucahome.broadcast.start.sound";
	public static final String BROADCAST_STOP_SOUND = "guepardoapps.lucahome.broadcast.stop.sound";
	public static final String BROADCAST_SET_RASPBERRY = "guepardoapps.lucahome.broadcast.set.raspberry";
	public static final String BROADCAST_ACTIVATE_SOUND_SOCKET = "guepardoapps.lucahome.broadcast.activate.sound.socket";
	public static final String BROADCAST_DEACTIVATE_SOUND_SOCKET = "guepardoapps.lucahome.broadcast.deactivate.sound.socket";
	// Socket
	public static final String BROADCAST_UPDATE_SOCKET_VIEW = "guepardoapps.lucahome.broadcast.update.view.socket.details";
	public static final String BROADCAST_ADD_SOCKET = "guepardoapps.lucahome.broadcast.add.wirelesssocket";
	public static final String BROADCAST_UPDATE_SOCKET = "guepardoapps.lucahome.broadcast.update.wirelesssockets";
	public static final String BROADCAST_DELETE_SOCKET = "guepardoapps.lucahome.broadcast.delete.wirelesssocket";
	public static final String BROADCAST_SET_SOCKET = "guepardoapps.lucahome.broadcast.set.wirelesssocket";
	public static final String BROADCAST_RELOAD_SOCKETS = "guepardoapps.lucahome.broadcast.reload.wirelesssockets";
	public static final String BROADCAST_NOTIFICATION_SOCKET = "guepardoapps.lucahome.broadcast.notification.wirelesssocket";
	// Schedule
	public static final String BROADCAST_ADD_SCHEDULE = "guepardoapps.lucahome.broadcast.add.schedule";
	public static final String BROADCAST_UPDATE_SCHEDULE = "guepardoapps.lucahome.broadcast.update.schedule";
	public static final String BROADCAST_DELETE_SCHEDULE = "guepardoapps.lucahome.broadcast.delete.schedule";
	public static final String BROADCAST_SET_SCHEDULE = "guepardoapps.lucahome.broadcast.set.schedule";
	public static final String BROADCAST_RELOAD_SCHEDULE = "guepardoapps.lucahome.broadcast.reload.schedule";
	// Timer
	public static final String BROADCAST_RELOAD_TIMER = "guepardoapps.lucahome.broadcast.reload.timer";
	public static final String BROADCAST_UPDATE_TIMER = "guepardoapps.lucahome.broadcast.UPDATE_CHANGE";
	// Birthday
	public static final String BROADCAST_ADD_BIRTHDAY = "guepardoapps.lucahome.broadcast.add.birthday";
	public static final String BROADCAST_UPDATE_BIRTHDAY = "guepardoapps.lucahome.broadcast.update.birthday";
	public static final String BROADCAST_DELETE_BIRTHDAY = "guepardoapps.lucahome.broadcast.delete.birthday";
	public static final String BROADCAST_RELOAD_BIRTHDAY = "guepardoapps.lucahome.broadcast.reload.birthday";
	public static final String BROADCAST_HAS_BIRTHDAY = "guepardoapps.lucahome.broadcast.has.birthday";
	// MapContent
	public static final String BROADCAST_UPDATE_MAP_CONTENT_VIEW = "guepardoapps.lucahome.broadcast.update.mapcontent.view";
	public static final String BROADCAST_RELOAD_MAP_CONTENT = "guepardoapps.lucahome.broadcast.reload.mapcontent";
	// Movie
	public static final String BROADCAST_ADD_MOVIE = "guepardoapps.lucahome.broadcast.add.movie";
	public static final String BROADCAST_UPDATE_MOVIE = "guepardoapps.lucahome.broadcast.update.movie";
	public static final String BROADCAST_DELETE_MOVIE = "guepardoapps.lucahome.broadcast.delete.movie";
	public static final String BROADCAST_ACTIVATE_MOVIE = "guepardoapps.lucahome.broadcast.activate.movie";
	public static final String BROADCAST_RELOAD_MOVIE = "guepardoapps.lucahome.broadcast.reload.movie";
	// Temperature
	public static final String BROADCAST_UPDATE_TEMPERATURE = "guepardoapps.lucahome.broadcast.UPDATE_TEMPERATURE";
	// Air Pressure
	public static final String BROADCAST_UPDATE_AIR_PRESSURE = "guepardoapps.lucahome.broadcast.UPDATE_AIR_PRESSURE";
	// Humidity
	public static final String BROADCAST_UPDATE_HUMIDITY = "guepardoapps.lucahome.broadcast.UPDATE_HUMIDITY";
	// Change
	public static final String BROADCAST_UPDATE_CHANGE = "guepardoapps.lucahome.broadcast.UPDATE_CHANGE";
	// Information
	public static final String BROADCAST_UPDATE_INFORMATION = "guepardoapps.lucahome.broadcast.UPDATE_INFORMATION";
	// Youtube
	public static final String BROADCAST_YOUTUBE_ID = "guepardoapps.mediamirror.broadcast.selected.youtube.id";
	public static final String BUNDLE_YOUTUBE_ID = "BUNDLE_YOUTUBE_ID";
	// Get
	public static final String BROADCAST_MEDIAMIRROR_VOLUME = "guepardoapps.mediamirror.broadcast.current.volume";
	public static final String BUNDLE_CURRENT_RECEIVED_VOLUME = "BUNDLE_CURRENT_RECEIVED_VOLUME";
	// Download
	public static final String BROADCAST_DOWNLOAD_BIRTHDAY_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_BIRTHDAY_FINISHED";
	public static final String BROADCAST_DOWNLOAD_CHANGE_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_CHANGE_FINISHED";
	public static final String BROADCAST_DOWNLOAD_INFORMATION_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_INFORMATION_FINISHED";
	public static final String BROADCAST_DOWNLOAD_MAP_CONTENT_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_MAP_CONTENT_FINISHED";
	public static final String BROADCAST_DOWNLOAD_MOVIE_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_MOVIE_FINISHED";
	public static final String BROADCAST_DOWNLOAD_SCHEDULE_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_SCHEDULE_FINISHED";
	public static final String BROADCAST_DOWNLOAD_SOCKET_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_SOCKET_FINISHED";
	public static final String BROADCAST_DOWNLOAD_TEMPERATURE_FINISHED = "guepardoapps.lucahome.broadcast.DOWNLOAD_TEMPERATURE_FINISHED";
	// ========== DOWNLOADS ==========
	public static final int DOWNLOAD_STEPS = 10;
	public static final String BIRTHDAY_DOWNLOAD = "BIRTHDAY_DOWNLOAD";
	public static final String CHANGE_DOWNLOAD = "CHANGE_DOWNLOAD";
	public static final String INFORMATION_DOWNLOAD = "INFORMATION_DOWNLOAD";
	public static final String MAP_CONTENT_DOWNLOAD = "MAP_CONTENT_DOWNLOAD";
	public static final String MOVIE_DOWNLOAD = "MOVIE_DOWNLOAD";
	public static final String SCHEDULE_DOWNLOAD = "SCHEDULE_DOWNLOAD";
	public static final String SOCKET_DOWNLOAD = "SOCKET_DOWNLOAD";
	public static final String TEMPERATURE_DOWNLOAD = "TEMPERATURE_DOWNLOAD";
	// ========== SHARED PREFERENCES ==========
	public static final String SHARED_PREF_NAME = "LUCA_HOME";
	public static final String SHARED_PREF_INSTALLED = "APP_VERSION_1.6.0.161117_INSTALLED";
	public static final String DISPLAY_SOCKET_NOTIFICATION = "DISPLAY_SOCKET_NOTIFICATION";
	public static final String DISPLAY_WEATHER_NOTIFICATION = "DISPLAY_WEATHER_NOTIFICATION";
	public static final String DISPLAY_TEMPERATURE_NOTIFICATION = "DISPLAY_TEMPERATURE_NOTIFICATION";
	public static final String START_AUDIO_APP = "Start_Audio_App";
	public static final String START_OSMC_APP = "Start_OSMC_App";
	public static final String USER_DATA_ENTERED = "USER_DATA_ENTERED";
	public static final String USER_NAME = "USER_NAME";
	public static final String USER_PASSPHRASE = "USER_PASSPHRASE";
	public static final String SOUND_RASPBERRY_SELECTION = "SOUND_RASPBERRY_SELECTION";
	// ========== ACTIONS ==========
	// Sound
	public static final String ACTION_PLAY_SOUND = "startplaying&song=";
	public static final String ACTION_STOP_SOUND = "stopplaying";
	public static final String ACTION_INCREASE_VOLUME = "increasevolume";
	public static final String ACTION_DECREASE_VOLUME = "decreasevolume";
	public static final String ACTION_GET_VOLUME = "getvolume";
	public static final String ACTION_GET_SOUNDS = "getsounds";
	public static final String ACTION_IS_SOUND_PLAYING = "issoundplaying";
	public static final String ACTION_GET_PLAYING_FILE = "getplayingfile";
	// Socket
	public static final String ACTION_GET_SOCKETS = "getsockets";
	public static final String ACTION_SET_SOCKET = "setsocket&socket=";
	public static final String ACTION_ADD_SOCKET = "addsocket&name=";
	public static final String ACTION_UPDATE_SOCKET = "updatesocket&name=";
	public static final String ACTION_DELETE_SOCKET = "deletesocket&socket=";
	public static final String ACTION_ACTIVATE_ALL_SOCKETS = "activateAllSockets";
	public static final String ACTION_DEACTIVATE_ALL_SOCKETS = "deactivateAllSockets";
	// Schedule
	public static final String ACTION_GET_SCHEDULES = "getschedules";
	public static final String ACTION_SET_SCHEDULE = "setschedule&schedule=";
	public static final String ACTION_ADD_SCHEDULE = "addschedule&name=";
	public static final String ACTION_UPDATE_SCHEDULE = "updateschedule&name=";
	public static final String ACTION_DELETE_SCHEDULE = "deleteschedule&schedule=";
	public static final String ACTION_ACTIVATE_ALL_SCHEDULES = "activateAllSchedules";
	public static final String ACTION_DEACTIVATE_ALL_SCHEDULES = "deactivateAllSchedules";
	// Birthday
	public static final String ACTION_GET_BIRTHDAYS = "getbirthdays";
	public static final String ACTION_ADD_BIRTHDAY = "addbirthday&id=";
	public static final String ACTION_UPDATE_BIRTHDAY = "updatebirthday&id=";
	public static final String ACTION_DELETE_BIRTHDAY = "deletebirthday&id=";
	// MapContent
	public static final String ACTION_GET_MAP_CONTENTS = "getmapcontents";
	public static final String ACTION_ADD_MAP_CONTENT = "addmapcontent&id=";
	public static final String ACTION_UPDATE_MAP_CONTENT = "updatemapcontent&id=";
	public static final String ACTION_DELETE_MAP_CONTENT = "deletemapcontent&id=";
	// Movie
	public static final String ACTION_GET_MOVIES = "getmovies";
	public static final String ACTION_START_MOVIE = "startmovie&title=";
	public static final String ACTION_ADD_MOVIE = "addmovie&title=";
	public static final String ACTION_UPDATE_MOVIE = "updatemovie&title=";
	public static final String ACTION_DELETE_MOVIE = "deletemovie&title=";
	// Temperature
	public static final String ACTION_GET_TEMPERATURES = "getcurrenttemperaturerest";
	// Information
	public static final String ACTION_GET_INFORMATIONS = "getinformationsrest";
	// Change
	public static final String ACTION_GET_CHANGES = "getchangesrest";
	// User
	public static final String ACTION_VALIDATE_USER = "validateuser";
	// ========== PACKAGE NAME ==========
	public static final String PACKAGE_BUBBLE_UPNP = "com.bubblesoft.android.bubbleupnp";
	public static final String PACKAGE_KORE = "org.xbmc.kore";
	public static final String PACKAGE_YATSE = "org.leetzone.android.yatsewidgetfree";
	// ========== RASPBERRY CONNECTION ==========
	public static final String[] SERVER_URLs = new String[] { /*ENTER_HERE_YOUR_IP*/ };
	public static final String ACTION_PATH = "/lib/lucahome.php?user=";
	public static final String REST_PASSWORD = "&password=";
	public static final String REST_ACTION = "&action=";
	public static final String STATE_ON = "&state=1";
	public static final String STATE_OFF = "&state=0";
	// ========== FURTHER DATA ==========
	public static final String CITY = "ENTER_HERE_YOUR_CITY";
	public static final String LUCAHOME_SSID = "ENTER_HERE_YOUR_SSID";
	public static final String ACTIVATED = "Activated";
	public static final String DEACTIVATED = "Deactivated";
	public static final String ACTIVE = "Active";
	public static final String INACTIVE = "Inactive";
}