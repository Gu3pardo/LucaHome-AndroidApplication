package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;

public class Security implements Serializable {
    private static final long serialVersionUID = -910775239043882349L;

    private static final String TAG = Security.class.getSimpleName();

    private boolean _isCameraActive;
    private boolean _isMotionControlActive;
    private String _cameraUrl;
    private SerializableList<String> _registeredEvents;

    public Security(
            boolean isCameraActive,
            boolean isMotionControlActive,
            @NonNull String cameraUrl,
            @NonNull SerializableList<String> registeredEvents) {
        _isCameraActive = isCameraActive;
        _isMotionControlActive = isMotionControlActive;
        _cameraUrl = cameraUrl;
        _registeredEvents = registeredEvents;
    }

    public boolean IsCameraActive() {
        return _isCameraActive;
    }

    public boolean IsMotionControlActive() {
        return _isMotionControlActive;
    }

    public String GetCameraUrl() {
        return _cameraUrl;
    }

    public SerializableList<String> GetRegisteredEvents() {
        return _registeredEvents;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s:{IsCameraActive:%s}{IsMotionControlActive:%s}{CameraUrl:%s}{RegisteredEvents:%s}}",
                TAG, _isCameraActive, _isMotionControlActive, _cameraUrl, _registeredEvents
        );
    }
}
