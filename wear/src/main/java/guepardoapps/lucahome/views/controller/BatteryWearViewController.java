package guepardoapps.lucahome.views.controller;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.view.Display;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.Tools;

import guepardoapps.library.toolset.controller.DisplayController;
import guepardoapps.lucahome.common.constants.CanvasConstants;

@SuppressWarnings("deprecation")
public class BatteryWearViewController {

    private static final String TAG = BatteryWearViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private Tools _tools;

    private Display _display;

    public BatteryWearViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);

        _context = context;
        _tools = new Tools();

        _display = new DisplayController(_context).GetDisplayDimension();
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
    }

    public void Draw(Canvas canvas, int color) {
        _logger.Debug("Draw");

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = _context.registerReceiver(null, intentFilter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level != -1) {
                String battery = String.valueOf(level) + "%";

                Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_EXTREMELY_SMALL);
                paint.setColor(color);
                canvas.drawText("Bat W", _display.getWidth() - 55, 225, paint);
                canvas.drawText(battery, _display.getWidth() - 55, 242, paint);
            } else {
                _logger.Warn("Level is -1!");
            }
        } else {
            _logger.Error("BatteryStatus is null!");
        }
    }
}
