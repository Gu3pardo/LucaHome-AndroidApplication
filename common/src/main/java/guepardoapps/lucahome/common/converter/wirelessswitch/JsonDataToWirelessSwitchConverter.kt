package guepardoapps.lucahome.common.converter.wirelessswitch

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.extensions.wirelessswitch.getJsonKey
import guepardoapps.lucahome.common.extensions.wirelessswitch.getPropertyJsonKey
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToWirelessSwitchConverter {
    private val tag = JsonDataToWirelessSwitchConverter::class.java.simpleName

    fun parse(jsonResponse: String): ArrayList<WirelessSwitch> {
        val list: ArrayList<WirelessSwitch> = ArrayList()

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return list
        }

        try {
            val jsonObject = JSONObject(jsonResponse)

            val wirelessSwitch = WirelessSwitch()
            val jsonKey: JsonKey = wirelessSwitch.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(WirelessSwitch::class.java.simpleName)

                val codeJsonKey = wirelessSwitch.getPropertyJsonKey(wirelessSwitch::code.name)
                val nameJsonKey = wirelessSwitch.getPropertyJsonKey(wirelessSwitch::name.name)
                val roomUuidJsonKey = wirelessSwitch.getPropertyJsonKey(wirelessSwitch::roomUuid.name)
                val uuidJsonKey = wirelessSwitch.getPropertyJsonKey(wirelessSwitch::uuid.name)
                val lastTriggerDateTimeJsonKey = wirelessSwitch.getPropertyJsonKey(wirelessSwitch::lastTriggerDateTime.name)
                val lastTriggerUserJsonKey = wirelessSwitch.getPropertyJsonKey(wirelessSwitch::lastTriggerUser.name)

                wirelessSwitch.uuid = UUID.fromString(data.getString(uuidJsonKey.key))
                wirelessSwitch.roomUuid = UUID.fromString(data.getString(roomUuidJsonKey.key))
                wirelessSwitch.name = data.getString(nameJsonKey.key)
                wirelessSwitch.code = data.getString(codeJsonKey.key)

                val lastTrigger: JSONObject = data.getJSONObject(lastTriggerDateTimeJsonKey.parent)

                val lastTriggerDateTimeLong: Long = lastTrigger.getLong(lastTriggerDateTimeJsonKey.key)
                wirelessSwitch.lastTriggerDateTime.timeInMillis = lastTriggerDateTimeLong

                wirelessSwitch.lastTriggerUser = lastTrigger.getString(lastTriggerUserJsonKey.key)

                list.add(wirelessSwitch)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}