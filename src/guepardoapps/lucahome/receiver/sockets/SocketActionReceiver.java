package guepardoapps.lucahome.receiver.sockets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.ServerActions;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.sockets.SocketActionService;

public class SocketActionReceiver extends BroadcastReceiver {

	private static final String TAG = SocketActionReceiver.class.getName();
	private LucaHomeLogger _logger;

	private ServiceController _serviceController;

	@Override
	public void onReceive(Context context, Intent intent) {
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("Received new socket change!");

		Bundle details = intent.getExtras();

		String action = details.getString(Bundles.ACTION);
		if (action.contains("ALL_SOCKETS")) {
			if (_serviceController == null) {
				_serviceController = new ServiceController(context);
			}
			_serviceController.StartRestService("SHOW_NOTIFICATION_SOCKET", ServerActions.DEACTIVATE_ALL_SOCKETS, "",
					LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
		} else if (action.contains("SINGLE_SOCKET")) {
			WirelessSocketDto socket = (WirelessSocketDto) details.getSerializable(Bundles.SOCKET_DATA);
			_logger.Debug("socket: " + socket.toString());

			Intent serviceIntent = new Intent(context, SocketActionService.class);

			Bundle serviceData = new Bundle();
			serviceData.putSerializable(Bundles.SOCKET_DATA, socket);
			serviceIntent.putExtras(serviceData);

			context.startService(serviceIntent);
		} else {
			_logger.Error("Action contains errors: " + action);
			Toast.makeText(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
		}
	}
}