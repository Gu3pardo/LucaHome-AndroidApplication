package guepardoapps.lucahome.receiver;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class TimeChangedReceiver extends BroadcastReceiver {

	private static final String TAG = TimeChangedReceiver.class.getName();
	private LucaHomeLogger _logger;

	private static final int NOTIFICATION_HOUR = 21;
	private static final int NOTIFICATION_MINUTE = 30;
	private static final int NOTIFICATION_SECOND = 0;
	private static final int NOTIFICATION_MILLISECOND = 0;

	private ServiceController _serviceController;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}
		_logger.Debug(TAG + " onReceive");

		final String action = intent.getAction();
		if (action.equals(Intent.ACTION_TIME_TICK)) {
			Calendar calendar = Calendar.getInstance();
			if (calendar.get(Calendar.HOUR_OF_DAY) == NOTIFICATION_HOUR) {
				if (calendar.get(Calendar.MINUTE) == NOTIFICATION_MINUTE) {
					if (calendar.get(Calendar.SECOND) == NOTIFICATION_SECOND) {
						if (calendar.get(Calendar.MILLISECOND) == NOTIFICATION_MILLISECOND) {
							_logger.Debug("Showing notification go to sleep!");

							if (_serviceController == null) {
								_serviceController = new ServiceController(context);
								_serviceController.StartNotificationService("", "", -1, LucaObject.GO_TO_BED);
							}
						}
					}
				}
			}
		}
	}
}