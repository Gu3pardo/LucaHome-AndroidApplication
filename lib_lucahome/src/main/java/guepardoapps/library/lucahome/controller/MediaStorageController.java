package guepardoapps.library.lucahome.controller;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.common.constants.NASConstants;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.CommandController;
import guepardoapps.library.toolset.controller.FileController;

public class MediaStorageController {

    private static final MediaStorageController SINGLETON_CONTROLLER = new MediaStorageController();

    private static final String TAG = MediaStorageController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private CommandController _commandController;
    private FileController _fileController;

    private boolean _isInitialized;

    private String _raspberryCameraDir = Environment.getExternalStorageDirectory() + File.separator
            + NASConstants.NAS_RASPBERRY_CAMERA;
    private boolean _nasCameraMounted;

    public static MediaStorageController getInstance() {
        return SINGLETON_CONTROLLER;
    }

    private MediaStorageController() {
        _logger = new LucaHomeLogger(TAG);
        _logger.Debug(TAG + " created...");
    }

    public void initialize(@NonNull Context context) {
        if (!_isInitialized) {

            _commandController = new CommandController(context);
            _fileController = new FileController();

            mountNAS();

            _isInitialized = true;
        }
    }

    public boolean IsInitialized() {
        return _isInitialized;
    }

    public boolean NasCameraMounted() {
        return _nasCameraMounted;
    }

    public String GetRaspberryCameraDir() {
        return _raspberryCameraDir;
    }

    public SerializableList<File> GetCameraFolder() {
        return _fileController.GetSubFolder(_raspberryCameraDir);
    }

    public boolean Dispose() {
        _logger.Debug("Dispose");

        if (!_isInitialized) {
            _logger.Error("not initialized!");
            return false;
        }

        return true;
    }

    private void mountNAS() {
        _logger.Debug("mountNAS");

        _logger.Debug("CameraDir: " + _raspberryCameraDir);
        if (_commandController.HasRoot()) {
            if (_fileController.CreateDirectory(_raspberryCameraDir)) {
                String[] command = new String[]{"su", "-c", "mount -t cifs //192.168.178.27/raspberryStore/Camera "
                        + _raspberryCameraDir + " -o user=mediamirror,pass=mediamirror"};

                String commandLog = "";
                for (String entry : command) {
                    commandLog += entry;
                }
                _logger.Debug("Command is " + commandLog);

                _nasCameraMounted = _commandController.PerformCommand(command);
                _logger.Info("_nasCameraMounted is: " + String.valueOf(_nasCameraMounted));
            } else {
                _logger.Error("Failed to create directory for NAS raspberry camera!");
            }
        } else {
            _logger.Warn("No root detected!");
        }
    }
}
