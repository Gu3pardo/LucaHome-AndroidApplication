package guepardoapps.lucahome.common.classes;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class Position implements Serializable {
    private static final long serialVersionUID = 8499902949363002830L;
    private static final String TAG = Position.class.getSimpleName();

    private PuckJs _puckJs;
    private double _lightValue;

    public Position(@NonNull PuckJs puckJs, double lightValue) {
        _puckJs = puckJs;
        _lightValue = lightValue;
    }

    public PuckJs GetPuckJs() {
        return _puckJs;
    }

    public double GetLightValue() {
        return _lightValue;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s: {PuckJs: %s};{LightValue: %.2f}}", TAG, _puckJs, _lightValue);
    }
}
