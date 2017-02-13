package guepardoapps.lucahome.common.enums;

import java.io.Serializable;

public enum MainServiceAction implements Serializable {
	
	NULL("", -1),
	BOOT("Boot", 0), 
	DOWNLOAD_ALL("Download_All", 100), 
	DOWLOAD_SOCKETS("Download_Sockets", 101), 
	DOWNLOAD_SCHEDULES("Download_Schedules", 102),  
	DOWNLOAD_BIRTHDAYS("Download_Birthdays", 103), 
	DOWNLOAD_MOVIES("Download_Movies", 104), 
	DOWNLOAD_TEMPERATURE("Download_Temperature", 105),
	DOWNLOAD_INFORMATIONS("Download_Informations", 106),
	DOWNLOAD_CHANGES("Download_Changes", 107),
	DOWNLOAD_USER("Download_User", 108),
	DOWNLOAD_WEATHER_DATA("Download_Weather_Data", 109),
	DOWNLOAD_WEATHER_CURRENT("Download_Weather_Current", 110),
	DOWNLOAD_WEATHER_FORECAST("Download_Weather_Forecast", 111),
	DOWNLOAD_SOUND("Download_Sound", 112),
	DOWNLOAD_SOUND_LIST("Download_Sound_List", 113),
	DOWNLOAD_MAP_CONTENT("Download_Map_Content", 114), 
	GET_ALL("Get_All", 200), 
	GET_SOCKETS("Get_Sockets", 201), 
	GET_SCHEDULES("Get_Schedules", 202),  
	GET_TIMER("Get_Timer", 203),  
	GET_BIRTHDAYS("Get_Birthdays", 204), 
	GET_MOVIES("Get_Movies", 205), 
	GET_TEMPERATURE("Get_Temperature", 206),
	GET_INFORMATIONS("Get_Informations", 207),
	GET_CHANGES("Get_Changes", 208),
	GET_USER("Get_User", 209),
	GET_WEATHER_DATA("Get_Weather_Data", 210),
	GET_WEATHER_CURRENT("Get_Weather_Current", 211),
	GET_WEATHER_FORECAST("Get_Weather_Forecast", 212),
	GET_SOUND("Get_Sound", 213),
	GET_SOUND_DATA("Get_Sound_data", 214),
	GET_SOUND_LIST("Get_Sound_List", 215),
	GET_MAP_CONTENT("Get_Map_Content", 216),
	GET_AIR_PRESSURE("Get_AirPressure", 217),
	GET_HUMIDITY("Get_Humidity", 218),
	SHOW_NOTIFICATION_SOCKET("Show_Notification_Socket", 300),
	SHOW_NOTIFICATION_WEATHER("Show_Notification_Weather", 301),
	SHOW_NOTIFICATION_TEMPERATURE("Show_Notification_Temperature", 302),
	ENABLE_HEATING("Enable_Heating", 400),
	ENABLE_HEATING_AND_SOUND("Enable_Heating_And_Sound", 401);

	private String _string;
	private int _int;

	private MainServiceAction(String stringValue, int intValue) {
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

	public static MainServiceAction GetById(int value) {
		for (MainServiceAction e : values()) {
			if (e._int == value) {
				return e;
			}
		}
		return null;
	}

	public static MainServiceAction GetByString(String value) {
		for (MainServiceAction e : values()) {
			if (e._string.contains(value)) {
				return e;
			}
		}
		return null;
	}
}
