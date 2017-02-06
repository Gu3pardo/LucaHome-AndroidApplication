package guepardoapps.lucahome.viewcontroller;

import android.content.Context;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.dto.MovieDto;
import guepardoapps.lucahome.services.helper.PackageService;
import guepardoapps.toolset.controller.SharedPrefController;

public class MovieController {

	private static final String TAG = MovieController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;

	private ServiceController _serviceController;
	private SharedPrefController _sharedPrefController;
	private SocketController _socketController;

	private PackageService _packageService;

	public MovieController(Context context) {
		_logger = new LucaHomeLogger(TAG);

		_context = context;

		_serviceController = new ServiceController(_context);
		_sharedPrefController = new SharedPrefController(_context, Constants.SHARED_PREF_NAME);
		_socketController = new SocketController(_context);

		_packageService = new PackageService(_context);
	}

	public void StartMovie(MovieDto movie) {
		_logger.Debug("Trying to start movie: " + movie.GetTitle());

		String action = Constants.ACTION_START_MOVIE + movie.GetTitle();
		_serviceController.StartRestService(movie.GetTitle(), action, null, LucaObject.MOVIE, RaspberrySelection.BOTH);

		if (_sharedPrefController.LoadBooleanValueFromSharedPreferences(Constants.START_OSMC_APP)) {
			if (_packageService.IsPackageInstalled(Constants.PACKAGE_KORE)) {
				_packageService.StartApplication(Constants.PACKAGE_KORE);
			} else if (_packageService.IsPackageInstalled(Constants.PACKAGE_YATSE)) {
				_packageService.StartApplication(Constants.PACKAGE_YATSE);
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
