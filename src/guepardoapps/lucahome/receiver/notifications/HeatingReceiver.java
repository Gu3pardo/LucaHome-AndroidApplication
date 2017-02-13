package guepardoapps.lucahome.receiver.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.MainService;
import guepardoapps.lucahome.services.NotificationService;

public class HeatingReceiver extends BroadcastReceiver {

	private static final String TAG = HeatingReceiver.class.getName();
	private LucaHomeLogger _logger;

	@Override
	public void onReceive(Context context, Intent intent) {
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("HeatingReceiver onReceive!");

		Bundle details = intent.getExtras();
		String action = details.getString(Bundles.ACTION);

		if (action.contains(NotificationService.HEATING_AND_SOUND)) {
			Intent startMainService = new Intent(context, MainService.class);
			Bundle mainServiceBundle = new Bundle();
			mainServiceBundle.putSerializable(Bundles.MAIN_SERVICE_ACTION, MainServiceAction.ENABLE_HEATING_AND_SOUND);
			startMainService.putExtras(mainServiceBundle);
			context.startService(startMainService);
		} else if (action.contains(NotificationService.HEATING_SINGLE)) {
			Intent startMainService = new Intent(context, MainService.class);
			Bundle mainServiceBundle = new Bundle();
			mainServiceBundle.putSerializable(Bundles.MAIN_SERVICE_ACTION, MainServiceAction.ENABLE_HEATING);
			startMainService.putExtras(mainServiceBundle);
			context.startService(startMainService);
		} else {
			_logger.Error("Action contains errors: " + action);
			Toast.makeText(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
		}
	}
}