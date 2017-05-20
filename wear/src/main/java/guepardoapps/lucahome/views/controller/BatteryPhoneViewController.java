package guepardoapps.lucahome.views.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Display;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.Tools;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.common.constants.CanvasConstants;

@SuppressWarnings("deprecation")
public class BatteryPhoneViewController {

    private static final String TAG = BatteryPhoneViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private ReceiverController _receiverController;
    private Tools _tools;

    private Display _display;

    private String _batteryPhoneValue;

    private BroadcastReceiver _batteryPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_batteryPhoneReceiver onReceive");
            String message = intent.getStringExtra(Bundles.PHONE_BATTERY);
            if (message != null) {
                _batteryPhoneValue = message;
            }
        }
    };

    public BatteryPhoneViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);

        _receiverController = new ReceiverController(context);
        _receiverController.RegisterReceiver(_batteryPhoneReceiver, new String[]{Broadcasts.UPDATE_PHONE_BATTERY});
        _tools = new Tools(context);

        _display = _tools.GetDisplayDimension();

        _batteryPhoneValue = "94%";
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }

    public void Draw(Canvas canvas, int color) {
        _logger.Debug("Draw");
        Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_EXTREMELY_SMALL);
        paint.setColor(color);
        canvas.drawText("Bat P", _display.getWidth() - 55, 190, paint);
        canvas.drawText(_batteryPhoneValue, _display.getWidth() - 55, 207, paint);
    }
}
