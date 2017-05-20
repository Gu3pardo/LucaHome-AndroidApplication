package guepardoapps.library.lucahome.common.enums;

import java.io.Serializable;

public enum Command implements Serializable {
	
	NULL("", -1),
	NAVIGATE("Navigate", 0), 
	SHOW("Show", 1), 
	SHOW_USER_LOGIN_DIALOG("Show_User_LogIn_Dialog", 2);

	private String _string;
	private int _int;

	Command(String stringValue, int intValue) {
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

	public static Command GetById(int value) {
		for (Command e : values()) {
			if (e._int == value) {
				return e;
			}
		}
		return NULL;
	}

	public static Command GetByString(String value) {
		for (Command e : values()) {
			if (e._string.contains(value)) {
				return e;
			}
		}
		return NULL;
	}
}
