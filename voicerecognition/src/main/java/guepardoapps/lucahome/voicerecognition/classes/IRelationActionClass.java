package guepardoapps.lucahome.voicerecognition.classes;

import java.util.ArrayList;

public interface IRelationActionClass {
    enum RelationAction {
        Unknown,
        WirelessSocketOn, WirelessSocketOff, WirelessSocketToggle,
        WirelessSwitchToggle,
        PlayYoutube, PlayRadio, Pause, Stop,
        WeatherCurrent, WeatherForecast,
        GetLight, GetTemperature
    }

    RelationAction GetRelationAction();

    ArrayList<String> GetActionParameter();
}
