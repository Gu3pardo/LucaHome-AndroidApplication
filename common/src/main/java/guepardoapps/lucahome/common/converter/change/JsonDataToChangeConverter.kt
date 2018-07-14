package guepardoapps.lucahome.common.converter.change

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.extensions.change.getJsonKey
import guepardoapps.lucahome.common.extensions.change.getPropertyJsonKey
import guepardoapps.lucahome.common.models.change.Change
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

internal class JsonDataToChangeConverter {
    private val tag = JsonDataToChangeConverter::class.java.simpleName

    fun parse(jsonResponse: String): ArrayList<Change> {
        val list: ArrayList<Change> = ArrayList()

        try {
            val jsonObject = JSONObject(jsonResponse)

            val change = Change()
            val jsonKey: JsonKey = change.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(Change::class.java.simpleName)

                val uuidJsonKey = change.getPropertyJsonKey(change::uuid.name)
                val typeJsonKey = change.getPropertyJsonKey(change::type.name)
                val userNameJsonKey = change.getPropertyJsonKey(change::userName.name)
                val timeJsonKey = change.getPropertyJsonKey(change::time.name)

                change.uuid = UUID.fromString(data.getString(uuidJsonKey.key))
                change.type = data.getString(typeJsonKey.key)
                change.userName = data.getString(userNameJsonKey.key)
                change.time.timeInMillis = data.getLong(timeJsonKey.key)

                list.add(change)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}