package guepardoapps.lucahome.voicerecognition.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.voicerecognition.classes.RelationActionClass;

public interface IRelationshipHelper {
    RelationActionClass GetRelationActionClass(@NonNull String result);

    WirelessSocket GetWirelessSocketByName(ArrayList<String> resultParameter, ArrayList<WirelessSocket> wirelessSocketList);

    WirelessSwitch GetWirelessSwitchByName(ArrayList<String> resultParameter, ArrayList<WirelessSwitch> wirelessSwitchList);

    Temperature GetTemperatureByArea(ArrayList<String> resultParameter, ArrayList<Temperature> temperatureList);

    PuckJs GetPuckJsByArea(ArrayList<String> resultParameter, ArrayList<PuckJs> puckJsList);
}
