package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

public enum HomeAutomationAction implements Serializable {

    NULL(0, ""),

    ENABLE_HEATING(10, "ENABLE_HEATING"),
    ENABLE_SEA_SOUND(11, "ENABLE_SEA_SOUND"),
    ENABLE_HEATING_AND_SOUND(12, "ENABLE_HEATING_AND_SOUND"),
    DISABLE_SEA_SOUND(13, "DISABLE_SEA_SOUND"),

    GET_ALL(20, "GET_ALL"),
    GET_BIRTHDAY_LIST(21, "GET_BIRTHDAY_LIST"),
    GET_CHANGE_LIST(22, "GET_CHANGE_LIST"),
    GET_INFORMATION_LIST(23, "GET_INFORMATION_LIST"),
    GET_MAP_DATA(24, "GET_MAP_DATA"),
    GET_MEDIA_MIRROR_DATA(25, "GET_MEDIA_MIRROR_DATA"),
    GET_MENU_LIST(26, "GET_MENU_LIST"),
    GET_MOTION_CAMERA_DATA(27, "GET_MOTION_CAMERA_DATA"),
    GET_MOVIE_LIST(28, "GET_MOVIE_LIST"),
    GET_SCHEDULE_LIST(29, "GET_SCHEDULE_LIST"),
    GET_SHOPPING_LIST(30, "GET_SHOPPING_LIST"),
    GET_SOCKET_LIST(31, "GET_SOCKET_LIST"),
    GET_TEMPERATURE_LIST(32, "GET_TEMPERATURE_LIST"),
    GET_TIMER_LIST(33, "GET_TIMER_LIST"),

    GET_WEATHER_CURRENT(40, "GET_WEATHER_CURRENT"),
    GET_WEATHER_FORECAST(41, "GET_WEATHER_FORECAST"),

    SHOW_NOTIFICATION_SOCKET(50, "SHOW_NOTIFICATION_SOCKET"),
    SHOW_NOTIFICATION_TEMPERATURE(51, "SHOW_NOTIFICATION_TEMPERATURE"),
    SHOW_NOTIFICATION_WEATHER(52, "SHOW_NOTIFICATION_WEATHER"),

    DISABLE_SECURITY_CAMERA(60, "DISABLE_SECURITY_CAMERA"),

    BEACON_SCANNING_START(70, "BEACON_SCANNING_START"),
    BEACON_SCANNING_STOP(71, "BEACON_SCANNING_STOP");

    private int _id;
    private String _action;

    HomeAutomationAction(int id, String action) {
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

    public static HomeAutomationAction GetById(int id) {
        for (HomeAutomationAction e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return NULL;
    }

    public static HomeAutomationAction GetByString(String action) {
        for (HomeAutomationAction e : values()) {
            if (e._action.contains(action)) {
                return e;
            }
        }
        return NULL;
    }
}
