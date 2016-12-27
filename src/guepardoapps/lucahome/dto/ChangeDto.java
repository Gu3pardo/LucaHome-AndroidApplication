package guepardoapps.lucahome.dto;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

public class ChangeDto implements Serializable {

	private static final long serialVersionUID = 8796770534384442492L;

	private String _type;
	private Date _date;
	private Time _time;
	private String _user;

	@SuppressWarnings("deprecation")
	public ChangeDto(String type, Date date, Time time, String user) {
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

	public Time GetTime() {
		return _time;
	}

	public String GetUser() {
		return _user;
	}

	public String toString() {
		return "{Change: {Type: " + _type + "};{Date: " + _date.toString() + "};{Time: " + _time.toString()
				+ "};{User: " + _user + "}}";
	}
}
