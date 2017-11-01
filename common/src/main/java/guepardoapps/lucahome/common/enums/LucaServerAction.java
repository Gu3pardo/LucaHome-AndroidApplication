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
    GET_CHANGES(30, "getchangesrest"),

    //COINS
    GET_COINS_ALL(130, "getcoinsall"),
    GET_COINS_USER(131, "getcoinsuser"),
    ADD_COIN(132, "addcoin&id="),
    UPDATE_COIN(133, "updatecoin&id="),
    DELETE_COIN(134, "deletecoin&id="),

    //INFORMATION
    GET_INFORMATIONS(40, "getinformationsrest"),

    //MAP_CONTENT
    GET_MAP_CONTENTS(50, "getmapcontents"),
    ADD_MAP_CONTENT(51, "addmapcontent&id="),
    UPDATE_MAP_CONTENT(52, "updatemapcontent&id="),
    DELETE_MAP_CONTENT(53, "deletemapcontent&id="),

    //MENU
    GET_MENU(60, "getmenu"),
    UPDATE_MENU(61, "updatemenu&weekday="),
    CLEAR_MENU(62, "clearmenu&weekday="),
    GET_LISTED_MENU(63, "getlistedmenu"),

    //MOVIE
    GET_MOVIES(70, "getmovies"),
    START_MOVIE(71, "startmovie&title="),
    UPDATE_MOVIE(72, "updatemovie&title="),
    GET_MOVIES_REDUCED(73, "getmoviesreduced"),

    //SCHEDULES
    GET_SCHEDULES(80, "getschedules"),
    SET_SCHEDULE(81, "setschedule&schedule="),
    ADD_SCHEDULE(82, "addschedule&name="),
    UPDATE_SCHEDULE(83, "updateschedule&name="),
    DELETE_SCHEDULE(84, "deleteschedule&schedule="),

    //SHOPPING_LIST
    GET_SHOPPING_LIST(90, "getshoppinglist"),
    ADD_SHOPPING_ENTRY_F(91, "addshoppingentry&id=%d&name=%s&group=%s&quantity=%d"),
    UPDATE_SHOPPING_ENTRY_F(92, "updateshoppingentry&id=%d&name=%s&group=%s&quantity=%d"),
    DELETE_SHOPPING_ENTRY_F(93, "deleteshoppingentry&id=%d"),

    //SOCKETS
    GET_SOCKETS(100, "getsockets"),
    SET_SOCKET(102, "setsocket&socket="),
    ADD_SOCKET(103, "addsocket&name="),
    UPDATE_SOCKET(104, "updatesocket&name="),
    DELETE_SOCKET(105, "deletesocket&socket="),
    DEACTIVATE_ALL_SOCKETS(106, "deactivateAllSockets"),

    //TEMPERATURE
    GET_TEMPERATURES(110, "getcurrenttemperaturerest"),

    //USER
    VALIDATE_USER(120, "validateuser");

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
