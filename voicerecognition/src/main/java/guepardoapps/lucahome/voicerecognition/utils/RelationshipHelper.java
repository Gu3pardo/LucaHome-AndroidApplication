package guepardoapps.lucahome.voicerecognition.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.voicerecognition.classes.RelationActionClass;
import guepardoapps.lucahome.voicerecognition.common.Constants;

@SuppressWarnings({"unused"})
public class RelationshipHelper implements IRelationshipHelper {

    @Override
    public RelationActionClass GetRelationActionClass(@NonNull String result) {
        List<String> resultParameter = Arrays.asList(result.split(" "));

        if (resultParameter.contains(Constants.ParameterSocket)) {
            if (resultParameter.contains(Constants.ActionEnable) || resultParameter.contains(Constants.ParameterOn)) {
                return new RelationActionClass(RelationActionClass.RelationAction.WirelessSocketOn, new ArrayList<>(resultParameter));

            } else if (resultParameter.contains(Constants.ActionDisable) || resultParameter.contains(Constants.ParameterOff)) {
                return new RelationActionClass(RelationActionClass.RelationAction.WirelessSocketOff, new ArrayList<>(resultParameter));

            } else if (resultParameter.contains(Constants.ActionToggle)) {
                return new RelationActionClass(RelationActionClass.RelationAction.WirelessSocketToggle, new ArrayList<>(resultParameter));

            }

        } else if (resultParameter.contains(Constants.ParameterSwitch)) {
            return new RelationActionClass(RelationActionClass.RelationAction.WirelessSwitchToggle, new ArrayList<>(resultParameter));

        } else if (resultParameter.contains(Constants.ParameterWeather)) {
            if (resultParameter.contains(Constants.ParameterCurrent)) {
                return new RelationActionClass(RelationActionClass.RelationAction.WeatherCurrent, new ArrayList<>());

            } else if (resultParameter.contains(Constants.ParameterForecast)) {
                return new RelationActionClass(RelationActionClass.RelationAction.WeatherForecast, new ArrayList<>());

            }

        } else if (resultParameter.contains(Constants.ActionPlay)) {
            if (resultParameter.contains(Constants.ParameterYoutube) || resultParameter.contains(Constants.ParameterVideo)) {
                return new RelationActionClass(RelationActionClass.RelationAction.PlayYoutube, new ArrayList<>());

            } else if (resultParameter.contains(Constants.ParameterRadio)) {
                return new RelationActionClass(RelationActionClass.RelationAction.PlayRadio, new ArrayList<>());

            }

        } else if (resultParameter.contains(Constants.ActionPause)) {
            return new RelationActionClass(RelationActionClass.RelationAction.Pause, new ArrayList<>());

        } else if (resultParameter.contains(Constants.ActionStop)) {
            return new RelationActionClass(RelationActionClass.RelationAction.Stop, new ArrayList<>());

        } else if (resultParameter.contains(Constants.ParameterTemperature)) {
            return new RelationActionClass(RelationActionClass.RelationAction.GetTemperature, new ArrayList<>(resultParameter));

        } else if (resultParameter.contains(Constants.ParameterLight)) {
            return new RelationActionClass(RelationActionClass.RelationAction.GetLight, new ArrayList<>(resultParameter));

        }

        return new RelationActionClass(RelationActionClass.RelationAction.Unknown, new ArrayList<>());
    }

    @Override
    public WirelessSocket GetWirelessSocketByName(ArrayList<String> resultParameter, ArrayList<WirelessSocket> wirelessSocketList) {
        if (resultParameter == null || resultParameter.size() == 0 || wirelessSocketList == null || wirelessSocketList.size() == 0) {
            return null;
        }

        for (int index = 0; index < wirelessSocketList.size(); index++) {
            WirelessSocket wirelessSocket = wirelessSocketList.get(index);
            if (resultParameter.contains(wirelessSocket.GetName())) {
                return wirelessSocket;
            }
        }

        return null;
    }

    @Override
    public WirelessSwitch GetWirelessSwitchByName(ArrayList<String> resultParameter, ArrayList<WirelessSwitch> wirelessSwitchList) {
        if (resultParameter == null || resultParameter.size() == 0 || wirelessSwitchList == null || wirelessSwitchList.size() == 0) {
            return null;
        }

        for (int index = 0; index < wirelessSwitchList.size(); index++) {
            WirelessSwitch wirelessSwitch = wirelessSwitchList.get(index);
            if (resultParameter.contains(wirelessSwitch.GetName())) {
                return wirelessSwitch;
            }
        }

        return null;
    }

    @Override
    public Temperature GetTemperatureByArea(ArrayList<String> resultParameter, ArrayList<Temperature> temperatureList) {
        if (resultParameter == null || resultParameter.size() == 0 || temperatureList == null || temperatureList.size() == 0) {
            return null;
        }

        ArrayList<Room> roomList = RoomService.getInstance().GetDataList();
        ArrayList<UUID> roomUuidList = new ArrayList<>();
        for (Room room : roomList) {
            if (resultParameter.contains(room.GetName())) {
                roomUuidList.add(room.GetUuid());
            }
        }

        for (int index = 0; index < temperatureList.size(); index++) {
            Temperature temperature = temperatureList.get(index);
            if (roomUuidList.contains(temperature.GetRoomUuid())) {
                return temperature;
            }
        }

        return null;
    }

    @Override
    public PuckJs GetPuckJsByArea(ArrayList<String> resultParameter, ArrayList<PuckJs> puckJsList) {
        if (resultParameter == null || resultParameter.size() == 0 || puckJsList == null || puckJsList.size() == 0) {
            return null;
        }

        ArrayList<Room> roomList = RoomService.getInstance().GetDataList();
        ArrayList<UUID> roomUuidList = new ArrayList<>();
        for (Room room : roomList) {
            if (resultParameter.contains(room.GetName())) {
                roomUuidList.add(room.GetUuid());
            }
        }

        for (int index = 0; index < puckJsList.size(); index++) {
            PuckJs puckJs = puckJsList.get(index);
            if (roomUuidList.contains(puckJs.GetRoomUuid())) {
                return puckJs;
            }
        }

        return null;
    }
}
