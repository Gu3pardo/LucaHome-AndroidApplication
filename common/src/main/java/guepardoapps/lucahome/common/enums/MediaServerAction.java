package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

@SuppressWarnings({"unused"})
public enum MediaServerAction implements Serializable {

    NULL(0, "", 0),

    YOUTUBE_PLAY(10, "YOUTUBE_PLAY", 1),
    YOUTUBE_PAUSE(11, "YOUTUBE_PAUSE", 1),
    YOUTUBE_STOP(12, "YOUTUBE_STOP", 1),
    YOUTUBE_SET_POSITION(13, "YOUTUBE_SET_POSITION", 1),

    CENTER_TEXT_SET(20, "CENTER_TEXT_SET", 1),

    RADIO_STREAM_PLAY(30, "RADIO_STREAM_PLAY", 2),
    RADIO_STREAM_STOP(31, "RADIO_STREAM_STOP", 2),

    MEDIA_NOTIFICATION_PLAY(40, "MEDIA_NOTIFICATION_PLAY", 2),
    MEDIA_NOTIFICATION_STOP(41, "MEDIA_NOTIFICATION_STOP", 2),

    SLEEP_SOUND_PLAY(50, "SLEEP_SOUND_PLAY", 2),
    SLEEP_SOUND_STOP(51, "SLEEP_SOUND_STOP", 2),

    RSS_FEED_SET(60, "RSS_FEED_SET", 2),
    RSS_FEED_RESET(61, "RSS_FEED_RESET", 2),

    UPDATE_CURRENT_WEATHER(70, "UPDATE_CURRENT_WEATHER", 2),
    UPDATE_FORECAST_WEATHER(71, "UPDATE_FORECAST_WEATHER", 2),
    UPDATE_RASPBERRY_TEMPERATURE(72, "UPDATE_RASPBERRY_TEMPERATURE", 2),
    UPDATE_IP_ADDRESS(73, "UPDATE_IP_ADDRESS", 2),
    UPDATE_BIRTHDAY_ALARM(74, "UPDATE_BIRTHDAY_ALARM", 2),
    UPDATE_CALENDAR_ALARM(75, "UPDATE_CALENDAR_ALARM", 2),

    VOLUME_INCREASE(80, "VOLUME_INCREASE", 3),
    VOLUME_DECREASE(81, "VOLUME_DECREASE", 3),
    VOLUME_MUTE(82, "VOLUME_MUTE", 3),
    VOLUME_UN_MUTE(83, "VOLUME_UN_MUTE", 3),

    SCREEN_BRIGHTNESS_INCREASE(90, "SCREEN_BRIGHTNESS_INCREASE", 3),
    SCREEN_BRIGHTNESS_DECREASE(91, "SCREEN_BRIGHTNESS_DECREASE", 3),
    SCREEN_ON(92, "SCREEN_ON", 3),
    SCREEN_OFF(93, "SCREEN_OFF", 4),
    SCREEN_NORMAL(94, "SCREEN_NORMAL", 3),

    GET_YOUTUBE_DATA(100, "GET_YOUTUBE_DATA", 1),
    GET_CENTER_TEXT(101, "GET_CENTER_TEXT", 1),
    GET_RADIO_DATA(102, "GET_RADIO_DATA", 1),
    GET_MEDIA_NOTIFICATION_DATA(103, "GET_MEDIA_NOTIFICATION_DATA", 1),
    GET_SLEEP_TIMER_DATA(104, "GET_SLEEP_TIMER_DATA", 1),
    GET_RSS_FEED_DATA(105, "GET_RSS_FEED_DATA", 1),
    GET_MEDIA_SERVER_INFORMATION_DATA(106, "GET_MEDIA_SERVER_INFORMATION_DATA", 1),

    SYSTEM_REBOOT(200, "SYSTEM_REBOOT", 5),
    SYSTEM_SHUTDOWN(201, "SYSTEM_SHUTDOWN", 5);

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
