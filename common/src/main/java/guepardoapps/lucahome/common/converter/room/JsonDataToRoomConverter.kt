package guepardoapps.lucahome.common.converter.room

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.enums.room.RoomType
import guepardoapps.lucahome.common.extensions.room.getJsonKey
import guepardoapps.lucahome.common.extensions.room.getPropertyJsonKey
import guepardoapps.lucahome.common.models.room.Room
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

internal class JsonDataToRoomConverter {
    private val tag = JsonDataToRoomConverter::class.java.simpleName

    fun parse(jsonResponse: String): ArrayList<Room> {
        val list: ArrayList<Room> = ArrayList()

        try {
            val jsonObject = JSONObject(jsonResponse)

            val room = Room()
            val jsonKey: JsonKey = room.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(Room::class.java.simpleName)

                val uuidJsonKey = room.getPropertyJsonKey(room::uuid.name)
                val nameJsonKey = room.getPropertyJsonKey(room::name.name)
                val typeJsonKey = room.getPropertyJsonKey(room::type.name)

                room.uuid = UUID.fromString(data.getString(uuidJsonKey.key))
                room.name = data.getString(nameJsonKey.key)
                room.type = RoomType.values()[data.getInt(typeJsonKey.key)]

                list.add(room)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}