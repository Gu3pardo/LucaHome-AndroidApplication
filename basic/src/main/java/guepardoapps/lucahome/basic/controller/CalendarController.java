package guepardoapps.lucahome.basic.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.dto.CalendarEntryDto;
import guepardoapps.lucahome.basic.utils.Logger;

public class CalendarController {
    private static final String TAG = CalendarController.class.getSimpleName();

    private Context _context;

    public CalendarController(@NonNull Context context) {
        _context = context;
    }

    public ArrayList<CalendarEntryDto> ReadCalendar(long timeSpanId) {
        Logger.getInstance().Debug(TAG, "ReadCalendar");

        ArrayList<CalendarEntryDto> entries = new ArrayList<>();
        HashSet<String> calendarIds = new HashSet<>();

        ContentResolver contentResolver = _context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"), new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventLocation"}, null, null, null);
        if (cursor == null) {
            Logger.getInstance().Error(TAG, "Cursor is null!");
            return new ArrayList<>();
        }

        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Cursor count: %d", cursor.getCount()));

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String _id = cursor.getString(0);
                    calendarIds.add(_id);
                }
            }
        } catch (Exception ex) {
            Logger.getInstance().Error(TAG, "Outer: " + ex.toString());
        } finally {
            cursor.close();
        }

        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Size of calendarIds is %d", calendarIds.size()));

        for (String id : calendarIds) {
            Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "CalendarId: %s", id));

            Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
            long now = new Date().getTime();

            ContentUris.appendId(builder, now - timeSpanId);
            ContentUris.appendId(builder, now + timeSpanId);

            Cursor eventCursor = contentResolver.query(builder.build(),
                    new String[]{"title", "begin", "end", "allDay"},
                    CalendarContract.Instances.CALENDAR_ID + "=" + id, null, "startDay ASC, startMinute ASC");

            if (eventCursor == null) {
                Logger.getInstance().Error(TAG, "EventCursor is null!");
                continue;
            }

            Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "eventCursor count: %d", eventCursor.getCount()));

            if (eventCursor.getCount() > 0) {
                if (eventCursor.moveToFirst()) {
                    do {
                        // Properties
                        final String title = eventCursor.getString(0);
                        final Date begin = new Date(eventCursor.getLong(1));
                        final Date end = new Date(eventCursor.getLong(2));
                        final boolean allDay = !eventCursor.getString(3).equals("0");
                        CalendarEntryDto newEntry = new CalendarEntryDto(title, begin, end, allDay);
                        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Adding new calendar entry %s", newEntry));
                        entries.add(newEntry);
                    } while (eventCursor.moveToNext());
                }
            }

            eventCursor.close();
        }

        Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Size of entry list is %d", entries.size()));
        entries.sort(Comparator.comparing(CalendarEntryDto::GetBegin));

        return entries;
    }
}
