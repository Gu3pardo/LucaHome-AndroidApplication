package guepardoapps.lucahome.common.controller;

import java.util.ArrayList;

public interface ICalendarController {
    int TitleIndex = 0;
    int StartDateIndex = 1;
    int EndDateIndex = 2;
    int AllDayIndex = 3;

    ArrayList<CalendarEntry> ReadCalendar(long timeSpanId);
}
