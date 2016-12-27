package guepardoapps.lucahome.wearcontrol.services;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.content.Context;
import android.os.Bundle;

import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.viewcontroller.ScheduleController;
import guepardoapps.lucahome.viewcontroller.SocketController;

import guepardoapps.toolset.controller.BroadcastController;

public class PhoneMessageListenerService extends WearableListenerService
		implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

	private static final String TAG = PhoneMessageListenerService.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;

	private BroadcastController _broadcastController;
	private ScheduleController _scheduleController;
	private SocketController _socketController;

	private static final String PHONE_MESSAGE_PATH = "/phone_message";
	private GoogleApiClient _apiClient;

	@Override
	public void onCreate() {
		super.onCreate();
		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_scheduleController = new ScheduleController(_context);
		_socketController = new SocketController(_context);

		initGoogleApiClient();
	}

	@Override
	public void onDestroy() {
		_logger.Debug("onDestroy");
		if (_apiClient != null) {
			_apiClient.unregisterConnectionCallbacks(this);
			Wearable.MessageApi.removeListener(_apiClient, this);
			if (_apiClient.isConnected()) {
				_apiClient.disconnect();
			}
		}
		super.onDestroy();
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		_logger.Debug("onMessageReceived");
		if (messageEvent.getPath().equalsIgnoreCase(PHONE_MESSAGE_PATH)) {
			String message = new String(messageEvent.getData());
			if (message != null) {
				_logger.Debug("message: " + message);

				String[] data = message.split("\\:");
				if (data[0] == null) {
					_logger.Warn("data[0] is null!");
					return;
				}

				if (data[0].contains("ACTION")) {
					if (data[1] == null) {
						_logger.Warn("data[1] is null!");
						return;
					}

					if (data[1].contains("GET")) {
						if (data[2] == null) {
							_logger.Warn("data[2] is null!");
							return;
						}

						if (data[2].contains("BIRTHDAYS")) {
							_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
									Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.GET_BIRTHDAYS);
						} else if (data[2].contains("MOVIES")) {
							_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
									Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.GET_MOVIES);
						} else if (data[2].contains("SCHEDULES")) {
							_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
									Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.GET_SCHEDULES);
						} else if (data[2].contains("SOCKETS")) {
							_broadcastController.SendSerializableBroadcast(Constants.BROADCAST_MAIN_SERVICE_COMMAND,
									Constants.BUNDLE_MAIN_SERVICE_ACTION, MainServiceAction.GET_SOCKETS);
						} else {
							_logger.Warn("data[2] not supported: " + data[2]);
						}
					} else if (data[1].contains("SET")) {
						if (data[2] == null) {
							_logger.Warn("data[2] is null!");
							return;
						}

						if (data[2].contains("SCHEDULE")) {
							if (data[3] != null && data[4] != null) {
								_scheduleController.SetSchedule(data[3], data[4].contains("1"));
							} else {
								_logger.Warn("data[3] or data[4] is null!");
							}
						} else if (data[2].contains("SOCKET")) {
							if (data[3] != null && data[4] != null) {
								_socketController.SetSocket(data[3], data[4].contains("1"));
							} else {
								_logger.Warn("data[3] or data[4] is null!");
							}
						} else {
							_logger.Warn("data[2] not supported: " + data[2]);
						}
					} else {
						_logger.Warn("data[1] not supported: " + data[1]);
					}
				} else {
					_logger.Warn("data[0] not supported: " + data[0]);
				}
			}
		} else {
			_logger.Warn("Path is not " + PHONE_MESSAGE_PATH);
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		_logger.Debug("onConnected");
		Wearable.MessageApi.addListener(_apiClient, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		_logger.Debug("onConnectionSuspended");
	}

	private void initGoogleApiClient() {
		_logger.Debug("initGoogleApiClient");
		_apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();

		if (_apiClient != null && !(_apiClient.isConnected() || _apiClient.isConnecting())) {
			_logger.Debug("_apiClient.connect");
			_apiClient.connect();
		}
	}
}
