package guepardoapps.lucahome.wearcontrol.services;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;

public class WearMessageService extends Service implements GoogleApiClient.ConnectionCallbacks {

	private static final String WEAR_MESSAGE_PATH = "/message";
	private static final String TAG = WearMessageService.class.getName();

	private LucaHomeLogger _logger;

	private boolean _dataSend;

	private GoogleApiClient _apiClient;

	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		if (_logger == null) {
			_logger = new LucaHomeLogger(TAG);
		}

		if (intent != null) {
			Bundle data = intent.getExtras();
			if (data != null) {
				String message = data.getString(Constants.BUNDLE_WEAR_MESSAGE_TEXT);
				if (message != null) {
					_logger.Debug("message: " + message);
					initGoogleApiClient();
					sendMessage(WEAR_MESSAGE_PATH, message);
				}
			} else {
				_logger.Warn("Data is null!");
			}
		} else {
			_logger.Warn("Intent is null!");
		}

		finish();
		return 0;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onConnected(Bundle arg0) {
		_logger.Debug("onConnected");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		_logger.Debug("onConnectionSuspended");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");
	}

	private void initGoogleApiClient() {
		_logger.Debug("initGoogleApiClient");
		_apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
		_apiClient.connect();
	}

	private void sendMessage(final String path, final String text) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				_logger.Debug("sendMessage run");
				NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(_apiClient).await();
				for (Node node : nodes.getNodes()) {
					MessageApi.SendMessageResult result = Wearable.MessageApi
							.sendMessage(_apiClient, node.getId(), path, text.getBytes()).await();

					if (result != null) {
						_logger.Debug("result: " + result);
					} else {
						_logger.Warn("result is null");
					}
				}
				_dataSend = true;
			}
		}).start();
	}

	private void finish() {
		_logger.Debug("finish");
		if (_dataSend) {
			stopSelf();
		} else {
			_logger.Warn("Data not send yet!");
		}
	}
}
