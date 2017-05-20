package guepardoapps.lucahome.views.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.format.Time;
import android.view.Display;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.Tools;

import guepardoapps.lucahome.common.constants.CanvasConstants;

@SuppressWarnings("deprecation")
public class DateViewController {

    private static final String TAG = DateViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Tools _tools;

    private Display _display;

    public DateViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);

        _tools = new Tools(context);
        _display = _tools.GetDisplayDimension();
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
    }

    public void DrawDate(Canvas canvas, Time time, int color) {
        _logger.Debug("drawDate");

        Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_SMALL);
        paint.setColor(color);

        String dayString = String.valueOf(time.monthDay);
        dayString = _tools.CheckLength(dayString);
        String monthString = String.valueOf(time.month + 1);
        monthString = _tools.CheckLength(monthString);
        String yearString = String.valueOf(time.year);

        String dateString = dayString + "." + monthString + "." + yearString;
        _logger.Info("dateString: " + dateString);

        canvas.drawText(dateString, _display.getWidth() - 130, 30, paint);
    }

    public void DrawWeekOfYear(Canvas canvas, Time time, int color) {
        _logger.Debug("drawWeekOfYear");

        Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_SMALL);
        paint.setColor(color);

        String weekOfYearString = String.valueOf(time.getWeekNumber());
        _logger.Info("weekOfYearString: " + weekOfYearString);

        canvas.drawText("Week", _display.getWidth() - 65, 60, paint);
        canvas.drawText(weekOfYearString, _display.getWidth() - 37, 90, paint);
    }

    public void DrawTime(Canvas canvas, Time time, int color) {
        _logger.Debug("drawTime");

        Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_MEDIUM);
        paint.setColor(color);

        String hourString = String.valueOf(time.hour);
        hourString = _tools.CheckLength(hourString);
        String minuteString = String.valueOf(time.minute);
        minuteString = _tools.CheckLength(minuteString);

        String timeString = hourString + ":" + minuteString;
        _logger.Info("timeString: " + timeString);

        canvas.drawText(timeString, (_display.getWidth() / 3), (_display.getHeight() / 3) + 15, paint);
    }
}
