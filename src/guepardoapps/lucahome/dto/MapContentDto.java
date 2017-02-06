package guepardoapps.lucahome.dto;

import java.io.Serializable;
import java.util.ArrayList;

import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.enums.DrawingType;

public class MapContentDto implements Serializable {

	private static final long serialVersionUID = 8764451572750126391L;

	private static final String TAG = MapContentDto.class.getName();
	@SuppressWarnings("unused")
	private LucaHomeLogger _logger;

	private int _id;
	private int[] _position;
	private DrawingType _drawingType;

	private ArrayList<String> _schedules;
	private ArrayList<String> _sockets;
	private String _temperatureArea;

	public MapContentDto(int id, int[] position, DrawingType drawingType, ArrayList<String> schedules,
			ArrayList<String> sockets, String temperatureArea) {
		_logger = new LucaHomeLogger(TAG);

		_id = id;
		_position = position;
		_drawingType = drawingType;

		_schedules = schedules;
		_sockets = sockets;
		_temperatureArea = temperatureArea;
	}

	public int GetId() {
		return _id;
	}

	public int[] GetPosition() {
		return _position;
	}

	public DrawingType GetDrawingType() {
		return _drawingType;
	}

	public ArrayList<String> GetSchedules() {
		return _schedules;
	}

	public ArrayList<String> GetSockets() {
		return _sockets;
	}

	public String GetTemperatureArea() {
		return _temperatureArea;
	}

	private String getSchedulesString() {
		String string = "";
		if (_schedules != null) {
			for (String entry : _schedules) {
				string += entry + "|";
			}
		}
		return string;
	}

	private String getSocketsString() {
		String string = "";
		if (_sockets != null) {
			for (String entry : _sockets) {
				string += entry + "|";
			}
		}
		return string;
	}

	public String GetCommandAdd() {
		return Constants.ACTION_ADD_MAP_CONTENT + String.valueOf(_id) + "&position=" + String.valueOf(_position[0])
				+ "|" + String.valueOf(_position[1]) + "&type=" + String.valueOf(_drawingType.GetId()) + "&schedules="
				+ getSchedulesString() + "&sockets=" + getSocketsString() + "&temperature=" + _temperatureArea;
	}

	public String GetCommandUpdate() {
		return Constants.ACTION_UPDATE_MAP_CONTENT + String.valueOf(_id) + "&position=" + String.valueOf(_position[0])
				+ "|" + String.valueOf(_position[1]) + "&type=" + String.valueOf(_drawingType.GetId()) + "&schedules="
				+ getSchedulesString() + "&sockets=" + getSocketsString() + "&temperature=" + _temperatureArea;
	}

	public String GetCommandDelete() {
		return Constants.ACTION_DELETE_MAP_CONTENT + String.valueOf(_id);
	}

	@Override
	public String toString() {
		return "MapContent: {Id: " + String.valueOf(_id) + "}{Position: " + String.valueOf(_position[0]) + "|"
				+ String.valueOf(_position[1]) + "}{DrawingType: " + _drawingType.toString() + "}{Schedules: "
				+ getSchedulesString() + "}{Sockets: " + getSocketsString() + "}{Temperature: " + _temperatureArea
				+ "}";
	}
}
