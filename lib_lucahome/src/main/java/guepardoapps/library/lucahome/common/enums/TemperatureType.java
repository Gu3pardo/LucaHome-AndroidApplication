package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

public enum TemperatureType implements Serializable {
	
	NULL("", -1),
	RASPBERRY("Raspberry", 0), 
	SMARTPHONE_SENSOR("Smartphone Sensor", 1), 
	CITY("City", 2);

	private String _string;
	private int _int;

	TemperatureType(String stringValue, int intValue) {
		_string = stringValue;
		_int = intValue;
	}

	@Override
	public String toString() {
		return _string;
	}

	public int GetInt() {
		return _int;
	}

	public static TemperatureType GetById(int intValue) {
		for (TemperatureType e : values()) {
			if (e._int == intValue) {
				return e;
			}
		}
		return NULL;
	}

	public static TemperatureType GetByString(String string) {
		for (TemperatureType e : values()) {
			if (e._string.contains(string)) {
				return e;
			}
		}
		return NULL;
	}
}
