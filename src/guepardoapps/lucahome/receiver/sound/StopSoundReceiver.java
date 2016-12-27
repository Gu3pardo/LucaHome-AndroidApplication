package guepardoapps.lucahome.receiver.sound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.controller.ServiceController;

public class StopSoundReceiver extends BroadcastReceiver {

	private static final String TAG = StopSoundReceiver.class.getName();
	private LucaHomeLogger _logger;

	private ServiceController _serviceController;

	@Override
	public void onReceive(Context context, Intent intent) {
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("Received stop sounds!");

		if (_serviceController == null) {
			_serviceController = new ServiceController(context);
		}
		_serviceController.StopSound();
	}
}