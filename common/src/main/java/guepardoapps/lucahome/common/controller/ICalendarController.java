package guepardoapps.lucahome.common.controller;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.CalendarEntry;

public interface ICalendarController {
    int TitleIndex = 0;
    int StartDateIndex = 1;
    int EndDateIndex = 2;
    int AllDayIndex = 3;

    ArrayList<CalendarEntry> ReadCalendar(long timeSpanId);
}
