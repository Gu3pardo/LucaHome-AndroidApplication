package guepardoapps.library.lucahome.services.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Bundles;

import guepardoapps.library.toolset.common.Logger;

public class MessageSendHelper {

    private static final String TAG = MessageSendHelper.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private Class<?> _phoneMessageService;

    public MessageSendHelper(
            @NonNull Context context,
            @NonNull Class<?> phoneMessageService) {
        _logger = new Logger(TAG);
        _context = context;
        _phoneMessageService = phoneMessageService;
    }

    public void SendMessage(@NonNull String message) {
        _logger.Debug("SendMessage");
        _logger.Debug("message: " + message);

        Intent serviceIntent = new Intent(_context, _phoneMessageService);
        Bundle serviceData = new Bundle();
        serviceData.putString(Bundles.PHONE_MESSAGE_TEXT, message);
        serviceIntent.putExtras(serviceData);
        _context.startService(serviceIntent);
    }
}
