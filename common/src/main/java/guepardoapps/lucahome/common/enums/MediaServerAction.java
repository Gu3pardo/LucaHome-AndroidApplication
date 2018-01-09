package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

@SuppressWarnings({"unused"})
public enum MediaServerAction implements Serializable {

    NULL(0, "", -1),

    SHOW_YOUTUBE_VIDEO(1, "Show_YouTube_Video", 1),
    PLAY_YOUTUBE_VIDEO(2, "Play_YouTube_Video", 1),
    PAUSE_YOUTUBE_VIDEO(3, "Pause_YouTube_Video", 1),
    STOP_YOUTUBE_VIDEO(4, "Stop_YouTube_Video", 1),
    GET_SAVED_YOUTUBE_IDS(5, "Get_Saved_Youtube_Ids", 1),
    SET_YOUTUBE_PLAY_POSITION(6, "Set_Youtube_Play_Position", 1),

    PLAY_SEA_SOUND(10, "Play_Sea_Sound", 1),
    STOP_SEA_SOUND(11, "Stop_Sea_Sound", 1),
    IS_SEA_SOUND_PLAYING(12, "Is_Sea_Sound_Playing", 1),
    GET_SEA_SOUND_COUNTDOWN(13, "Get_Sea_Sound_Countdown", 1),

    SHOW_CENTER_TEXT(20, "Show_Center_Text", 1),

    SET_RSS_FEED(30, "Set_Rss_Feed", 1),
    RESET_RSS_FEED(31, "Reset_Rss_Feed", 1),

    UPDATE_CURRENT_WEATHER(40, "Update_Current_Weather", 2),
    UPDATE_FORECAST_WEATHER(41, "Update_Forecast_Weather", 2),
    UPDATE_RASPBERRY_TEMPERATURE(42, "Update_Raspberry_Temperature", 2),
    UPDATE_IP_ADDRESS(43, "Update_Ip_Address", 2),
    UPDATE_BIRTHDAY_ALARM(44, "Update_Birthday_Alarm", 2),
    UPDATE_CALENDAR_ALARM(45, "Update_Calendar_Alarm", 2),

    INCREASE_VOLUME(50, "Increase_Volume", 2),
    DECREASE_VOLUME(51, "Decrease_Volume", 2),
    MUTE_VOLUME(52, "Mute_Volume", 2),
    UN_MUTE_VOLUME(53, "Un_Mute_Volume", 2),
    GET_CURRENT_VOLUME(54, "Get_Current_Volume", 2),

    INCREASE_SCREEN_BRIGHTNESS(60, "Increase_Screen_Brightness", 3),
    DECREASE_SCREEN_BRIGHTNESS(61, "Decrease_Screen_Brightness", 3),
    GET_SCREEN_BRIGHTNESS(62, "Get_Screen_Brightness", 1),
    SCREEN_ON(63, "SCREEN_ON", 3),
    SCREEN_OFF(64, "SCREEN_OFF", 3),
    SCREEN_NORMAL(65, "SCREEN_NORMAL", 3),

    GET_BATTERY_LEVEL(70, "Get_Battery_Level", 1),
    GET_SERVER_VERSION(71, "Get_Server_Version", 1),

    GET_MEDIA_SERVER_DTO(80, "Get_MediaServer_Dto", 1),

    SHOW_RADIO_STREAM(90, "Show_Radio_Stream", 1),
    PLAY_RADIO_STREAM(91, "Play_Radio_Stream", 1),
    STOP_RADIO_STREAM(92, "Stop_Radio_Stream", 1),
    IS_RADIO_STREAM_PLAYING(93, "Is_Radio_Stream_Playing", 1);

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
            if (e._action.contentEquals(action)) {
                return e;
            }
        }
        return NULL;
    }
}
