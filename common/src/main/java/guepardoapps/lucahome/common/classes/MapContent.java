package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.R;

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

    private Runnable _runnable;

    public MapContent(
            int id,
            int[] position,
            @NonNull DrawingType drawingType,
            @NonNull String temperatureArea,
            WirelessSocket socket,
            SerializableList<Schedule> scheduleList,
            Temperature temperature,
            boolean visibility,
            @NonNull Runnable runnable) {
        _id = id;
        _position = position;
        _drawingType = drawingType;
        _temperatureArea = temperatureArea;
        _socket = socket;
        _scheduleList = scheduleList;
        _temperature = temperature;
        _visibility = visibility;
        _runnable = runnable;
    }

    public int GetId() {
        return _id;
    }

    public int[] GetPosition() {
        return _position;
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

    public Runnable GetButtonClick() {
        return _runnable;
    }

    public void SetButtonClick(@NonNull Runnable runnable) {
        _runnable = runnable;
    }

    public String ButtonText() {
        switch (_drawingType) {
            case MediaServer:
            case Socket:
                return (_socket != null ? _socket.GetShortName() : "");
            case Temperature:
                return (_temperature != null ? _temperature.GetTemperatureString() : "T");
            case ShoppingList:
                return "S";
            case Menu:
                return "M";
            case Camera:
                return "C";
            case PuckJS:
                return "P";
            case Raspberry:
                return "R";
            case Arduino:
                return "A";
            case Null:
            default:
                return "";
        }
    }

    public int Drawable() {
        switch (_drawingType) {
            case Socket:
                if (_socket == null) {
                    return R.drawable.drawing_socket_off;
                }

                if (_socket.IsActivated()) {
                    return R.drawable.drawing_socket_on;
                } else {
                    return R.drawable.drawing_socket_off;
                }

            case MediaServer:
                if (_socket == null) {
                    return R.drawable.drawing_mediamirror_off;
                }

                if (_socket.IsActivated()) {
                    return R.drawable.drawing_mediamirror_on;
                } else {
                    return R.drawable.drawing_mediamirror_off;
                }

            case Temperature:
                return (_temperature != null ? _temperature.GetDrawable() : R.drawable.drawing_temperature);
            case Raspberry:
                return R.drawable.drawing_raspberry;
            case Arduino:
                return R.drawable.drawing_arduino;
            case ShoppingList:
                return R.drawable.drawing_shoppinglist;
            case Menu:
                return R.drawable.drawing_menu;
            case Camera:
                return R.drawable.drawing_camera;
            case PuckJS:
                return R.drawable.drawing_puckjs;
            case Null:
            default:
                return R.drawable.drawing_socket_off;
        }
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "( %s: (Id: %d );(Position: %d|%d );(Type: %s );(TemperatureArea: %s );(Socket: %s );(ScheduleList: %s );(Temperature: %s );(ButtonVisibility: %s ))",
                TAG, _id, _position[0], _position[1], _drawingType, _temperatureArea, _socket, _scheduleList, _temperature, _visibility);
    }
}
