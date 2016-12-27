package guepardoapps.lucahome.services.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.dto.UserDto;
import guepardoapps.lucahome.services.RESTService;
import guepardoapps.toolset.controller.ReceiverController;
import guepardoapps.toolset.controller.SharedPrefController;

public class UserService {

	private static final String TAG = UserService.class.getName();
	private LucaHomeLogger _logger;

	private boolean _userValidated;
	private Runnable _storedCallback = null;

	private Context _context;

	private ReceiverController _receiverController;
	private SharedPrefController _sharedPrefController;

	private BroadcastReceiver _receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("Received data...");

			String[] answerArray = intent.getStringArrayExtra(Constants.BUNDLE_VALIDATE_USER);

			for (String answer : answerArray) {
				if (answer != null) {
					_logger.Debug(answer);
					if (answer.contains("Error")) {
						_logger.Warn(answer);
						_userValidated = false;
						break;
					} else {
						answer = answer.replace(Constants.ACTION_VALIDATE_USER, "").replace(":", "");
						if (answer.length() == 1 && answer.contains("1")) {
							_userValidated &= true;
						} else {
							_logger.Warn(answer);
						}
					}
				}
			}

			_receiverController.UnregisterReceiver(_receiver);

			_storedCallback.run();
			_storedCallback = null;
		}
	};

	public UserService(Context context) {
		_logger = new LucaHomeLogger(TAG);

		_userValidated = true;

		_context = context;

		_receiverController = new ReceiverController(_context);
		_sharedPrefController = new SharedPrefController(_context, Constants.SHARED_PREF_NAME);
	}

	public UserDto LoadUser() {
		String userName = _sharedPrefController.LoadStringValueFromSharedPreferences(Constants.USER_NAME);
		String userPassword = _sharedPrefController.LoadStringValueFromSharedPreferences(Constants.USER_PASSPHRASE);
		if (userName != null && userPassword != null) {
			UserDto user = new UserDto(userName, userPassword);
			return user;
		}
		return null;
	}

	public void ValidateUser(UserDto user, Runnable callback) {
		_receiverController.RegisterReceiver(_receiver, new String[] { Constants.BROADCAST_VALIDATE_USER });

		if (callback != null) {
			_storedCallback = callback;
		}

		Intent serviceIntent = new Intent(_context, RESTService.class);
		Bundle serviceData = new Bundle();

		serviceData.putString(Constants.BUNDLE_USER, user.GetUserName());
		serviceData.putString(Constants.BUNDLE_PASSPHRASE, user.GetPassword());

		serviceData.putString(Constants.BUNDLE_ACTION, Constants.ACTION_VALIDATE_USER);
		serviceData.putString(Constants.BUNDLE_NAME, Constants.BUNDLE_VALIDATE_USER);
		serviceData.putString(Constants.BUNDLE_BROADCAST, Constants.BROADCAST_VALIDATE_USER);
		serviceData.putSerializable(Constants.BUNDLE_LUCA_OBJECT, LucaObject.USER);

		serviceIntent.putExtras(serviceData);

		_context.startService(serviceIntent);
	}

	public boolean GetValidationResult() {
		return _userValidated;
	}
}
