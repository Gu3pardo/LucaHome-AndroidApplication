package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.UserDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.RESTService;
import guepardoapps.library.lucahome.services.helper.UserService;
import guepardoapps.library.lucahome.services.wearcontrol.WearMessageService;

public class ServiceController {

    private static final String TAG = ServiceController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private UserService _userService;

    public ServiceController(Context context) {
        _logger = new LucaHomeLogger(TAG);

        _context = context;
        _userService = new UserService(_context);
    }

    public void StartRestService(String name,
                                 String action,
                                 String broadcast,
                                 LucaObject lucaObject,
                                 RaspberrySelection raspberrySelection) {
        UserDto loggedInUser = _userService.LoadUser();
        if (loggedInUser != null) {
            StartRestService(loggedInUser.GetUserName(), loggedInUser.GetPassword(), name, action, broadcast,
                    lucaObject, raspberrySelection);
        } else {
            _logger.Warn("loggedInUser is null!");
            StartRestService("", "", name, action, broadcast, lucaObject, raspberrySelection);
        }
    }

    public void StartRestService(String user,
                                 String password,
                                 String name,
                                 String action,
                                 String broadcast,
                                 LucaObject lucaObject,
                                 RaspberrySelection raspberrySelection) {
        Intent serviceIntent = new Intent(_context, RESTService.class);
        Bundle serviceData = new Bundle();

        serviceData.putString(Bundles.USER, user);
        serviceData.putString(Bundles.PASSPHRASE, password);

        serviceData.putString(Bundles.ACTION, action);
        serviceData.putString(Bundles.NAME, name);
        serviceData.putString(Bundles.BROADCAST, broadcast);
        serviceData.putSerializable(Bundles.LUCA_OBJECT, lucaObject);
        serviceData.putSerializable(Bundles.RASPBERRY_SELECTION, raspberrySelection);

        serviceIntent.putExtras(serviceData);
        _context.startService(serviceIntent);
    }

    public void SendMessageToWear(String message) {
        _logger.Info("Wear message is " + message);
        Intent serviceIntent = new Intent(_context, WearMessageService.class);
        Bundle serviceData = new Bundle();
        serviceData.putString(Bundles.WEAR_MESSAGE_TEXT, message);
        serviceIntent.putExtras(serviceData);
        _context.startService(serviceIntent);
    }
}
