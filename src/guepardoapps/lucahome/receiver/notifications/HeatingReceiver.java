package guepardoapps.lucahome.receiver.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.IDs;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.NotificationService;

import guepardoapps.toolset.controller.BroadcastController;

public class HeatingReceiver extends BroadcastReceiver {

	private static final String TAG = HeatingReceiver.class.getName();
	private LucaHomeLogger _logger;

	@Override
	public void onReceive(Context context, Intent intent) {
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("HeatingReceiver onReceive!");

		Bundle details = intent.getExtras();
		String action = details.getString(Bundles.ACTION);

		ServiceController serviceController = new ServiceController(context);
		serviceController.CloseNotification(IDs.NOTIFICATION_SLEEP);

		MainServiceAction mainServiceAction = MainServiceAction.NULL;

		if (action.contains(NotificationService.HEATING_AND_SOUND)) {
			mainServiceAction = MainServiceAction.ENABLE_HEATING_AND_SOUND;
		} else if (action.contains(NotificationService.HEATING_SINGLE)) {
			mainServiceAction = MainServiceAction.ENABLE_HEATING;
		} else {
			_logger.Error("Action contains errors: " + action);
			Toasty.error(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
		}

		BroadcastController broadcastController = new BroadcastController(context);
		broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
				new String[] { Bundles.MAIN_SERVICE_ACTION }, new Object[] { mainServiceAction });
	}
}