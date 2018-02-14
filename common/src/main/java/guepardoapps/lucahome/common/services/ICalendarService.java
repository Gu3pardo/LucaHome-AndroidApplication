package guepardoapps.lucahome.common.services;

import guepardoapps.lucahome.common.classes.CalendarEntry;

public interface ICalendarService extends ILucaService<CalendarEntry> {
    String CalendarLoadFinishedBroadcast = "guepardoapps.lucahome.common.services.calendar.load.finished";

    String CalendarLoadFinishedBundle = "CalendarLoadFinishedBundle";
}
