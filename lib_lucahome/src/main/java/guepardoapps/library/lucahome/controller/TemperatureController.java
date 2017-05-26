package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
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
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.MAIN_SERVICE_COMMAND,
                        new String[]{Bundles.MAIN_SERVICE_ACTION},
                        new Object[]{MainServiceAction.DOWNLOAD_TEMPERATURE});
                break;
            case CITY:
                _broadcastController.SendSerializableArrayBroadcast(
                        Broadcasts.MAIN_SERVICE_COMMAND,
                        new String[]{Bundles.MAIN_SERVICE_ACTION},
                        new Object[]{MainServiceAction.DOWNLOAD_WEATHER_CURRENT});
                break;
            default:
                _logger.Warn(temperature.GetTemperatureType().toString() + " is not supported!");
                break;
        }
    }
}
