package guepardoapps.lucahome.common.controller;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.CalendarEntry;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"deprecation", "WeakerAccess"})
public class CalendarController implements ICalendarController {
    private static final String Tag = CalendarController.class.getSimpleName();

    private Context _context;

    public CalendarController(@NonNull Context context) {
        _context = context;
    }

    @Override
    public ArrayList<CalendarEntry> ReadCalendar(long timeSpanId) {
        Logger.getInstance().Debug(Tag, "ReadCalendar");

        ArrayList<CalendarEntry> entries = new ArrayList<>();
        HashSet<String> calendarIds = new HashSet<>();

        ContentResolver contentResolver = _context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"), new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventLocation"}, null, null, null);
        if (cursor == null) {
            Logger.getInstance().Error(Tag, "Cursor is null!");
            return new ArrayList<>();
        }

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String _id = cursor.getString(0);
                    calendarIds.add(_id);
                }
            }
        } catch (Exception ex) {
            Logger.getInstance().Error(Tag, "Outer: " + ex.toString());
        } finally {
            cursor.close();
        }

        for (String id : calendarIds) {
            Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
            long now = new Date().getTime();

            ContentUris.appendId(builder, now - timeSpanId);
            ContentUris.appendId(builder, now + timeSpanId);

            Cursor eventCursor = contentResolver.query(builder.build(),
                    new String[]{"title", "begin", "end", "allDay"},
                    CalendarContract.Instances.CALENDAR_ID + "=" + id, null, "startDay ASC, startMinute ASC");

            if (eventCursor == null) {
                Logger.getInstance().Error(Tag, "EventCursor is null!");
                continue;
            }

            Logger.getInstance().Debug(Tag, String.format(Locale.getDefault(), "eventCursor count: %d", eventCursor.getCount()));

            if (eventCursor.getCount() > 0) {
                int calendarId = 0;
                if (eventCursor.moveToFirst()) {
                    do {
                        // Properties
                        final String title = eventCursor.getString(TitleIndex);
                        final Date startDate = new Date(eventCursor.getLong(StartDateIndex));

                        Calendar start = Calendar.getInstance();
                        start.set(startDate.getYear(), startDate.getMonth(), startDate.getDay(), startDate.getHours(), startDate.getMinutes(), startDate.getSeconds());

                        final Date endDate = new Date(eventCursor.getLong(EndDateIndex));
                        Calendar end = Calendar.getInstance();
                        end.set(endDate.getYear(), endDate.getMonth(), endDate.getDay(), endDate.getHours(), endDate.getMinutes(), endDate.getSeconds());

                        final boolean allDay = !eventCursor.getString(AllDayIndex).equals("0");

                        CalendarEntry newEntry = new CalendarEntry(UUID.fromString(String.valueOf(calendarId)), title, start, end, allDay);
                        entries.add(newEntry);

                        calendarId++;
                    } while (eventCursor.moveToNext());
                }
            }

            eventCursor.close();
        }

        entries.sort(Comparator.comparing(CalendarEntry::GetStartDate));
        return entries;
    }
}
