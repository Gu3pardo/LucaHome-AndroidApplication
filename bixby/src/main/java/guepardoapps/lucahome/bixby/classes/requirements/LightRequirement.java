package guepardoapps.lucahome.bixby.classes.requirements;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LightRequirement implements IBixbyRequirement {
    private static final String Tag = LightRequirement.class.getSimpleName();

    private LightCompareType _compareType;
    private double _compareValue;
    private double _toleranceInPercent;
    private String _lightArea;

    public LightRequirement(@NonNull LightCompareType compareType, double compareValue, double toleranceInPercent, @NonNull String lightArea) {
        _compareType = compareType;
        _compareValue = compareValue;
        _toleranceInPercent = toleranceInPercent;
        _lightArea = lightArea;
    }

    public LightRequirement() {
        this(LightCompareType.Null, 0, 0, "");
    }

    public LightRequirement(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 4) {
            try {
                _compareType = LightCompareType.values()[Integer.parseInt(data[0])];
                _compareValue = Double.parseDouble(data[1]);
                _toleranceInPercent = Double.parseDouble(data[2]);
                _lightArea = data[3];

            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
                _compareType = LightCompareType.Null;
                _compareValue = 0;
                _toleranceInPercent = 0;
                _lightArea = "";
            }
        } else {
            Logger.getInstance().Error(Tag, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _compareType = LightCompareType.Null;
            _compareValue = 0;
            _toleranceInPercent = 0;
            _lightArea = "";
        }
    }

    public LightCompareType GetCompareType() {
        return _compareType;
    }

    public double GetCompareValue() {
        return _compareValue;
    }

    public double GetToleranceInPercent() {
        return _toleranceInPercent;
    }

    public String GetLightArea() {
        return _lightArea;
    }

    public boolean ValidateActualValue(double actualValue) {
        switch (_compareType) {
            case Below:
                return actualValue < _compareValue;
            case Above:
                return actualValue > _compareValue;
            case Near:
                return _compareValue * (1 + _toleranceInPercent / 100) > actualValue && actualValue > _compareValue * (1 - _toleranceInPercent / 100);
            case Null:
            default:
                return true;
        }
    }

    @Override
    public String GetDatabaseString() {
        return String.format(Locale.getDefault(), "%d:%.2f:%.2f:%s", _compareType.ordinal(), _compareValue, _toleranceInPercent, _lightArea);
    }

    @Override
    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s: %s with %.2f +/- %.1f%%\n%s", Tag, _compareType, _compareValue, _toleranceInPercent, _lightArea);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "{\"Class\":\"%s\",\"CompareType\":\"%s\",\"CompareValue\":%.2f,\"ToleranceInPercent\":%.2f,\"LightArea\":\"%s\"}",
                Tag, _compareType, _compareValue, _toleranceInPercent, _lightArea);
    }
}
