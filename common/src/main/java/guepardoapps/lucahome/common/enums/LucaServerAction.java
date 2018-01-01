package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum LucaServerAction implements Serializable {

    NULL(0, ""),

    //BIRTHDAYS
    GET_BIRTHDAYS(10, "getbirthdays"),
    ADD_BIRTHDAY(11, "addbirthday&id="),
    UPDATE_BIRTHDAY(12, "updatebirthday&id="),
    DELETE_BIRTHDAY(13, "deletebirthday&id="),

    //CAMERA
    START_MOTION(20, "startmotion"),
    STOP_MOTION(21, "stopmotion"),
    GET_MOTION_DATA(22, "getmotiondata"),
    SET_MOTION_CONTROL_TASK(23, "setcontroltaskcamera&state="),

    //CHANGE
    GET_CHANGES(30, "getchanges"),

    //COINS
    GET_COINS_ALL(130, "getcoinsall"),
    GET_COINS_USER(131, "getcoinsuser"),
    ADD_COIN(132, "addcoin&id="),
    UPDATE_COIN(133, "updatecoin&id="),
    DELETE_COIN(134, "deletecoin&id="),

    //INFORMATION
    GET_INFORMATIONS(40, "getinformations"),

    //LISTED MENU
    GET_LISTEDMENU(50, "getlistedmenu"),
    ADD_LISTEDMENU(51, "addlistedmenu&id="),
    UPDATE_LISTEDMENU(52, "updatelistedmenu&id="),
    DELETE_LISTEDMENU(53, "deletelistedmenu&id="),

    //MAP_CONTENT
    GET_MAP_CONTENTS(60, "getmapcontents"),
    ADD_MAP_CONTENT(61, "addmapcontent&id="),
    UPDATE_MAP_CONTENT(62, "updatemapcontent&id="),
    DELETE_MAP_CONTENT(63, "deletemapcontent&id="),

    //MENU
    GET_MENU(54, "getmenu"),
    UPDATE_MENU(55, "updatemenu&weekday="),
    CLEAR_MENU(56, "clearmenu&weekday="),

    //MOVIE
    GET_MOVIES(70, "getmovies"),
    START_MOVIE(71, "startmovie&id="),
    UPDATE_MOVIE(72, "updatemovie&id="),

    //SCHEDULES
    GET_SCHEDULES(80, "getschedules"),
    SET_SCHEDULE(81, "setschedule&id="),
    ADD_SCHEDULE(82, "addschedule&id="),
    UPDATE_SCHEDULE(83, "updateschedule&id="),
    DELETE_SCHEDULE(84, "deleteschedule&id="),

    //SHOPPING_LIST
    GET_SHOPPING_LIST(90, "getshoppinglist"),
    ADD_SHOPPING_ENTRY_F(91, "addshoppingentry&id=%d&name=%s&group=%s&quantity=%d&unit=%s"),
    UPDATE_SHOPPING_ENTRY_F(92, "updateshoppingentry&id=%d&name=%s&group=%s&quantity=%d&unit=%s"),
    DELETE_SHOPPING_ENTRY_F(93, "deleteshoppingentry&id=%d"),

    //SOCKETS
    GET_SOCKETS(100, "getsockets"),
    SET_SOCKET(102, "setsocket&socket="),
    ADD_SOCKET(103, "addsocket&typeid="),
    UPDATE_SOCKET(104, "updatesocket&typeid="),
    DELETE_SOCKET(105, "deletesocket&typeid="),
    DEACTIVATE_ALL_SOCKETS(106, "deactivateAllSockets"),

    //SWITCHES
    GET_SWITCHES(110, "getswitches"),
    ADD_SWITCH(111, "addswitch&id="),
    UPDATE_SWITCH(112, "updateswitch&id="),
    DELETE_SWITCH(113, "deleteswitch&id="),
    TOGGLE_SWITCH(114, "toggleswitch&name="),
    TOGGLE_ALL_SWITCHES(115, "toggleallswitches"),

    //METER DATA
    GET_METER_DATA(120, "getmeterdata"),
    ADD_METER_DATA(121, "addmeterdata&id="),
    UPDATE_METER_DATA(122, "updatemeterdata&id="),
    DELETE_METER_DATA(123, "deletemeterdata&id="),

    //MONEY METER DATA
    GET_MONEY_METER_DATA_ALL(130, "getmoneymeterdataall"),
    GET_MONEY_METER_DATA_USER(131, "getmoneymeterdatauser"),
    ADD_MONEY_METER_DATA(132, "addmoneymeterdata&id="),
    UPDATE_MONEY_METER_DATA(133, "updatemoneymeterdata&id="),
    DELETE_MONEY_METER_DATA(134, "deletemoneymeterdata&id="),

    //TEMPERATURE
    GET_TEMPERATURES(140, "getcurrenttemperature"),

    //USER
    VALIDATE_USER(150, "validateuser");

    private int _id;
    private String _action;

    LucaServerAction(int id, String action) {
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

    public static LucaServerAction GetById(int id) {
        for (LucaServerAction e : values()) {
            if (e._id == id) {
                return e;
            }
        }
        return NULL;
    }
}
