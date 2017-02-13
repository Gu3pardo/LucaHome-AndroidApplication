package guepardoapps.mediamirror.common.enums;

import java.io.Serializable;

public enum ServerAction implements Serializable {

	NULL(0, ""), 
	
	SHOW_YOUTUBE_VIDEO(1, "Show_YouTube_Video"), 
	PLAY_YOUTUBE_VIDEO(2, "Play_YouTube_Video"), 
	STOP_YOUTUBE_VIDEO(3, "Stop_YouTube_Video"), 
	GET_SAVED_YOUTUBE_IDS(4, "Get_Saved_Youtube_Ids"),
	PLAY_SEA_SOUND(5, "PLAY_SEA_SOUND"),
	STOP_SEA_SOUND(6, "STOP_SEA_SOUND"),
	
	SHOW_WEBVIEW(10, "Show_Webview"), 
	SHOW_CENTER_TEXT(11, "Show_Center_Text"),
	
	SET_RSS_FEED(20, "Set_Rss_Feed"), 
	RESET_RSS_FEED(21, "Reset_Rss_Feed"), 
	
	UPDATE_CURRENT_WEATHER(30, "Update_Current_Weather"), 
	UPDATE_FORECAST_WEATHER(31, "Update_Forecast_Weather"), 
	UPDATE_RASPBERRY_TEMPERATURE(32, "Update_Raspberry_Temperature"), 
	UPDATE_IP_ADDRESS(33, "Update_Ip_Address"), 
	UPDATE_BIRTHDAY_ALARM(34, "Update_Birthday_Alarm"),
	
	INCREASE_VOLUME(40, "Increase_Volume"),
	DECREASE_VOLUME(41, "Decrease_Volume"),
	MUTE_VOLUME(42, "Mute_Volume"),
	UNMUTE_VOLUME(43, "Unmute_Volume"),
	GET_CURRENT_VOLUME(44, "Get_Current_Volume"),
	
	PLAY_ALARM(50, "PLAY_ALARM"),
	STOP_ALARM(51, "STOP_ALARM"),
	
	GAME_COMMAND(60, "GAME_COMMAND"),
	GAME_PONG_START(61, "GAME_PONG_START"),
	GAME_PONG_STOP(62, "GAME_PONG_STOP"),
	GAME_PONG_PAUSE(63, "GAME_PONG_PAUSE"),
	GAME_PONG_RESUME(64, "GAME_PONG_RESUME"),
	GAME_PONG_RESTART(65, "GAME_PONG_RESTART"),
	GAME_SNAKE_START(66, "GAME_SNAKE_START"),
	GAME_SNAKE_STOP(67, "GAME_SNAKE_STOP"),
	GAME_TETRIS_START(68, "GAME_TETRIS_START"),
	GAME_TETRIS_STOP(69, "GAME_TETRIS_STOP"),

	INCREASE_SCREEN_BRIGHTNESS(70, "Increase_Screen_Brightness"),
	DECREASE_SCREEN_BRIGHTNESS(71, "Decrease_Screen_Brightness"),
	SCREEN_ON(72, "SCREEN_ON"),
	SCREEN_OFF(73, "SCREEN_OFF"),
	SCREEN_SAVER(74, "SCREEN_SAVER"),
	SCREEN_NORMAL(75, "SCREEN_NORMAL"),
	
	SYSTEM_REBOOT(80, "SYSTEM_REBOOT"),
	SYSTEM_SHUTDOWN(81, "SYSTEM_SHUTDOWN"),
	
	PING(99, "PING");

	private int _id;
	private String _action;

	private ServerAction(int id, String action) {
		_id = id;
		_action = action;
	}

	public int GetId() {
		return _id;
	}

	@Override
	public String toString() {
		return _action;
	}

	public static ServerAction GetById(int id) {
		for (ServerAction e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return null;
	}

	public static ServerAction GetByString(String action) {
		for (ServerAction e : values()) {
			if (e._action.contains(action)) {
				return e;
			}
		}
		return null;
	}
}
