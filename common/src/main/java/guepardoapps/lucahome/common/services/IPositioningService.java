package guepardoapps.lucahome.common.services;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.classes.Position;

public interface IPositioningService {
    String PositioningCalculationFinishedBroadcast = "guepardoapps.lucahome.common.services.positioning.calculation.finished";
    String PositioningCalculationFinishedBundle = "PositioningCalculationFinishedBundle";

    void SetActiveActivityContext(@NonNull Context activeActivityContext);

    Context GetActiveActivityContext();

    Position GetCurrentPosition();

    void SetHandleBluetoothAutomatically(boolean handleBluetoothAutomatically);

    boolean GetHandleBluetoothAutomatically();

    void SetScanEnabled(boolean scanEnabled);

    boolean GetScanEnabled();

    void SetBetweenScanPeriod(long betweenScanPeriod);

    long GetBackgroundBetweenScanPeriod();

    long GetForegroundBetweenScanPeriod();

    void SetScanPeriod(long scanPeriod);

    long GetBackgroundScanPeriod();

    long GetForegroundScanPeriod();
}
