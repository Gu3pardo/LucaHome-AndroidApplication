package guepardoapps.lucahome.common.converter.wirelesssocket

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.extensions.getJsonKey
import guepardoapps.lucahome.common.extensions.getPropertyJsonKey
import guepardoapps.lucahome.common.extensions.toBoolean
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToWirelessSocketConverter {
    private val tag = JsonDataToWirelessSocketConverter::class.java.simpleName

    fun parse(jsonResponse: String): ArrayList<WirelessSocket> {
        val list: ArrayList<WirelessSocket> = ArrayList()

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return list
        }

        try {
            val jsonObject = JSONObject(jsonResponse)

            val wirelessSocket = WirelessSocket()
            val jsonKey: JsonKey = wirelessSocket.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(WirelessSocket::class.java.simpleName)

                val stateJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::state.name)
                val codeJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::code.name)
                val nameJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::name.name)
                val roomUuidJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::roomUuid.name)
                val uuidJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::uuid.name)
                val lastTriggerDateTimeJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::lastTriggerDateTime.name)
                val lastTriggerUserJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::lastTriggerUser.name)

                wirelessSocket.uuid = UUID.fromString(data.getString(uuidJsonKey.key))
                wirelessSocket.roomUuid = UUID.fromString(data.getString(roomUuidJsonKey.key))
                wirelessSocket.name = data.getString(nameJsonKey.key)
                wirelessSocket.code = data.getString(codeJsonKey.key)
                wirelessSocket.state = data.getInt(stateJsonKey.key).toBoolean()

                val lastTrigger: JSONObject = data.getJSONObject(lastTriggerDateTimeJsonKey.parent)

                val lastTriggerDateTimeLong: Long = lastTrigger.getLong(lastTriggerDateTimeJsonKey.key)
                wirelessSocket.lastTriggerDateTime.timeInMillis = lastTriggerDateTimeLong

                wirelessSocket.lastTriggerUser = lastTrigger.getString(lastTriggerUserJsonKey.key)

                list.add(wirelessSocket)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}