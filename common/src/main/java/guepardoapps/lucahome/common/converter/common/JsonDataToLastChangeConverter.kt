package guepardoapps.lucahome.common.converter.common

import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToLastChangeConverter {
    private val tag = JsonDataToLastChangeConverter::class.java.simpleName

    fun parse(jsonResponse: String): Calendar? {

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return null
        }

        val lastChangeDateTime: Calendar = Calendar.getInstance()
        try {
            val jsonObject = JSONObject(jsonResponse)
            val dataArray: JSONArray = jsonObject.getJSONArray("Data")

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val lastChangeDateTimeLong: Long = value.getLong("LastChange")
                lastChangeDateTime.timeInMillis = lastChangeDateTimeLong
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return lastChangeDateTime
        }
    }
}