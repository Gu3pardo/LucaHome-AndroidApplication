package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MotionCameraDto implements Serializable {

	private static final long serialVersionUID = -9107752390435190850L;

	private static final String TAG = MotionCameraDto.class.getSimpleName();

	private boolean _cameraState;
	private String _cameraUrl;
	private SerializableList<String> _motionEvents;
	private boolean _cameraControlState;

	public MotionCameraDto(
			boolean cameraState,
			@NonNull String cameraUrl,
			@NonNull SerializableList<String> motionEvents,
			boolean cameraControlState) {
		_cameraState = cameraState;
		_cameraUrl = cameraUrl;
		_motionEvents = motionEvents;
		_cameraControlState = cameraControlState;
	}

	public boolean GetCameraState() {
		return _cameraState;
	}

	public String GetCameraUrl() {
		return _cameraUrl;
	}

	public SerializableList<String> GetMotionEvents() {
		return _motionEvents;
	}

	public boolean GetCameraControlState() {
		return _cameraControlState;
	}

	public String toString() {
		return "{" + TAG + ": {CameraState: " + String.valueOf(_cameraState)
				+ "};{CameraUrl: " + _cameraUrl
				+ "};{MotionEvents: " + _motionEvents.toString()
				+ "};{CameraControlState: " + String.valueOf(_cameraControlState) + "}}";
	}
}
