package guepardoapps.lucahome.basic.controller;

import java.util.Date;
import java.util.HashSet;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.dto.CalendarEntryDto;
import guepardoapps.lucahome.basic.utils.Logger;

public class CalendarController {
    private static final String TAG = CalendarController.class.getSimpleName();

    private Context _context;

    public CalendarController(@NonNull Context context) {
        _context = context;
    }

    public SerializableList<CalendarEntryDto> ReadCalendar(long timeSpanId) {
        SerializableList<CalendarEntryDto> entries = new SerializableList<>();
        HashSet<String> calendarIds = new HashSet<>();

        ContentResolver contentResolver = _context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
                new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventLocation"}, null, null,
                null);
        if (cursor == null) {
            Logger.getInstance().Error(TAG, "Cursor is null!");
            return new SerializableList<>();
        }

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String _id = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    calendarIds.add(_id);
                }
            }
        } catch (Exception ex) {
            Logger.getInstance().Error(TAG, ex.getMessage());
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
                Logger.getInstance().Error(TAG, "EventCursor is null!");
                continue;
            }

            if (eventCursor.getCount() > 0) {

                if (eventCursor.moveToFirst()) {
                    do {
                        // Properties
                        final String title = eventCursor.getString(0);
                        final Date begin = new Date(eventCursor.getLong(1));
                        final Date end = new Date(eventCursor.getLong(2));
                        final boolean allDay = !eventCursor.getString(3).equals("0");
                        CalendarEntryDto newEntry = new CalendarEntryDto(title, begin, end, allDay);
                        entries.addValue(newEntry);
                    } while (eventCursor.moveToNext());
                }
            }

            eventCursor.close();
            break;
        }

        return entries;
    }
}
