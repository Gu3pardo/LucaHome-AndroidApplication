package guepardoapps.lucahome.common.converter

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.extensions.getJsonKey
import guepardoapps.lucahome.common.extensions.getPropertyJsonKey
import guepardoapps.lucahome.common.models.WirelessSocket
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

                val stateJsonKey = c.getPropertyJsonKey(c::code.name)
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
                val state: Boolean = data.getString(stateJsonKey.key) == "1"

                val lastTrigger: JSONObject = data.getJSONObject(lastTriggerDateTimeJsonKey.parent)

                val lastTriggerUser: String = lastTrigger.getString(lastTriggerUserJsonKey.key)

                val year = lastTrigger.getInt("Year")
                val month = lastTrigger.getInt("Month")
                val day = lastTrigger.getInt("Day")
                val hour = lastTrigger.getInt("Hour")
                val minute = lastTrigger.getInt("Minute")

                val lastTriggerDateTime: Calendar = Calendar.getInstance()
                lastTriggerDateTime.set(year, month, day, hour, minute)

                list.add(WirelessSocket(uuid, roomUuid, name, code, state, lastTriggerDateTime, lastTriggerUser))
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}