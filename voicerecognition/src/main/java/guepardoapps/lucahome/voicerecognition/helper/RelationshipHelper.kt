package guepardoapps.lucahome.voicerecognition.helper

import guepardoapps.lucahome.common.models.puckjs.PuckJs
import guepardoapps.lucahome.common.models.temperature.Temperature
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch
import guepardoapps.lucahome.common.services.room.RoomService
import guepardoapps.lucahome.voicerecognition.common.Constants
import guepardoapps.lucahome.voicerecognition.enums.Action
import guepardoapps.lucahome.voicerecognition.models.RelationAction
import java.util.*
import kotlin.collections.ArrayList

fun getRelationActionClass(result: String): RelationAction {
    val resultParameter = Arrays.asList(*result.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

    when {
        resultParameter.contains(Constants.ParameterSocket) -> if (resultParameter.contains(Constants.ActionEnable) || resultParameter.contains(Constants.ParameterOn)) {
            return RelationAction(Action.WirelessSocketOn, ArrayList(resultParameter))
        } else if (resultParameter.contains(Constants.ActionDisable) || resultParameter.contains(Constants.ParameterOff)) {
            return RelationAction(Action.WirelessSocketOff, ArrayList(resultParameter))
        } else if (resultParameter.contains(Constants.ActionToggle)) {
            return RelationAction(Action.WirelessSocketToggle, ArrayList(resultParameter))
        }

        resultParameter.contains(Constants.ParameterSwitch) -> return RelationAction(Action.WirelessSwitchToggle, ArrayList(resultParameter))
        resultParameter.contains(Constants.ParameterWeather) -> if (resultParameter.contains(Constants.ParameterCurrent)) {
            return RelationAction(Action.WeatherCurrent, ArrayList())
        } else if (resultParameter.contains(Constants.ParameterForecast)) {
            return RelationAction(Action.WeatherForecast, ArrayList())
        }

        resultParameter.contains(Constants.ActionPlay) -> if (resultParameter.contains(Constants.ParameterYoutube) || resultParameter.contains(Constants.ParameterVideo)) {
            return RelationAction(Action.PlayYoutube, ArrayList())
        } else if (resultParameter.contains(Constants.ParameterRadio)) {
            return RelationAction(Action.PlayRadio, ArrayList())
        }

        resultParameter.contains(Constants.ActionPause) -> return RelationAction(Action.Pause, ArrayList())
        resultParameter.contains(Constants.ActionStop) -> return RelationAction(Action.Stop, ArrayList())
        resultParameter.contains(Constants.ParameterTemperature) -> return RelationAction(Action.GetTemperature, ArrayList(resultParameter))
        resultParameter.contains(Constants.ParameterLight) -> return RelationAction(Action.GetLight, ArrayList(resultParameter))
    }

    return RelationAction(Action.Unknown, ArrayList())
}

fun getWirelessSocketByName(resultParameter: ArrayList<String>?, wirelessSocketList: MutableList<WirelessSocket>?): WirelessSocket? {
    if (resultParameter == null || resultParameter.size == 0 || wirelessSocketList == null || wirelessSocketList.size == 0) {
        return null
    }
    return wirelessSocketList.find { value -> resultParameter.contains(value.name) }
}

fun getWirelessSwitchByName(resultParameter: ArrayList<String>?, wirelessSwitchList: MutableList<WirelessSwitch>?): WirelessSwitch? {
    if (resultParameter == null || resultParameter.size == 0 || wirelessSwitchList == null || wirelessSwitchList.size == 0) {
        return null
    }
    return wirelessSwitchList.find { value -> resultParameter.contains(value.name) }
}

fun getTemperatureByArea(resultParameter: ArrayList<String>?, temperatureList: MutableList<Temperature>?): Temperature? {
    if (resultParameter == null || resultParameter.size == 0 || temperatureList == null || temperatureList.size == 0) {
        return null
    }
    val roomNameList = RoomService.instance.get().map { value -> value.name }
    return temperatureList.find { value -> roomNameList.contains(value.area) }
}

fun getPuckJsByArea(resultParameter: ArrayList<String>?, puckJsList: MutableList<PuckJs>?): PuckJs? {
    if (resultParameter == null || resultParameter.size == 0 || puckJsList == null || puckJsList.size == 0) {
        return null
    }
    val roomUuidList = RoomService.instance.get().map { value -> value.uuid }
    return puckJsList.find { value -> roomUuidList.contains(value.roomUuid) }
}