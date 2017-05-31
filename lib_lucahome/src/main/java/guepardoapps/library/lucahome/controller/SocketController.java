package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.Packages;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.SharedPrefController;

public class SocketController {

    private static final String TAG = SocketController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private LucaPackageController _packageController;
    private ServiceController _serviceController;
    private SharedPrefController _sharedPrefController;

    public SocketController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _serviceController = new ServiceController(context);
        _sharedPrefController = new SharedPrefController(context, SharedPrefConstants.SHARED_PREF_NAME);
        _packageController = new LucaPackageController(context);
    }

    public void SetSocket(
            @NonNull WirelessSocketDto socket,
            boolean newState) {
        _logger.Debug("SetSocket: " + socket.GetName() + " to " + String.valueOf(newState));
        _serviceController.StartRestService(
                socket.GetName(),
                socket.GetCommandSet(newState),
                Broadcasts.RELOAD_SOCKETS);
    }

    public void SetSocket(
            @NonNull String socketName,
            boolean newState) {
        _logger.Debug("SetSocket: " + socketName + " to " + String.valueOf(newState));
        _serviceController.StartRestService(
                socketName,
                LucaServerAction.SET_SOCKET.toString() + socketName + ((newState) ? Constants.STATE_ON : Constants.STATE_OFF),
                Broadcasts.RELOAD_SOCKETS);
    }

    public void LoadSockets() {
        _logger.Debug("SetSocket");
        _serviceController.StartRestService(
                Bundles.SOCKET_DOWNLOAD,
                LucaServerAction.GET_SOCKETS.toString(),
                Broadcasts.DOWNLOAD_SOCKET_FINISHED);
    }

    public void DeleteSocket(@NonNull WirelessSocketDto socket) {
        _logger.Debug("DeleteSocket: " + socket.GetName());
        _serviceController.StartRestService(
                socket.GetName(),
                socket.GetCommandDelete(),
                Broadcasts.RELOAD_SOCKETS);
    }

    public boolean ValidateSocketCode(@NonNull String code) {
        _logger.Debug("ValidateSocketCode: " + code);

        if (code.length() != 6) {
            return false;
        }

        for (int charIndex = 0; charIndex < 5; charIndex++) {
            if (!(code.charAt(charIndex) == '0' || code.charAt(charIndex) == '1')) {
                return false;
            }
        }

        return (code.charAt(5) == 'A'
                || code.charAt(5) == 'B'
                || code.charAt(5) == 'C'
                || code.charAt(5) == 'D'
                || code.charAt(5) == 'E');
    }

    public void CheckMedia(@NonNull WirelessSocketDto socket) {
        _logger.Debug("GetDrawable: " + socket.GetName());

        if (socket.GetName().contains("Sound")) {
            if (socket.IsActivated()) {
                return;
            }

            if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.START_AUDIO_APP)) {
                if (_packageController.IsPackageInstalled(Packages.BUBBLE_UPNP)) {
                    _packageController.StartApplication(Packages.BUBBLE_UPNP);
                }
            }
        }
    }
}
