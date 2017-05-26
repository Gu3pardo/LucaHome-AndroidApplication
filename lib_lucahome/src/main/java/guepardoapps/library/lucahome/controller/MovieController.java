package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Packages;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.services.helper.PackageService;

import guepardoapps.library.toolset.controller.SharedPrefController;

public class MovieController {

    private static final String TAG = MovieController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private ServiceController _serviceController;
    private SharedPrefController _sharedPrefController;
    private SocketController _socketController;

    private PackageService _packageService;

    public MovieController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);

        _serviceController = new ServiceController(context);
        _sharedPrefController = new SharedPrefController(context, SharedPrefConstants.SHARED_PREF_NAME);
        _socketController = new SocketController(context);

        _packageService = new PackageService(context);
    }

    public void StartMovie(@NonNull MovieDto movie) {
        _logger.Debug("Trying to start movie: " + movie.GetTitle());

        String action = ServerActions.START_MOVIE + movie.GetTitle();
        _serviceController.StartRestService(
                movie.GetTitle(),
                action,
                "",
                LucaObject.MOVIE,
                RaspberrySelection.BOTH);

        if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(SharedPrefConstants.START_OSMC_APP)) {
            if (_packageService.IsPackageInstalled(Packages.KORE)) {
                _packageService.StartApplication(Packages.KORE);
            } else if (_packageService.IsPackageInstalled(Packages.YATSE)) {
                _packageService.StartApplication(Packages.YATSE);
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
