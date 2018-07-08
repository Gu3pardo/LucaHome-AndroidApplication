package guepardoapps.lucahome.common.enums.common

import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.user.UserRole
import java.io.Serializable

enum class ServerAction(var id: Int, var command: String) : Serializable {

    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.No)
    NULL(0, ""),

    //CHANGE
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    ChangeGet(70, "changeget"),

    //PUCK JS
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    PuckJsGet(60, "puckjsget"),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    PuckJsAdd(61, "puckjsadd&uuid=%s&name=%s&roomuuid=%s&mac=%s"),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    PuckJsUpdate(62, "puckjsupdate&uuid=%s&roomuuid=%s&name=%s&mac=%s"),
    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    PuckJsDelete(63, "puckjsdelete&uuid=%s"),

    //ROOM
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    RoomGet(50, "roomget"),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    RoomAdd(51, "roomadd&uuid="),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    RoomUpdate(52, "roomupdate&uuid="),
    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    RoomDelete(53, "roomdelete&uuid="),

    //TEMPERATURE
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    TemperatureGet(40, "temperatureget"),

    //USER
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    UserValidate(10, "uservalidate"),
    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    UserAdd(11, "useradd&uuid="),
    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    UserUpdate(12, "userupdate&uuid="),
    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    UserDelete(13, "userdelete&uuid="),

    //WIRELESS SOCKETS
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSocketGet(20, "wirelesssocketget"),
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSocketSet(21, "wirelesssocketset&uuid="),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSocketAdd(22, "wirelesssocketadd&uuid="),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSocketUpdate(23, "wirelesssocketupdate&uuid="),
    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSocketDelete(24, "wirelesssocketdelete&uuid="),
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSocketDeactivateAll(25, "wirelesssocketdeactivateall"),

    //WIRELESS SWITCHES
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSwitchGet(30, "wirelessswitchget"),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSwitchAdd(31, "wirelessswitchadd&uuid="),
    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSwitchUpdate(32, "wirelessswitchupdate&uuid="),
    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSwitchDelete(33, "wirelessswitchdelete&uuid="),
    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    WirelessSwitchToggle(34, "wirelessswitchtoggle&uuid="),

    /*

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

    //MEDIA SERVER
    GET_MEDIA_SERVER(160, "getmediaserver"),
    ADD_MEDIA_SERVER(161, "addmediaserver&uuid="),
    UPDATE_MEDIA_SERVER(162, "updatemediaserver&uuid="),
    DELETE_MEDIA_SERVER(163, "deletemediaserver&uuid="),

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
    DELETE_YOUTUBE_VIDEO(223, "deleteyoutubevideo&uuid="),

    //WIRELESS TIMER
    GET_WIRELESS_TIMER(230, "getwirelesstimer"),
    ADD_WIRELESS_TIMER(231, "addwirelesstimer&uuid="),
    UPDATE_WIRELESS_TIMER(232, "updatewirelesstimer&uuid="),
    DELETE_WIRELESS_TIMER(233, "deletewirelesstimer&uuid=")

    */
}
