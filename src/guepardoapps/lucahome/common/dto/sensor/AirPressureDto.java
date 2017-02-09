package guepardoapps.lucahome.common.dto.sensor;

import java.io.Serializable;
import java.sql.Time;

public class AirPressureDto implements Serializable {

	private static final long serialVersionUID = -3771050814199928178L;

	private double _airPressure;
	private String _area;
	private Time _lastUpdate;

	public AirPressureDto(double airPressure, String area, Time lastUpdate) {
		_airPressure = airPressure;
		_area = area;
		_lastUpdate = lastUpdate;
	}

	public double GetAirPressureValue() {
		return _airPressure;
	}

	public void SetAirPressure(double airPressure) {
		_airPressure = airPressure;
	}

	public String GetAirPressureString() {
		return String.valueOf(_airPressure).substring(0, 6) + "mbar";
	}

	public String GetArea() {
		return _area;
	}

	public Time GetLastUpdate() {
		return _lastUpdate;
	}

	public void SetLastUpdate(Time lastUpdate) {
		_lastUpdate = lastUpdate;
	}

	public String GetWatchFaceText() {
		return _area + ": " + GetAirPressureString() + " " + _lastUpdate.toString();
	}

	public String toString() {
		return "{AirPressure: {Value: " + GetAirPressureString() + "};{Area: " + _area + "};{LastUpdate: "
				+ _lastUpdate.toString() + "}}";
	}
}
