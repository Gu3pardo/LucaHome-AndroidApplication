package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum LucaObject implements Serializable {
	
	WIRELESS_SOCKET("Wireless_Socket", 0), 
	SCHEDULE("Schedule", 1), 
	TIMER("Timer", 2),  
	BIRTHDAY("Birthday", 3), 
	MOVIE("Movie", 4), 
	TEMPERATURE("Temperature", 5),
	INFORMATION("Information", 6),
	CHANGE("Change", 7),
	USER("User", 10),
	WEATHER_DATA("Weather_Data", 20),
	WEATHER_CURRENT("Weather_Current", 21),
	WEATHER_FORECAST("Weather_Forecast", 22),
	SOUND("Sound", 23),
	MAP_CONTENT("Map_Content", 24),
	GO_TO_BED("GoToBed", 25),
	DUMMY("Dummy", 99);

	private String _string;
	private int _int;

	private LucaObject(String stringValue, int intValue) {
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

	public static LucaObject GetById(int value) {
		for (LucaObject e : values()) {
			if (e._int == value) {
				return e;
			}
		}
		return null;
	}

	public static LucaObject GetByString(String value) {
		for (LucaObject e : values()) {
			if (e._string.contains(value)) {
				return e;
			}
		}
		return null;
	}
}
