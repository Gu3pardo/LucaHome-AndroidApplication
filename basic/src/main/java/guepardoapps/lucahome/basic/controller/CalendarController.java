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
    private static Logger _logger;

    private Context _context;

    public CalendarController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
    }

    public SerializableList<CalendarEntryDto> ReadCalendar(long timeSpanId) {
        _logger.Debug("ReadCalendar");

        SerializableList<CalendarEntryDto> entries = new SerializableList<>();
        HashSet<String> calendarIds = new HashSet<>();

        ContentResolver contentResolver = _context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
                new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventLocation"}, null, null,
                null);
        if (cursor == null) {
            _logger.Error("Cursor is null!");
            return new SerializableList<>();
        }

        try {
            _logger.Debug("Count=" + cursor.getCount());
            if (cursor.getCount() > 0) {
                _logger.Debug("the control is just inside of the cursor. count loop");
                while (cursor.moveToNext()) {
                    String _id = cursor.getString(0);
                    String displayName = cursor.getString(1);

                    _logger.Debug("Id: " + _id + " Display Name: " + displayName);
                    calendarIds.add(_id);
                }
            }
        } catch (Exception ex) {
            _logger.Error(ex.getMessage());
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
                _logger.Error("EventCursor is null!");
                continue;
            }

            _logger.Debug("eventCursor count=" + eventCursor.getCount());
            if (eventCursor.getCount() > 0) {

                if (eventCursor.moveToFirst()) {
                    do {

                        // Properties
                        final String title = eventCursor.getString(0);
                        final Date begin = new Date(eventCursor.getLong(1));
                        final Date end = new Date(eventCursor.getLong(2));
                        final boolean allDay = !eventCursor.getString(3).equals("0");
                        _logger.Debug(
                                String.format("Title: %s; Begin: %s; End: %s; All Day: %s", title, begin, end, allDay));

                        CalendarEntryDto newEntry = new CalendarEntryDto(title, begin, end, allDay);
                        _logger.Debug(String.format("Created new CalendarEntry %s", newEntry.toString()));

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
