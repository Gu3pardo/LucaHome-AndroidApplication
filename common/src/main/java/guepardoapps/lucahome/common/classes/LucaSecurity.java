package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LucaSecurity implements ILucaClass {
    private static final String Tag = LucaSecurity.class.getSimpleName();

    public enum CameraType {Raspberry, MediaServer}

    private UUID _roomUuid;

    private boolean _isCameraActive;
    private boolean _isMotionControlActive;
    private String _cameraUrl;
    private CameraType _cameraType;
    private ArrayList<String> _registeredEvents;

    public LucaSecurity(
            @NonNull UUID roomUuid,
            boolean isCameraActive,
            boolean isMotionControlActive,
            @NonNull String cameraUrl,
            @NonNull CameraType cameraType,
            @NonNull ArrayList<String> registeredEvents) {
        _roomUuid = roomUuid;
        _isCameraActive = isCameraActive;
        _isMotionControlActive = isMotionControlActive;
        _cameraUrl = cameraUrl;
        _cameraType = cameraType;
        _registeredEvents = registeredEvents;
    }

    @Override
    public UUID GetUuid() {
        return UUID.randomUUID();
    }

    @Override
    public UUID GetRoomUuid() {
        return _roomUuid;
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

    public CameraType GetCameraType() {
        return _cameraType;
    }

    public ArrayList<String> GetRegisteredEvents() {
        return _registeredEvents;
    }

    @Override
    public String GetCommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandAdd not implemented for " + Tag);
    }

    @Override
    public String GetCommandUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandUpdate not implemented for " + Tag);
    }

    @Override
    public String GetCommandDelete() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandDelete not implemented for " + Tag);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetIsOnServer not implemented for " + Tag);
    }

    @Override
    public boolean GetIsOnServer() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetIsOnServer not implemented for " + Tag);
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction lucaServerDbAction) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetServerDbAction not implemented for " + Tag);
    }

    @Override
    public LucaServerDbAction GetServerDbAction() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetServerDbAction not implemented for " + Tag);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"RoomUuid\":\"%s\",\"IsCameraActive\":\"%s\",\"IsMotionControlActive\":\"%s\",\"CameraUrl\":\"%s\",\"RegisteredEvents\":\"%s\",\"CameraType\":\"%s\"}",
                Tag, _roomUuid, _isCameraActive, _isMotionControlActive, _cameraUrl, _registeredEvents, _cameraType);
    }
}
