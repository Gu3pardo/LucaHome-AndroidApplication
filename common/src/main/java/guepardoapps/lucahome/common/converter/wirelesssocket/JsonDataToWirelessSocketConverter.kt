package guepardoapps.lucahome.common.converter.wirelesssocket

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.converter.common.IJsonDataConverter
import guepardoapps.lucahome.common.extensions.getJsonKey
import guepardoapps.lucahome.common.extensions.getPropertyJsonKey
import guepardoapps.lucahome.common.extensions.toBoolean
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToWirelessSocketConverter : IJsonDataConverter<WirelessSocket> {
    private val tag = JsonDataToWirelessSocketConverter::class.java.simpleName

    override fun parseStringToList(jsonResponse: String): ArrayList<WirelessSocket> {
        val list: ArrayList<WirelessSocket> = ArrayList()

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return list
        }

        try {
            val jsonObject = JSONObject(jsonResponse)

            val c = WirelessSocket()
            val jsonKey: JsonKey = c.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(WirelessSocket::class.java.simpleName)

                val stateJsonKey = c.getPropertyJsonKey(c::state.name)
                val codeJsonKey = c.getPropertyJsonKey(c::code.name)
                val nameJsonKey = c.getPropertyJsonKey(c::name.name)
                val roomUuidJsonKey = c.getPropertyJsonKey(c::roomUuid.name)
                val uuidJsonKey = c.getPropertyJsonKey(c::uuid.name)
                val lastTriggerDateTimeJsonKey = c.getPropertyJsonKey(c::lastTriggerDateTime.name)
                val lastTriggerUserJsonKey = c.getPropertyJsonKey(c::lastTriggerUser.name)

                val uuid: UUID = UUID.fromString(data.getString(uuidJsonKey.key))
                val roomUuid: UUID = UUID.fromString(data.getString(roomUuidJsonKey.key))
                val name: String = data.getString(nameJsonKey.key)
                val code: String = data.getString(codeJsonKey.key)
                val state: Boolean = data.getInt(stateJsonKey.key).toBoolean()

                val lastTrigger: JSONObject = data.getJSONObject(lastTriggerDateTimeJsonKey.parent)

                val lastTriggerDateTimeLong: Long = lastTrigger.getLong(lastTriggerDateTimeJsonKey.key)
                val lastTriggerDateTime: Calendar = Calendar.getInstance()
                lastTriggerDateTime.timeInMillis = lastTriggerDateTimeLong

                val lastTriggerUser: String = lastTrigger.getString(lastTriggerUserJsonKey.key)

                list.add(WirelessSocket(uuid, roomUuid, name, code, state, lastTriggerDateTime, lastTriggerUser))
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}