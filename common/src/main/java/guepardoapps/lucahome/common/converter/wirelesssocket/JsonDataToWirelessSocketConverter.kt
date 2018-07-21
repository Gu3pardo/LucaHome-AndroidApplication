package guepardoapps.lucahome.common.converter.wirelesssocket

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.extensions.common.toBoolean
import guepardoapps.lucahome.common.extensions.wirelesssocket.getJsonKey
import guepardoapps.lucahome.common.extensions.wirelesssocket.getPropertyJsonKey
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.utils.Logger
import java.util.*

internal class JsonDataToWirelessSocketConverter {
    private val tag = JsonDataToWirelessSocketConverter::class.java.simpleName

    fun parse(jsonResponse: String): ArrayList<WirelessSocket> {
        val list: ArrayList<WirelessSocket> = ArrayList()

        try {
            val parser = Parser()
            val stringBuilder = StringBuilder(jsonResponse)
            val jsonObject = parser.parse(stringBuilder) as JsonObject

            val wirelessSocket = WirelessSocket()
            val jsonKey: JsonKey = wirelessSocket.getJsonKey()
            val dataArray = jsonObject.array<JsonObject>(jsonKey.parent)

            for (index: Int in 0..dataArray!!.size) {
                val value = dataArray[index]
                val data = value.obj(WirelessSocket::class.java.simpleName)

                val stateJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::state.name)
                val codeJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::code.name)
                val nameJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::name.name)
                val roomUuidJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::roomUuid.name)
                val uuidJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::uuid.name)
                val lastTriggerDateTimeJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::lastTriggerDateTime.name)
                val lastTriggerUserJsonKey = wirelessSocket.getPropertyJsonKey(wirelessSocket::lastTriggerUser.name)

                wirelessSocket.uuid = UUID.fromString(data!!.string(uuidJsonKey.key))
                wirelessSocket.roomUuid = UUID.fromString(data.string(roomUuidJsonKey.key))
                wirelessSocket.name = data.string(nameJsonKey.key)!!
                wirelessSocket.code = data.string(codeJsonKey.key)!!
                wirelessSocket.state = data.int(stateJsonKey.key)!!.toBoolean()

                val lastTrigger = data.obj(lastTriggerDateTimeJsonKey.parent)

                val lastTriggerDateTimeLong: Long = lastTrigger!!.long(lastTriggerDateTimeJsonKey.key)!!
                wirelessSocket.lastTriggerDateTime.timeInMillis = lastTriggerDateTimeLong

                wirelessSocket.lastTriggerUser = lastTrigger.string(lastTriggerUserJsonKey.key)!!

                list.add(wirelessSocket)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}