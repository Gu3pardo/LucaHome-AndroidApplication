package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum LucaServerActionTypes implements Serializable {

    NULL(0, ""),

    //BIRTHDAYS
    GET_BIRTHDAYS(10, "getbirthdays"),
    ADD_BIRTHDAY(11, "addbirthday&uuid="),
    UPDATE_BIRTHDAY(12, "updatebirthday&uuid="),
    DELETE_BIRTHDAY(13, "deletebirthday&uuid="),

    //CAMERA
    START_MOTION(20, "startmotion"),
    STOP_MOTION(21, "stopmotion"),
    GET_MOTION_DATA(22, "getmotiondata"),
    SET_MOTION_CONTROL_TASK(23, "setcontroltaskcamera&state="),

    //CHANGES
    GET_CHANGES(30, "getchanges"),

    //COINS
    GET_COINS_ALL(130, "getcoinsall"),
    GET_COINS_USER(131, "getcoinsuser"),
    ADD_COIN(132, "addcoin&uuid="),
    UPDATE_COIN(133, "updatecoin&uuid="),
    DELETE_COIN(134, "deletecoin&uuid="),

    //INFORMATION
    GET_INFORMATION(40, "getinformation"),

    //MAP CONTENT
    GET_MAP_CONTENT(50, "getmapcontent"),
    ADD_MAP_CONTENT(51, "addmapcontent&uuid="),
    UPDATE_MAP_CONTENT(52, "updatemapcontent&uuid="),
    DELETE_MAP_CONTENT(53, "deletemapcontent&uuid="),

    //MEALS
    GET_MEALS(60, "getmeals"),
    UPDATE_MEAL(61, "updatemeal&weekday="),
    CLEAR_MEAL(62, "clearmeal&weekday="),

    //SUGGESTED MEALS
    GET_SUGGESTED_MEALS(63, "getsuggestedmeals"),
    ADD_SUGGESTED_MEAL(64, "addsuggestedmeal&uuid="),
    UPDATE_SUGGESTED_MEAL(65, "updatesuggestedmeal&uuid="),
    DELETE_SUGGESTED_MEAL(66, "deletesuggestedmeal&uuid="),

    //MOVIES
    GET_MOVIES(70, "getmovies"),
    START_MOVIE(71, "startmovie&uuid="),
    UPDATE_MOVIE(72, "updatemovie&uuid="),

    //SHOPPING ITEMS
    GET_SHOPPING_LIST(80, "getshoppinglist"),
    ADD_SHOPPING_ITEM_F(81, "addshoppingitem&uuid=%s&name=%s&group=%s&quantity=%d&unit=%s"),
    UPDATE_SHOPPING_ITEM_F(82, "updateshoppingitem&uuid=%s&name=%s&group=%s&quantity=%d&unit=%s"),
    DELETE_SHOPPING_ITEM_F(83, "deleteshoppingitem&uuid=%s"),
    CLEAR_SHOPPING_LIST(84, "clearshoppinglist"),

    //SUGGESTED SHOPPING ITEMS
    GET_SUGGESTED_SHOPPING_ITEMS(85, "getsuggestedshoppingitems"),
    ADD_SUGGESTED_SHOPPING_ITEM_F(86, "addsuggestedshoppingitem&uuid=%s&name=%s&group=%s&quantity=%d&unit=%s"),
    UPDATE_SUGGESTED_SHOPPING_ITEM_F(87, "updatesuggestedshoppingitem&uuid=%s&name=%s&group=%s&quantity=%d&unit=%s"),
    DELETE_SUGGESTED_SHOPPING_ITEM_F(88, "deletesuggestedshoppingitem&uuid=%s"),

    //WIRELESS SCHEDULES
    GET_WIRELESS_SCHEDULES(90, "getwirelessschedules"),
    SET_WIRELESS_SCHEDULE(91, "setwirelessschedule&uuid="),
    ADD_WIRELESS_SCHEDULE(92, "addwirelessschedule&uuid="),
    UPDATE_WIRELESS_SCHEDULE(93, "updatewirelessschedule&uuid="),
    DELETE_WIRELESS_SCHEDULE(94, "deletewirelessschedule&uuid="),

    //WIRELESS SOCKETS
    GET_WIRELESS_SOCKETS(100, "getwirelesssockets"),
    SET_WIRELESS_SOCKET(102, "setwirelesssocket&uuid="),
    ADD_WIRELESS_SOCKET(103, "addwirelesssocket&uuid="),
    UPDATE_WIRELESS_SOCKET(104, "updatewirelesssocket&uuid="),
    DELETE_WIRELESS_SOCKET(105, "deletewirelesssocket&uuid="),
    DEACTIVATE_ALL_WIRELESS_SOCKETS(106, "deactivateallwirelesssockets"),

    //WIRELESS SWITCHES
    GET_WIRELESS_SWITCHES(110, "getwirelessswitches"),
    ADD_WIRELESS_SWITCH(111, "addwirelessswitch&uuid="),
    UPDATE_WIRELESS_SWITCH(112, "updatewirelessswitch&uuid="),
    DELETE_WIRELESS_SWITCH(113, "deletewirelessswitch&uuid="),
    TOGGLE_WIRELESS_SWITCH(114, "togglewirelessswitch&uuid="),
    TOGGLE_ALL_WIRELESS_SWITCHES(115, "toggleallwirelessswitches"),

    //METER LOGS
    GET_METER_LOGS(120, "getmeterlogs"),
    ADD_METER_LOG(121, "addmeterlog&uuid="),
    UPDATE_METER_LOG(122, "updatemeterlog&uuid="),
    DELETE_METER_LOG(123, "deletemeterlog&uuid="),

    //MONEY LOGS
    GET_MONEY_LOGS_ALL(130, "getmoneylogsall"),
    GET_MONEY_LOGS_USER(131, "getmoneylogsuser"),
    ADD_MONEY_LOG(132, "addmoneylog&uuid="),
    UPDATE_MONEY_LOG(133, "updatemoneylog&uuid="),
    DELETE_MONEY_LOG(134, "deletemoneylog&uuid="),

    //PUCK JS
    GET_PUCK_JS_LIST(140, "getpuckjs"),
    ADD_PUCK_JS_F(141, "addpuckjs&uuid=%s&name=%s&roomuuid=%s&mac=%s"),
    UPDATE_PUCK_JS_F(142, "updatepuckjs&uuid=%s&roomuuid=%s&name=%s&mac=%s"),
    DELETE_PUCK_JS_F(143, "deletepuckjs&uuid=%s"),

    //ROOM
    GET_ROOMS(180, "getrooms"),
    ADD_ROOM(181, "addroom&uuid="),
    UPDATE_ROOM(182, "updateroom&uuid="),
    DELETE_ROOM(183, "deleteroom&uuid="),

    //TEMPERATURE
    GET_TEMPERATURE(150, "getcurrenttemperature"),

    //MEDIA SERVER
    GET_MEDIA_SERVER(160, "getmediaserver"),
    ADD_MEDIA_SERVER(161, "addmediaserver&uuid="),
    UPDATE_MEDIA_SERVER(162, "updatemediaserver&uuid="),
    DELETE_MEDIA_SERVER(163, "deletemediaserver&uuid="),

    //USER
    VALIDATE_USER(170, "validateuser"),
    ADD_USER(171, "adduser&uuid="),
    UPDATE_USER(172, "updateuser&uuid="),
    DELETE_USER(173, "deleteuser&uuid="),

    // ACCESS
    START_ACCESS_ALARM(190, "startaccessalarm"),
    STOP_ACCESS_ALARM(191, "stopaccessalarm"),
    SEND_ACCESS_CODE(192, "sendaccesscode&code="),
    PLAY_ACCESS_ALARM(193, "playaccessalarm"),

    // RADIO STREAM
    GET_RADIO_STREAMS(200, "getradiostreams"),
    ADD_RADIO_STREAM(201, "addradiostream&uuid="),
    UPDATE_RADIO_STREAM(202, "updateradiostream&uuid="),
    DELETE_RADIO_STREAM(203, "deleteradiostream&uuid="),

    // RSS FEED
    GET_RSS_FEEDS(210, "getrssfeeds"),
    ADD_RSS_FEED(211, "addrssfeed&uuid="),
    UPDATE_RSS_FEED(212, "updaterssfeed&uuid="),
    DELETE_RSS_FEED(213, "deleterssfeed&uuid="),

    // YOUTUBE VIDEO
    GET_YOUTUBE_VIDEOS(220, "getyoutubevideos"),
    ADD_YOUTUBE_VIDEO(221, "addyoutubevideo&uuid="),
    UPDATE_YOUTUBE_VIDEO(222, "updateyoutubevideo&uuid="),
    DELETE_YOUTUBE_VIDEO(223, "deleteyoutubevideo&uuid=");

    private int _id;
    private String _action;

    LucaServerActionTypes(int id, String action) {
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

    public static LucaServerActionTypes GetById(int id) {
        for (LucaServerActionTypes type : values()) {
            if (type._id == id) {
                return type;
            }
        }
        return NULL;
    }
}
