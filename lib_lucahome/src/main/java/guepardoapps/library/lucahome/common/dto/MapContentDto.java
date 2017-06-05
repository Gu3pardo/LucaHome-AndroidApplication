package guepardoapps.library.lucahome.common.dto;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.library.lucahome.common.enums.DrawingType;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;

public class MapContentDto implements Serializable {

    private static final long serialVersionUID = 8764451572750126391L;

    private static final String TAG = MapContentDto.class.getSimpleName();

    private int _id;
    private int[] _position;
    private DrawingType _drawingType;

    private ArrayList<String> _schedules;
    private ArrayList<String> _sockets;
    private String _temperatureArea;

    private String _mediaServerIp;

    private boolean _visibility;

    public MapContentDto(
            int id,
            int[] position,
            @NonNull DrawingType drawingType,
            @NonNull ArrayList<String> schedules,
            @NonNull ArrayList<String> sockets,
            @NonNull String temperatureArea,
            @NonNull String mediaServerIp,
            boolean visibility) {
        _id = id;
        _position = position;
        _drawingType = drawingType;

        _schedules = schedules;
        _sockets = sockets;
        _temperatureArea = temperatureArea;

        _mediaServerIp = mediaServerIp;

        _visibility = visibility;
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

    public String GetMediaServerIp() {
        return _mediaServerIp;
    }

    public boolean IsVisible() {
        return _visibility;
    }

    public String GetPositionString() {
        return String.format(Locale.getDefault(), "%d|%d", _position[0], _position[1]);
    }

    public String GetSchedulesString() {
        String string = "";
        if (_schedules != null) {
            for (String entry : _schedules) {
                string += entry + "|";
            }
        }
        return string;
    }

    public String GetSocketsString() {
        String string = "";
        if (_sockets != null) {
            for (String entry : _sockets) {
                string += entry + "|";
            }
        }
        return string;
    }

    public String GetCommandAdd() {
        return LucaServerAction.ADD_MAP_CONTENT.toString() + String.valueOf(_id)
                + "&position=" + GetPositionString()
                + "&type=" + String.valueOf(_drawingType.GetId())
                + "&schedules=" + GetSchedulesString()
                + "&sockets=" + GetSocketsString()
                + "&temperature=" + _temperatureArea;
    }

    public String GetCommandUpdate() {
        return LucaServerAction.UPDATE_MAP_CONTENT.toString() + String.valueOf(_id)
                + "&position=" + GetPositionString()
                + "&type=" + String.valueOf(_drawingType.GetId())
                + "&schedules=" + GetSchedulesString()
                + "&sockets=" + GetSocketsString()
                + "&temperature=" + _temperatureArea;
    }

    public String GetCommandDelete() {
        return LucaServerAction.DELETE_MAP_CONTENT.toString() + String.valueOf(_id);
    }

    @Override
    public String toString() {
        return "{" + TAG + ": {Id: " + String.valueOf(_id)
                + "}{Position: " + GetPositionString()
                + "}{DrawingType: " + _drawingType.toString()
                + "}{Schedules: " + GetSchedulesString()
                + "}{Sockets: " + GetSocketsString()
                + "}{Temperature: " + _temperatureArea
                + "}{MediaServerIp: " + _mediaServerIp
                + "}{Visibility: " + String.valueOf(_visibility) + "}";
    }
}
