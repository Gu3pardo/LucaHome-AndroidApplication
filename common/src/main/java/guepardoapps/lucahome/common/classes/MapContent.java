package guepardoapps.lucahome.common.classes;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.builder.MapContentBuilder;

public class MapContent implements Serializable {
    private static final long serialVersionUID = 8796770534384442492L;
    private static final String TAG = MapContent.class.getSimpleName();

    public enum DrawingType {Null, Raspberry, Arduino, Socket, Temperature, MediaServer, ShoppingList, Menu, Camera, PuckJS}

    private int _id;
    private int[] _position;
    private DrawingType _drawingType;
    private String _temperatureArea;
    private WirelessSocket _socket;
    private SerializableList<Schedule> _scheduleList;
    private Temperature _temperature;
    private boolean _visibility;

    public MapContent(
            int id,
            int[] position,
            @NonNull DrawingType drawingType,
            @NonNull String temperatureArea,
            WirelessSocket socket,
            SerializableList<Schedule> scheduleList,
            Temperature temperature,
            boolean visibility) {
        _id = id;
        _position = position;
        _drawingType = drawingType;
        _temperatureArea = temperatureArea;
        _socket = socket;
        _scheduleList = scheduleList;
        _temperature = temperature;
        _visibility = visibility;
    }

    public int GetId() {
        return _id;
    }

    public int[] GetPosition() {
        return _position;
    }

    public String GetPositionString() {
        return String.format(Locale.getDefault(), "%d|%d", _position[0], _position[1]);
    }

    public void SetPosition(int[] position) {
        _position = position;
    }

    public DrawingType GetDrawingType() {
        return _drawingType;
    }

    public void SetDrawingType(DrawingType drawingType) {
        _drawingType = drawingType;
    }

    public String GetTemperatureArea() {
        return _temperatureArea;
    }

    public void SetTemperatureArea(String temperatureArea) {
        _temperatureArea = temperatureArea;
    }

    public WirelessSocket GetSocket() {
        return _socket;
    }

    public void SetSocket(WirelessSocket socket) {
        _socket = socket;
    }

    public SerializableList<Schedule> GetScheduleList() {
        return _scheduleList;
    }

    public void SetScheduleList(SerializableList<Schedule> scheduleList) {
        _scheduleList = scheduleList;
    }

    public Temperature GetTemperature() {
        return _temperature;
    }

    public void SetTemperature(Temperature temperature) {
        _temperature = temperature;
    }

    public boolean IsVisible() {
        return _visibility;
    }

    public Runnable GetButtonClick(@NonNull Context context) {
        return MapContentBuilder.GetRunnable(_drawingType, _socket, _temperature, context);
    }

    public String ButtonText() {
        return MapContentBuilder.GetButtonText(_drawingType, _socket, _temperature);
    }

    public int Drawable() {
        return MapContentBuilder.GetDrawable(_drawingType, _socket, _temperature);
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "( %s: (Id: %d );(Position: %d|%d );(Type: %s );(TemperatureArea: %s );(Socket: %s );(ScheduleList: %s );(Temperature: %s );(ButtonVisibility: %s ))",
                TAG, _id, _position[0], _position[1], _drawingType, _temperatureArea, _socket, _scheduleList, _temperature, _visibility);
    }
}
