package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.sql.Date;

import guepardoapps.library.toolset.common.classes.SerializableTime;

public class ChangeDto implements Serializable {

	private static final long serialVersionUID = 8796770534384442492L;

	private String _type;
	private Date _date;
	private SerializableTime _time;
	private String _user;

	@SuppressWarnings("deprecation")
	public ChangeDto(
			@NonNull String type,
			@NonNull Date date,
			@NonNull SerializableTime time,
			@NonNull String user) {
		_type = type;
		_date = date;
		_date.setYear(_date.getYear() - 1900);
		_time = time;
		_user = user;
	}

	public String GetType() {
		return _type;
	}

	public Date GetDate() {
		return _date;
	}

	public SerializableTime GetTime() {
		return _time;
	}

	public String GetUser() {
		return _user;
	}

	public String toString() {
		return "{Change: {Type: " + _type
				+ "};{Date: " + _date.toString()
				+ "};{Time: " + _time.toString()
				+ "};{User: " + _user + "}}";
	}
}
