package guepardoapps.lucahome.common.constants;

public class ServerActions {
	// Sound
	public static final String PLAY_SOUND = "startplaying&song=";
	public static final String STOP_SOUND = "stopplaying";
	public static final String INCREASE_VOLUME = "increasevolume";
	public static final String DECREASE_VOLUME = "decreasevolume";
	public static final String GET_VOLUME = "getvolume";
	public static final String GET_SOUNDS = "getsounds";
	public static final String IS_SOUND_PLAYING = "issoundplaying";
	public static final String GET_PLAYING_FILE = "getplayingfile";
	// Socket
	public static final String GET_SOCKETS = "getsockets";
	public static final String SET_SOCKET = "setsocket&socket=";
	public static final String ADD_SOCKET = "addsocket&name=";
	public static final String UPDATE_SOCKET = "updatesocket&name=";
	public static final String DELETE_SOCKET = "deletesocket&socket=";
	public static final String ACTIVATE_ALL_SOCKETS = "activateAllSockets";
	public static final String DEACTIVATE_ALL_SOCKETS = "deactivateAllSockets";
	// Schedule
	public static final String GET_SCHEDULES = "getschedules";
	public static final String SET_SCHEDULE = "setschedule&schedule=";
	public static final String ADD_SCHEDULE = "addschedule&name=";
	public static final String UPDATE_SCHEDULE = "updateschedule&name=";
	public static final String DELETE_SCHEDULE = "deleteschedule&schedule=";
	public static final String ACTIVATE_ALL_SCHEDULES = "activateAllSchedules";
	public static final String DEACTIVATE_ALL_SCHEDULES = "deactivateAllSchedules";
	// Birthday
	public static final String GET_BIRTHDAYS = "getbirthdays";
	public static final String ADD_BIRTHDAY = "addbirthday&id=";
	public static final String UPDATE_BIRTHDAY = "updatebirthday&id=";
	public static final String DELETE_BIRTHDAY = "deletebirthday&id=";
	// MapContent
	public static final String GET_MAP_CONTENTS = "getmapcontents";
	public static final String ADD_MAP_CONTENT = "addmapcontent&id=";
	public static final String UPDATE_MAP_CONTENT = "updatemapcontent&id=";
	public static final String DELETE_MAP_CONTENT = "deletemapcontent&id=";
	// Movie
	public static final String GET_MOVIES = "getmovies";
	public static final String START_MOVIE = "startmovie&title=";
	public static final String ADD_MOVIE = "addmovie&title=";
	public static final String UPDATE_MOVIE = "updatemovie&title=";
	public static final String DELETE_MOVIE = "deletemovie&title=";
	// Temperature
	public static final String GET_TEMPERATURES = "getcurrenttemperaturerest";
	// Information
	public static final String GET_INFORMATIONS = "getinformationsrest";
	// Change
	public static final String GET_CHANGES = "getchangesrest";
	// User
	public static final String VALIDATE_USER = "validateuser";
}