package guepardoapps.library.lucahome.controller;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;

public class MenuController {

    private static final String TAG = MenuController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private BroadcastController _broadcastController;
    private ServiceController _serviceController;

    public MenuController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);

        _broadcastController = new BroadcastController(context);
        _serviceController = new ServiceController(context);
    }

    public void CheckMenuDto(@NonNull SerializableList<MenuDto> menuList) {
        _logger.Debug("CheckMenuDto");

        boolean reload = false;

        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);

        for (int index = 0; index < menuList.getSize(); index++) {
            MenuDto menu = menuList.getValue(index);
            _logger.Debug(String.format("Checking menu %s", menu.toString()));

            if (menu.GetYear() < year) {
                _logger.Debug(String.format("Year of menu %s is lower then this year! Updating...", menu.toString()));

                menu = UpdateDate(menu);
                if (menu == null) {
                    continue;
                }
                menu.SetTitle("-");
                menu.SetDescription("-");
                _serviceController.StartRestService(Bundles.MENU, menu.GetCommandUpdate(), "", LucaObject.MENU,
                        RaspberrySelection.BOTH);
                reload = true;
                continue;
            }

            if (menu.GetMonth() < month) {
                _logger.Debug(String.format("Month of menu %s is lower then this year! Updating...", menu.toString()));

                menu = UpdateDate(menu);
                if (menu == null) {
                    continue;
                }
                menu.SetTitle("-");
                menu.SetDescription("-");
                _serviceController.StartRestService(Bundles.MENU, menu.GetCommandUpdate(), "", LucaObject.MENU,
                        RaspberrySelection.BOTH);
                reload = true;
                continue;
            }

            if (menu.GetDay() < dayOfMonth && menu.GetMonth() <= month) {
                _logger.Debug(String.format(
                        "Day of menu %s is lower then this day and month is lower then this month! Updating...",
                        menu.toString()));

                menu = UpdateDate(menu);
                if (menu == null) {
                    continue;
                }
                menu.SetTitle("-");
                menu.SetDescription("-");
                _serviceController.StartRestService(Bundles.MENU, menu.GetCommandUpdate(), "", LucaObject.MENU,
                        RaspberrySelection.BOTH);
                reload = true;
            }
        }

        if (reload) {
            _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_MENU);
        }
    }

    @SuppressLint("DefaultLocale")
    public String GetDateString(@NonNull MenuDto menu) {
        String weekendString = menu.GetWeekday().substring(0, 2);
        String dateString = String.format("%2d.%2d.%4d", menu.GetDay(), menu.GetMonth(), menu.GetYear());
        return String.format("%s, %s", weekendString, dateString);
    }

    public MenuDto UpdateDate(@NonNull MenuDto menu) {
        _logger.Debug(String.format("Updating menu %s", menu.toString()));

        Calendar today = Calendar.getInstance();

        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        int menuDayOfWeek;

        String weekday = menu.GetWeekday();
        if (weekday.startsWith("mo")) {
            menuDayOfWeek = 2;
        } else if (weekday.startsWith("tu")) {
            menuDayOfWeek = 3;
        } else if (weekday.startsWith("we")) {
            menuDayOfWeek = 4;
        } else if (weekday.startsWith("th")) {
            menuDayOfWeek = 5;
        } else if (weekday.startsWith("fr")) {
            menuDayOfWeek = 6;
        } else if (weekday.startsWith("sa")) {
            menuDayOfWeek = 7;
        } else if (weekday.startsWith("su")) {
            menuDayOfWeek = 1;
        } else {
            menuDayOfWeek = 0;
        }

        if (menuDayOfWeek <= 0) {
            _logger.Error("Day of week was not found!");
            return null;
        }

        int dayOfWeekDifference = menuDayOfWeek - dayOfWeek;
        if (dayOfWeekDifference < 0) {
            dayOfWeekDifference += 7;
        }

        if (menu.GetYear() < year) {
            return getMenuDto(menu, year, month, dayOfMonth, dayOfWeekDifference);
        }

        if (menu.GetMonth() < month) {
            return getMenuDto(menu, year, month, dayOfMonth, dayOfWeekDifference);
        }

        if (menu.GetDay() < dayOfMonth) {
            return getMenuDto(menu, year, month, dayOfMonth, dayOfWeekDifference);
        }

        return menu;
    }

    private MenuDto getMenuDto(
            @NonNull MenuDto menu,
            int year,
            int month,
            int dayOfMonth,
            int dayOfWeekDifference) {
        dayOfMonth += dayOfWeekDifference;

        switch (month - 1) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
                if (dayOfMonth > 31) {
                    dayOfMonth -= 31;
                    month++;
                }
                break;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                if (dayOfMonth > 30) {
                    dayOfMonth -= 30;
                    month++;
                }
                break;
            case Calendar.FEBRUARY:
                if (year % 4 == 0) {
                    if (dayOfMonth > 29) {
                        dayOfMonth -= 29;
                        month++;
                    }
                } else {
                    if (dayOfMonth > 28) {
                        dayOfMonth -= 28;
                        month++;
                    }
                }
                break;
            case Calendar.DECEMBER:
                if (dayOfMonth > 31) {
                    dayOfMonth -= 31;
                    month = 1;
                    year++;
                }
                break;
            default:
                return null;
        }

        menu.SetDay(dayOfMonth);
        menu.SetMonth(month);
        menu.SetYear(year);

        menu.SetNeedsUpdate(true);

        return menu;
    }
}
