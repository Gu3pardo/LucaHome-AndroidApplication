package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Packages;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.SharedPrefController;

public class MovieController {

    private static final String TAG = MovieController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private LucaPackageController _packageController;
    private ServiceController _serviceController;
    private SharedPrefController _sharedPrefController;
    private SocketController _socketController;

    public MovieController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);

        _serviceController = new ServiceController(context);
        _sharedPrefController = new SharedPrefController(context, SharedPrefConstants.SHARED_PREF_NAME);
        _socketController = new SocketController(context);

        _packageController = new LucaPackageController(context);
    }

    public void StartMovie(@NonNull MovieDto movie) {
        _logger.Debug("Trying to start movie: " + movie.GetTitle());

        _serviceController.StartRestService(
                movie.GetTitle(),
                LucaServerAction.START_MOVIE.toString() + movie.GetTitle(),
                "");

        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.START_OSMC_APP)) {
            if (_packageController.IsPackageInstalled(Packages.KORE)) {
                _packageController.StartApplication(Packages.KORE);
            } else if (_packageController.IsPackageInstalled(Packages.YATSE)) {
                _packageController.StartApplication(Packages.YATSE);
            } else {
                _logger.Warn("User wanted to start an application, but nothing is installed!");
            }
        }

        if (movie.GetSockets() != null) {
            for (String socketName : movie.GetSockets()) {
                if (socketName != null && socketName.length() > 0) {
                    _socketController.SetSocket(socketName, true);
                }
            }
        }
    }
}
