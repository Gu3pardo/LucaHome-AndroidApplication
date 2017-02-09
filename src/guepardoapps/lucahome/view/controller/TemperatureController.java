package guepardoapps.lucahome.view.controller;

import android.content.Context;

import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.toolset.controller.BroadcastController;

public class TemperatureController {

	private static final String TAG = TemperatureController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private BroadcastController _broadcastController;

	public TemperatureController(Context context) {
		_context = context;

		_logger = new LucaHomeLogger(TAG);
		_broadcastController = new BroadcastController(_context);
	}

	public void ReloadTemperature(TemperatureDto temperature) {
		_logger.Debug("Trying to reload temperature: " + temperature.toString());

		switch (temperature.GetTemperatureType()) {
		case RASPBERRY:
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
					new String[] { Bundles.MAIN_SERVICE_ACTION },
					new Object[] { MainServiceAction.DOWNLOAD_TEMPERATURE });
			break;
		case CITY:
			_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
					new String[] { Bundles.MAIN_SERVICE_ACTION },
					new Object[] { MainServiceAction.DOWNLOAD_WEATHER_CURRENT });
			break;
		default:
			_logger.Warn(temperature.GetTemperatureType().toString() + " is not supported!");
			break;
		}
	}
}
