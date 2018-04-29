package guepardoapps.lucahome.common.services;

import android.support.annotation.NonNull;

import guepardoapps.lucahome.common.classes.Temperature;

@SuppressWarnings({"unused"})
public interface ITemperatureService extends ILucaService<Temperature> {
    String TemperatureDownloadFinishedBroadcast = "guepardoapps.lucahome.common.services.temperature.download.finished";
    String TemperatureDownloadFinishedBundle = "TemperatureDownloadFinishedBundle";

    int NotificationId = 64371930;

    void SetActiveTemperature(@NonNull Temperature activeTemperature);

    Temperature GetActiveTemperature();
}
