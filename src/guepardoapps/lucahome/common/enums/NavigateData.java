package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum NavigateData implements Serializable {
	
	NULL("", -1), 
	FINISH("Finish", 0),
	BOOT("Boot", 1), 
	HOME("Home", 2), 
	LIST("List", 3), 
	SETTINGS("Settings", 4), 
	FLAT_MAP("Flat_Map", 5), 
	SOUND("Sound", 6);

	private String _string;
	private int _int;

	private NavigateData(String stringValue, int intValue) {
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

	public static NavigateData GetById(int value) {
		for (NavigateData e : values()) {
			if (e._int == value) {
				return e;
			}
		}
		return null;
	}

	public static NavigateData GetByString(String value) {
		for (NavigateData e : values()) {
			if (e._string.contains(value)) {
				return e;
			}
		}
		return null;
	}
}
