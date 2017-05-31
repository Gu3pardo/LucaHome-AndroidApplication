package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.BroadcastController;

public class TemperatureController {

    private static final String TAG = TemperatureController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private BroadcastController _broadcastController;

    public TemperatureController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _broadcastController = new BroadcastController(context);
    }

    public void ReloadTemperature(@NonNull TemperatureDto temperature) {
        _logger.Debug("Trying to reload temperature: " + temperature.toString());

        switch (temperature.GetTemperatureType()) {
            case RASPBERRY:
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_CURRENT_WEATHER);
                break;
            case CITY:
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_CURRENT_WEATHER);
                break;
            default:
                _logger.Warn(temperature.GetTemperatureType().toString() + " is not supported!");
                break;
        }
    }
}
