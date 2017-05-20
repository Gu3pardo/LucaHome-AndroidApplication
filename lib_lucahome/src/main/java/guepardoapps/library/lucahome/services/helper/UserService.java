package guepardoapps.library.lucahome.services.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.UserDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.RESTService;

import guepardoapps.library.toolset.controller.ReceiverController;
import guepardoapps.library.toolset.controller.SharedPrefController;

public class UserService {

    private static final String TAG = UserService.class.getSimpleName();
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

            String[] answerArray = intent.getStringArrayExtra(Bundles.VALIDATE_USER);

            for (String answer : answerArray) {
                if (answer != null) {
                    _logger.Debug(answer);
                    if (answer.contains("Error")) {
                        _logger.Warn(answer);
                        _userValidated = false;
                        break;
                    } else {
                        answer = answer.replace(ServerActions.VALIDATE_USER, "").replace(":", "");
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
        _sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
    }

    public UserDto LoadUser() {
        String userName = _sharedPrefController.LoadStringValueFromSharedPreferences(SharedPrefConstants.USER_NAME);
        String userPassword = _sharedPrefController
                .LoadStringValueFromSharedPreferences(SharedPrefConstants.USER_PASSPHRASE);
        if (userName != null && userPassword != null) {
            return new UserDto(userName, userPassword);
        }
        return null;
    }

    public void ValidateUser(UserDto user, Runnable callback) {
        _receiverController.RegisterReceiver(_receiver, new String[]{Broadcasts.VALIDATE_USER});

        if (callback != null) {
            _storedCallback = callback;
        }

        Intent serviceIntent = new Intent(_context, RESTService.class);
        Bundle serviceData = new Bundle();

        serviceData.putString(Bundles.USER, user.GetUserName());
        serviceData.putString(Bundles.PASSPHRASE, user.GetPassword());

        serviceData.putString(Bundles.ACTION, ServerActions.VALIDATE_USER);
        serviceData.putString(Bundles.NAME, Bundles.VALIDATE_USER);
        serviceData.putString(Bundles.BROADCAST, Broadcasts.VALIDATE_USER);
        serviceData.putSerializable(Bundles.LUCA_OBJECT, LucaObject.USER);

        serviceIntent.putExtras(serviceData);

        _context.startService(serviceIntent);
    }

    public boolean GetValidationResult() {
        return _userValidated;
    }
}
