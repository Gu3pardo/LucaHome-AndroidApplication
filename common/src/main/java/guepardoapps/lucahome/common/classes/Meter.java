package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;

@SuppressWarnings({"unused"})
public class Meter implements Serializable {
    private static final long serialVersionUID = 5647348598384442492L;
    private static final String TAG = Meter.class.getSimpleName();

    private int _typeId;
    private String _type;
    private String _meterId;
    private String _area;

    private SerializableList<MeterData> _meterDataList;

    public Meter(
            int typeId,
            @NonNull String type,
            @NonNull String meterId,
            @NonNull String area,
            @NonNull SerializableList<MeterData> meterDataList) {
        _typeId = typeId;
        _type = type;
        _meterId = meterId;
        _area = area;
        _meterDataList = meterDataList;
    }

    public int GetTypeId() {
        return _typeId;
    }

    public void SetTypeId(int typeId) {
        _typeId = typeId;
    }

    public String GetType() {
        return _type;
    }

    public void SetType(@NonNull String type) {
        _type = type;
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

    public SerializableList<MeterData> GetMeterDataList() {
        return _meterDataList;
    }

    public void SetMeterDataList(SerializableList<MeterData> meterDataList) {
        _meterDataList = meterDataList;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s: {Type: %s};{TypeId: %d};{MeterId: %s};{Area: %s}}",
                TAG, _type, _typeId, _meterId, _area);
    }
}
