package guepardoapps.lucahome.views.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.Tools;

import guepardoapps.lucahome.common.constants.CanvasConstants;

public class StepViewController implements SensorEventListener {

    private static final String TAG = StepViewController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Tools _tools;

    private double _stepCount;
    private SensorManager _sensorManager;

    public StepViewController(Context context) {
        _logger = new LucaHomeLogger(TAG);

        _tools = new Tools();

        _stepCount = 2130;
        _sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor stepSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            _logger.Debug("Found StepSensor!");
            _sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            _logger.Warn("Sensor is null!");
        }
    }

    public void onDestroy() {
        _sensorManager.unregisterListener(this);
    }

    public void Draw(Canvas canvas, int color) {
        _logger.Debug("Draw");
        Paint paint = _tools.CreateDefaultPaint(canvas, CanvasConstants.TEXT_SIZE_VERY_SMALL);
        paint.setColor(color);
        canvas.drawText("STEPS", CanvasConstants.DRAW_DEFAULT_OFFSET_X, 25, paint);
        canvas.drawText(String.valueOf((int) _stepCount), CanvasConstants.DRAW_DEFAULT_OFFSET_X, 50, paint);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_STEP_COUNTER:
                _stepCount = event.values[0];
                _logger.Info("New stepCount: " + String.valueOf(_stepCount));
                break;
            default:
                _logger.Info("Currently not supported: " + event.sensor.toString());
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int arg1) {
    }
}
