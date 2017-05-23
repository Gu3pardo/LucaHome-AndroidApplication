package guepardoapps.library.lucahome.runnable;

import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.DatabaseController;

public class DeleteStoredActionRunnable implements Runnable {

    private static final String TAG = DeleteStoredActionRunnable.class.getSimpleName();
    private LucaHomeLogger _logger;

    private DatabaseController _databaseController;

    private String _actionName;

    private boolean _isInitialized = false;

    public DeleteStoredActionRunnable(
            @NonNull String actionName,
            @NonNull DatabaseController databaseController) {
        _logger = new LucaHomeLogger(TAG);

        _databaseController = databaseController;

        if (actionName.isEmpty()) {
            _logger.Error("actionName is null or is empty!");
            return;
        }
        _actionName = actionName;

        _isInitialized = true;
    }

    public String GetActionName() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return "";
        }
        return _actionName;
    }

    public boolean IsInitialized() {
        return _isInitialized;
    }

    @Override
    public void run() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }
        _databaseController.DeleteActionByName(_actionName);
    }

}
