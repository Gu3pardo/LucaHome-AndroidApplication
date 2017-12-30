package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableDate;
import guepardoapps.lucahome.basic.classes.SerializableTime;
import guepardoapps.lucahome.common.enums.LucaServerAction;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;

public class MeterData implements Serializable {
    private static final long serialVersionUID = 8793748598384442111L;
    private static final String TAG = MeterData.class.getSimpleName();

    public enum ServerAction {Null, Add, Update}

    private int _id;
    private String _type;
    private int _typeId;

    private SerializableDate _saveDate;
    private SerializableTime _saveTime;

    private String _meterId;
    private String _area;
    private double _value;
    private String _imageName;

    private ServerAction _serverAction;
    private ILucaClass.LucaServerDbAction _serverDbAction;

    public MeterData(
            int id,
            @NonNull String type,
            int typeId,

            @NonNull SerializableDate saveDate,
            @NonNull SerializableTime saveTime,

            @NonNull String meterId,
            @NonNull String area,
            double value,
            @NonNull String imageName) {
        _id = id;
        _type = type;
        _typeId = typeId;

        _saveDate = saveDate;
        _saveTime = saveTime;

        _meterId = meterId;
        _area = area;
        _value = value;
        _imageName = imageName;

        _serverAction = ServerAction.Null;
        _serverDbAction = ILucaClass.LucaServerDbAction.Null;
    }

    public int GetId() {
        return _id;
    }

    public void SetId(int id) {
        _id = id;
    }

    public String GetType() {
        return _type;
    }

    public void SetType(@NonNull String type) {
        _type = type;
    }

    public int GetTypeId() {
        return _typeId;
    }

    public void SetTypeId(int typeId) {
        _typeId = typeId;
    }

    public SerializableDate GetSaveDate() {
        return _saveDate;
    }

    public void SetSaveDate(@NonNull SerializableDate saveDate) {
        _saveDate = saveDate;
    }

    public SerializableTime GetSaveTime() {
        return _saveTime;
    }

    public void SetSaveDate(@NonNull SerializableTime saveTime) {
        _saveTime = saveTime;
    }

    public String GetMeterId() {
        return _meterId;
    }

    public void SetMeterId(@NonNull String meterId) {
        _meterId = meterId;
    }

    public String GetArea() {
        return _area;
    }

    public void SetArea(@NonNull String area) {
        _area = area;
    }

    public double GetValue() {
        return _value;
    }

    public void SetValue(double value) {
        _value = value;
    }

    public String GetImageName() {
        return _imageName;
    }

    public void SetImageName(@NonNull String imageName) {
        _imageName = imageName;
    }

    public ServerAction GetServerAction() {
        return _serverAction;
    }

    public void SetServerAction(@NonNull ServerAction serverAction) {
        _serverAction = serverAction;
    }

    public void SetServerDbAction(@NonNull ILucaClass.LucaServerDbAction serverDbAction) {
        _serverDbAction = serverDbAction;
    }

    public ILucaClass.LucaServerDbAction GetServerDbAction() {
        return _serverDbAction;
    }

    public String CommandAdd() {
        return String.format(Locale.getDefault(), "%s%d&type=%s&typeId=%d&day=%d&month=%d&year=%d&hour=%d&minute=%d&meterId=%s&area=%s&value=%.6f&imageName=%s",
                LucaServerAction.ADD_METER_DATA.toString(), _id, _type, _typeId, _saveDate.DayOfMonth(), _saveDate.Month(), _saveDate.Year(), _saveTime.Hour(), _saveTime.Minute(), _meterId, _area, _value, _imageName);
    }

    public String CommandUpdate() {
        return String.format(Locale.getDefault(), "%s%d&type=%s&typeId=%d&day=%d&month=%d&year=%d&hour=%d&minute=%d&meterId=%s&area=%s&value=%.6f&imageName=%s",
                LucaServerAction.UPDATE_METER_DATA.toString(), _id, _type, _typeId, _saveDate.DayOfMonth(), _saveDate.Month(), _saveDate.Year(), _saveTime.Hour(), _saveTime.Minute(), _meterId, _area, _value, _imageName);
    }

    public String CommandDelete() {
        return String.format(Locale.getDefault(), "%s%d", LucaServerAction.DELETE_METER_DATA.toString(), _id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s: {Id: %d};{Type: %s};{TypeId: %d};{SaveDate: %s};{SaveTime: %s};{MeterId: %s};{Area: %s};{Value: %.6f};{ImageName: %s}}",
                TAG, _id, _type, _typeId, _saveDate, _saveTime, _meterId, _area, _value, _imageName);
    }
}
