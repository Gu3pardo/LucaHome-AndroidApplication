package guepardoapps.mediamirror.common.enums;

import java.io.Serializable;

public enum ServerAction implements Serializable {

	NULL(0, ""), 
	SHOW_YOUTUBE_VIDEO(1, "Show_YouTube_Video"), 
	PLAY_YOUTUBE_VIDEO(2, "Play_YouTube_Video"), 
	STOP_YOUTUBE_VIDEO(3, "Stop_YouTube_Video"), 
	SHOW_WEBVIEW(4, "Show_Webview"), 
	SHOW_CENTER_TEXT(5, "Show_Center_Text"),
	SET_RSS_FEED(6, "Set_Rss_Feed"), 
	RESET_RSS_FEED(7, "Reset_Rss_Feed"), 
	UPDATE_CURRENT_WEATHER(8, "Update_Current_Weather"), 
	UPDATE_FORECAST_WEATHER(9, "Update_Forecast_Weather"), 
	UPDATE_RASPBERRY_TEMPERATURE(10, "Update_Raspberry_Temperature"), 
	UPDATE_IP_ADDRESS(11, "Update_Ip_Address"), 
	UPDATE_BIRTHDAY_ALARM(12, "Update_Birthday_Alarm"),
	INCREASE_VOLUME(13, "Increase_Volume"),
	DECREASE_VOLUME(14, "Decrease_Volume"),
	MUTE_VOLUME(15, "Mute_Volume"),
	UNMUTE_VOLUME(16, "Unmute_Volume"),
	GET_CURRENT_VOLUME(17, "Get_Current_Volume"),
	GET_SAVED_YOUTUBE_IDS(18, "Get_Saved_Youtube_Ids"),
	INCREASE_SCREEN_BRIGHTNESS(19, "Increase_Screen_Brightness"),
	DECREASE_SCREEN_BRIGHTNESS(20, "Decrease_Screen_Brightness"),
	PLAY_ALARM(21, "PLAY_ALARM"),
	STOP_ALARM(22, "STOP_ALARM"),
	GAME_COMMAND(23, "GAME_COMMAND"),
	GAME_PONG_START(24, "GAME_PONG_START"),
	GAME_PONG_STOP(25, "GAME_PONG_STOP"),
	GAME_PONG_PAUSE(26, "GAME_PONG_PAUSE"),
	GAME_PONG_RESUME(27, "GAME_PONG_RESUME"),
	GAME_PONG_RESTART(28, "GAME_PONG_RESTART"),
	GAME_SNAKE_START(29, "GAME_SNAKE_START"),
	GAME_SNAKE_STOP(30, "GAME_SNAKE_STOP"),
	GAME_TETRIS_START(31, "GAME_TETRIS_START"),
	GAME_TETRIS_STOP(32, "GAME_TETRIS_STOP"),
	SCREEN_ON(33, "SCREEN_ON"),
	SCREEN_OFF(34, "SCREEN_OFF"),
	SCREEN_SAVER(35, "SCREEN_SAVER"),
	SCREEN_NORMAL(36, "SCREEN_NORMAL"),
	SYSTEM_REBOOT(37, "SYSTEM_REBOOT"),
	SYSTEM_SHUTDOWN(38, "SYSTEM_SHUTDOWN"),
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
