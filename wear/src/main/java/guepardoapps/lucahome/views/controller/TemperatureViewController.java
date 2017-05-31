package guepardoapps.lucahome.views.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Display;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.enums.TemperatureType;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.Tools;
import guepardoapps.library.toolset.controller.DisplayController;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.common.constants.CanvasConstants;

@SuppressWarnings("deprecation")
public class TemperatureViewController {

    private static final String TAG = TemperatureViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private ReceiverController _receiverController;
    private Tools _tools;

    private Display _display;

    private boolean _isHomeNetwork = true;
    private TemperatureDto _raspberryTemperature;

    private BroadcastReceiver _temperatureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_temperatureReceiver onReceive");

            TemperatureDto newRaspberryTemperature = (TemperatureDto) intent
                    .getSerializableExtra(Bundles.RASPBERRY_TEMPERATURE);
            if (newRaspberryTemperature != null) {
                _raspberryTemperature = newRaspberryTemperature;
                _logger.Debug("New _raspberryTemperature: " + _raspberryTemperature.toString());
            } else {
                _logger.Warn("newRaspberryTemperature is null!");
            }
        }
    };

    private final BroadcastReceiver _wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_wifiStateReceiver onReceive");
            String wifiState = intent.getStringExtra(Bundles.WIFI_STATE);
            if (wifiState != null) {
                if (wifiState.contains("HOME")) {
                    _isHomeNetwork = true;
                } else if (wifiState.contains("NO")) {
                    _isHomeNetwork = false;
                } else {
                    _isHomeNetwork = false;
                    _logger.Warn("wifiState not supported: " + wifiState);
                }
            } else {
                _logger.Warn("wifiState is null!");
            }
        }
    };

    public TemperatureViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);

        _receiverController = new ReceiverController(context);
        _receiverController.RegisterReceiver(_temperatureReceiver,
                new String[]{Broadcasts.UPDATE_RASPBERRY_TEMPERATURE});
        _receiverController.RegisterReceiver(_wifiStateReceiver, new String[]{Broadcasts.UPDATE_WIFI_STATE});
        _tools = new Tools();

        _display = new DisplayController(context).GetDisplayDimension();

        _raspberryTemperature = new TemperatureDto(18.9, "Workspace Jonas", null, "", TemperatureType.NULL, "");
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
    }

    public void Draw(Canvas canvas, int color) {
        _logger.Debug("Draw");
        if (!_isHomeNetwork) {
            _logger.Debug("No home network available!");
            return;
        }
        Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_EXTREMELY_SMALL);
        paint.setColor(color);
        canvas.drawText(_raspberryTemperature.GetWatchFaceText(), CanvasConstants.DRAW_DEFAULT_OFFSET_X,
                _display.getHeight() - 90, paint);
    }
}
