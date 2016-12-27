package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum DrawingType implements Serializable {

	NULL(0, ""), 
	RASPBERRY(1, "Raspberry"), 
	ARDUINO(2, "Arduino"), 
	SOCKET(3, "Socket"), 
	TEMPERATURE(4, "Temperature");

	private String _type;
	private int _id;

	private DrawingType(int id, String type) {
		_id = id;
		_type = type;
	}

	public int GetId() {
		return _id;
	}

	@Override
	public String toString() {
		return _type;
	}

	public static DrawingType GetById(int id) {
		for (DrawingType e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return null;
	}
}
