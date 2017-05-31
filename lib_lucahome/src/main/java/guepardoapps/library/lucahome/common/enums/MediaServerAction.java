package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

public enum MediaServerAction implements Serializable {

	NULL(0, "", -1), 
	
	SHOW_YOUTUBE_VIDEO(1, "Show_YouTube_Video", 1), 
	PLAY_YOUTUBE_VIDEO(2, "Play_YouTube_Video", 1), 
	PAUSE_YOUTUBE_VIDEO(3, "Pause_YouTube_Video", 1), 
	STOP_YOUTUBE_VIDEO(4, "Stop_YouTube_Video", 1), 
	GET_SAVED_YOUTUBE_IDS(5, "Get_Saved_Youtube_Ids", 1),
	SET_YOUTUBE_PLAY_POSITION(6, "Set_Youtube_Play_Position", 1),
	
	PLAY_SEA_SOUND(6, "Play_Sea_Sound", 1),
	STOP_SEA_SOUND(7, "Stop_Sea_Sound", 1),
	IS_SEA_SOUND_PLAYING(8, "Is_Sea_Sound_Playing", 1),
	GET_SEA_SOUND_COUNTDOWN(9, "Get_Sea_Sound_Countdown", 1),
	
	SHOW_WEBVIEW(10, "Show_Webview", 1), 
	SHOW_CENTER_TEXT(11, "Show_Center_Text", 1),
	
	SET_RSS_FEED(20, "Set_Rss_Feed", 1), 
	RESET_RSS_FEED(21, "Reset_Rss_Feed", 1), 
	
	UPDATE_CURRENT_WEATHER(30, "Update_Current_Weather", 2), 
	UPDATE_FORECAST_WEATHER(31, "Update_Forecast_Weather", 2), 
	UPDATE_RASPBERRY_TEMPERATURE(32, "Update_Raspberry_Temperature", 2), 
	UPDATE_IP_ADDRESS(33, "Update_Ip_Address", 2), 
	UPDATE_BIRTHDAY_ALARM(34, "Update_Birthday_Alarm", 2),
	UPDATE_CALENDAR_ALARM(35, "Update_Calendar_Alarm", 2),
	
	INCREASE_VOLUME(40, "Increase_Volume", 2),
	DECREASE_VOLUME(41, "Decrease_Volume", 2),
	MUTE_VOLUME(42, "Mute_Volume", 2),
	UNMUTE_VOLUME(43, "Unmute_Volume", 2),
	GET_CURRENT_VOLUME(44, "Get_Current_Volume", 2),
	
	PLAY_ALARM(50, "Play_Alarm", 4),
	STOP_ALARM(51, "Stop_Alarm", 4),
	
	GAME_COMMAND(60, "GAME_COMMAND", 1),
	GAME_PONG_START(61, "GAME_PONG_START", 1),
	GAME_PONG_STOP(62, "GAME_PONG_STOP", 1),
	GAME_PONG_PAUSE(63, "GAME_PONG_PAUSE", 1),
	GAME_PONG_RESUME(64, "GAME_PONG_RESUME", 1),
	GAME_PONG_RESTART(65, "GAME_PONG_RESTART", 1),
	GAME_SNAKE_START(66, "GAME_SNAKE_START", 1),
	GAME_SNAKE_STOP(67, "GAME_SNAKE_STOP", 1),
	GAME_TETRIS_START(68, "GAME_TETRIS_START", 1),
	GAME_TETRIS_STOP(69, "GAME_TETRIS_STOP", 1),

	INCREASE_SCREEN_BRIGHTNESS(70, "Increase_Screen_Brightness", 3),
	DECREASE_SCREEN_BRIGHTNESS(71, "Decrease_Screen_Brightness", 3),
	GET_SCREEN_BRIGHTNESS(72, "Get_Screen_Brightness", 1),
	SCREEN_ON(73, "SCREEN_ON", 3),
	SCREEN_OFF(74, "SCREEN_OFF", 3),
	SCREEN_NORMAL(75, "SCREEN_NORMAL", 3),
	
	SYSTEM_REBOOT(80, "SYSTEM_REBOOT", 5),
	SYSTEM_SHUTDOWN(81, "SYSTEM_SHUTDOWN", 5),

	GET_BATTERY_LEVEL(90, "Get_Battery_Level", 1),
	GET_SERVER_VERSION(91, "Get_Server_Version", 1),
	
	GET_MEDIAMIRROR_DTO(100, "Get_MediaMirror_Dto", 1),
	
	PING(1000, "PING", 1);

	private int _id;
	private String _action;
	private int _actionLevel;

	MediaServerAction(int id, String action, int actionLevel) {
		_id = id;
		_action = action;
		_actionLevel = actionLevel;
	}

	public int GetId() {
		return _id;
	}

	@Override
	public String toString() {
		return _action;
	}

	public int GetLevel() {
		return _actionLevel;
	}

	public static MediaServerAction GetById(int id) {
		for (MediaServerAction e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return NULL;
	}

	public static MediaServerAction GetByString(String action) {
		for (MediaServerAction e : values()) {
			if (e._action.contains(action)) {
				return e;
			}
		}
		return NULL;
	}
}
