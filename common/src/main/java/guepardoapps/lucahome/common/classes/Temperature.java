package guepardoapps.lucahome.common.classes;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import guepardoapps.lucahome.common.utils.DrawableCreator;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Temperature implements ILucaClass {
    private static final String Tag = Temperature.class.getSimpleName();

    public enum TemperatureType {Dummy, Raspberry, PuckJs, City}

    private static final int TemperatureCold = 10;
    private static final int TemperatureNormal = 17;
    private static final int TemperatureWarm = 21;
    private static final int TemperatureHot = 25;
    private static final int TemperatureExtremeHot = 35;

    private static final int DrawableDefaultSize = 35;
    private static final int DrawableDefaultPadding = 0;

    private UUID _uuid;
    private UUID _roomUuid;
    private double _temperature;
    private Calendar _dateTime;
    private TemperatureType _temperatureType;
    private String _sensorPath;
    private String _graphPath;

    public Temperature(
            @NonNull UUID uuid,
            @NonNull UUID roomUuid,
            double temperature,
            @NonNull Calendar dateTime,
            @NonNull TemperatureType temperatureType,
            @NonNull String sensorPath,
            @NonNull String graphPath) {
        _uuid = uuid;
        _roomUuid = roomUuid;
        _temperature = temperature;
        _dateTime = dateTime;
        _temperatureType = temperatureType;
        _sensorPath = sensorPath;
        _graphPath = graphPath;
    }

    @Override
    public UUID GetUuid() {
        return _uuid;
    }

    @Override
    public UUID GetRoomUuid() {
        return _roomUuid;
    }

    public void SetTemperature(double temperature) {
        _temperature = temperature;
    }

    public double GetTemperature() {
        return _temperature;
    }

    public String GetTemperatureString() {
        return String.format(Locale.getDefault(), "%.2f " + ((char) 0x00B0) + "C", _temperature);
    }

    public void SetDateTime(@NonNull Calendar dateTime) {
        _dateTime = dateTime;
    }

    public Calendar GetDateTime() {
        return _dateTime;
    }

    public TemperatureType GetTemperatureType() {
        return _temperatureType;
    }

    public String GetSensorPath() {
        return _sensorPath;
    }

    public String GetGraphPath() {
        return _graphPath;
    }

    public Drawable GetDrawable() {
        if (_temperature < TemperatureCold) {
            return DrawableCreator.DrawGradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, Color.WHITE, Color.BLUE, GradientDrawable.OVAL, 0f);

        } else if (_temperature >= TemperatureCold
                && _temperature < TemperatureNormal) {
            return DrawableCreator.DrawCircle(DrawableDefaultSize, DrawableDefaultSize, Color.BLUE, DrawableDefaultPadding);

        } else if (_temperature >= TemperatureNormal
                && _temperature <= TemperatureWarm) {
            return DrawableCreator.DrawCircle(DrawableDefaultSize, DrawableDefaultSize, Color.GREEN, DrawableDefaultPadding);

        } else if (_temperature > TemperatureWarm
                && _temperature < TemperatureHot) {
            return DrawableCreator.DrawCircle(DrawableDefaultSize, DrawableDefaultSize, 0xffa500/*orange*/, DrawableDefaultPadding);

        } else if (_temperature > TemperatureHot
                && _temperature < TemperatureExtremeHot) {
            return DrawableCreator.DrawCircle(DrawableDefaultSize, DrawableDefaultSize, Color.RED, DrawableDefaultPadding);

        } else if (_temperature >= TemperatureExtremeHot) {
            return DrawableCreator.DrawGradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, Color.RED, Color.WHITE, GradientDrawable.OVAL, 0f);
        }

        return DrawableCreator.DrawCircle(DrawableDefaultSize, DrawableDefaultSize, 0x800080/*purple*/, DrawableDefaultPadding);
    }

    @Override
    public String GetCommandAdd() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandAdd not implemented for " + Tag);
    }

    @Override
    public String GetCommandUpdate() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandUpdate not implemented for " + Tag);
    }

    @Override
    public String GetCommandDelete() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetCommandDelete not implemented for " + Tag);
    }

    @Override
    public void SetIsOnServer(boolean isOnServer) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetIsOnServer not implemented for " + Tag);
    }

    @Override
    public boolean GetIsOnServer() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetIsOnServer not implemented for " + Tag);
    }

    @Override
    public void SetServerDbAction(@NonNull LucaServerDbAction lucaServerDbAction) throws NoSuchMethodException {
        throw new NoSuchMethodException("Method SetServerDbAction not implemented for " + Tag);
    }

    @Override
    public LucaServerDbAction GetServerDbAction() throws NoSuchMethodException {
        throw new NoSuchMethodException("Method GetServerDbAction not implemented for " + Tag);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"Uuid\":\"%s\",\"RoomUuid\":\"%s\",\"Temperature\":\"%s\",\"DateTime\":\"%s\",\"TemperatureType\":\"%s\",\"SensorPath\":\"%s\",\"GraphPath\":\"%s\"}",
                Tag, _uuid, _roomUuid, GetTemperatureString(), _dateTime, _temperatureType, _sensorPath, _graphPath);
    }
}
