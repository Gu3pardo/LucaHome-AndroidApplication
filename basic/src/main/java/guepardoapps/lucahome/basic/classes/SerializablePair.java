package guepardoapps.lucahome.basic.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class SerializablePair<K, V> implements Serializable {
    private static final String TAG = SerializablePair.class.getSimpleName();

    private K _key;
    private V _value;

    public SerializablePair(@NonNull K key, @NonNull V value) {
        _key = key;
        _value = value;
    }

    public K GetKey() {
        return _key;
    }

    public V GetValue() {
        return _value;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "%s: {Key: %s}{Value: %s}",
                TAG, _key, _value);
    }
}
