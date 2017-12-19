package guepardoapps.mediamirror.common.enums;

import java.io.Serializable;

public enum AutomaticSettings implements Serializable {

	NULL(0, 25, (25 * 255 / 100), 0, false),

	WORKDAY_SIX(1, 25, (20 * 255 / 100), 6, false),
	WORKDAY_NINE(2, 10, (10 * 255 / 100), 9, false),
	WORKDAY_EIGHTEEN(3, 70, (20 * 255 / 100), 18, false),
	WORKDAY_TWENTY_ONE(4, 30, (10 * 255 / 100), 21, false),

	WEEKEND_NINE(11, 40, (30 * 255 / 100), 9, true),
	WEEKEND_TWELVE(12, 50, (25 * 255 / 100), 12, true),
	WEEKEND_FIFTEEN(13, 75, (25 * 255 / 100), 15, true),
	WEEKEND_EIGHTEEN(14, 80, (25 * 255 / 100), 18, true),
	WEEKEND_TWENTY_ONE(15, 60, (15 * 255 / 100), 21, true),
	WEEKEND_TWENTY_THREE(16, 15, (10 * 255 / 100), 23, true);

	private int _id;

	private int _soundVolumePercentage;
	private double _screenBrightnessPercentage;

	private int _enableHour;
	private boolean _isWeekend;

	AutomaticSettings(
			int id,
			int soundVolumePercentage,
			double screenBrightnessPercentage,
			int enableHour,
			boolean isWeekend) {
		_id = id;

		_soundVolumePercentage = soundVolumePercentage;
		_screenBrightnessPercentage = screenBrightnessPercentage;

		_enableHour = enableHour;
		_isWeekend = isWeekend;
	}

	public int GetId() {
		return _id;
	}

	public int GetSoundVolumePercentage(){
		return _soundVolumePercentage;
	}

	public double GetScreenBrightnessPercentage(){
		return _screenBrightnessPercentage;
	}

	public int GetEnableHour() {
		return _enableHour;
	}

	public boolean IsWeekend(){
		return _isWeekend;
	}

	public static AutomaticSettings GetById(int id) {
		for (AutomaticSettings e : values()) {
			if (e._id == id) {
				return e;
			}
		}
		return NULL;
	}
}
