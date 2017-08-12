package guepardoapps.lucahome.data.controller;

import guepardoapps.lucahome.basic.utils.Logger;

public class LocalDriveController {
    private static final LocalDriveController SINGLETON = new LocalDriveController();

    private static final String TAG = LocalDriveController.class.getSimpleName();
    private Logger _logger;

    private LocalDriveController() {
        _logger = new Logger(TAG);
    }

    public static LocalDriveController getInstance() {
        return SINGLETON;
    }
}
