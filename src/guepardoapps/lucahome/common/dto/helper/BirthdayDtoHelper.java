package guepardoapps.lucahome.common.dto.helper;

import java.util.Calendar;

import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;

public class BirthdayDtoHelper {

	private static final String TAG = BirthdayDtoHelper.class.getName();
	private LucaHomeLogger _logger;

	private Calendar _calendar;

	public BirthdayDtoHelper() {
		_logger = new LucaHomeLogger(TAG);
		_calendar = Calendar.getInstance();
		_logger.Debug("_calendar: " + _calendar.toString());
	}

	public boolean HasBirthday(BirthdayDto birthday) {
		_logger.Debug("HasBirthday");
		_logger.Debug("Birthday: " + birthday.toString());
		if ((birthday.GetBirthday().get(Calendar.DAY_OF_MONTH) == _calendar.get(Calendar.DAY_OF_MONTH))
				&& (birthday.GetBirthday().get(Calendar.MONTH) == _calendar.get(Calendar.MONTH))) {
			_logger.Debug("HasBirthday: " + String.valueOf(true));
			return true;
		}
		_logger.Debug("HasBirthday: " + String.valueOf(false));
		return false;
	}

	public int GetAge(BirthdayDto birthday) {
		_logger.Debug("GetAge");
		_logger.Debug("Birthday: " + birthday.toString());
		int age;
		if ((_calendar.get(Calendar.MONTH) > birthday.GetBirthday().get(Calendar.MONTH))
				|| (_calendar.get(Calendar.MONTH) == birthday.GetBirthday().get(Calendar.MONTH)
						&& _calendar.get(Calendar.DAY_OF_MONTH) >= birthday.GetBirthday().get(Calendar.DAY_OF_MONTH))) {
			age = _calendar.get(Calendar.YEAR) - birthday.GetBirthday().get(Calendar.YEAR);
		} else {
			age = _calendar.get(Calendar.YEAR) - birthday.GetBirthday().get(Calendar.YEAR) - 1;
		}
		_logger.Debug("Age: " + String.valueOf(age));
		return age;
	}
}
