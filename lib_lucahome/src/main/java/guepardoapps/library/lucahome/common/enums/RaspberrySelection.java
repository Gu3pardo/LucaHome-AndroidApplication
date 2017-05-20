package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

public enum RaspberrySelection implements Serializable {
	
	BOTH("Both", 0, "Flat"), 
	RASPBERRY_1("Raspberry_1", 1, "Living Room"), 
	RASPBERRY_2("Raspberry_2", 2, "Sleeping Room"), 
	DUMMY("Dummy", -1, "n.a.");

	private String _string;
	private int _int;
	private String _area;

	RaspberrySelection(String stringValue, int intValue, String area) {
		_string = stringValue;
		_int = intValue;
		_area = area;
	}

	@Override
	public String toString() {
		return _string;
	}

	public int GetInt() {
		return _int;
	}
	public String GetArea() {
		return _area;
	}

	public static RaspberrySelection GetByString(String string) {
		for (RaspberrySelection e : values()) {
			if (e._string.contains(string)) {
				return e;
			}
		}
		return BOTH;
	}

	public static RaspberrySelection GetById(int intValue) {
		for (RaspberrySelection e : values()) {
			if (e._int == intValue) {
				return e;
			}
		}
		return BOTH;
	}

	public static RaspberrySelection GetByArea(String area) {
		for (RaspberrySelection e : values()) {
			if (e._area.contains(area)) {
				return e;
			}
		}
		return BOTH;
	}
}
