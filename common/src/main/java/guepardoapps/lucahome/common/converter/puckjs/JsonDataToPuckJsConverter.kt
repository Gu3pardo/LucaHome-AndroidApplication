package guepardoapps.lucahome.common.converter.puckjs

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.extensions.puckjs.getJsonKey
import guepardoapps.lucahome.common.extensions.puckjs.getPropertyJsonKey
import guepardoapps.lucahome.common.models.puckjs.PuckJs
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToPuckJsConverter {
    private val tag = JsonDataToPuckJsConverter::class.java.simpleName

    fun parse(jsonResponse: String): ArrayList<PuckJs> {
        val list: ArrayList<PuckJs> = ArrayList()

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return list
        }

        try {
            val jsonObject = JSONObject(jsonResponse)

            val puckJs = PuckJs()
            val jsonKey: JsonKey = puckJs.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(PuckJs::class.java.simpleName)

                val uuidJsonKey = puckJs.getPropertyJsonKey(puckJs::uuid.name)
                val roomUuidJsonKey = puckJs.getPropertyJsonKey(puckJs::roomUuid.name)
                val nameJsonKey = puckJs.getPropertyJsonKey(puckJs::name.name)
                val macJsonKey = puckJs.getPropertyJsonKey(puckJs::mac.name)

                puckJs.uuid = UUID.fromString(data.getString(uuidJsonKey.key))
                puckJs.roomUuid = UUID.fromString(data.getString(roomUuidJsonKey.key))
                puckJs.name = data.getString(nameJsonKey.key)
                puckJs.mac = data.getString(macJsonKey.key)

                list.add(puckJs)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}