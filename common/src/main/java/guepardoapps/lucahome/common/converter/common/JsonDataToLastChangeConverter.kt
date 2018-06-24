package guepardoapps.lucahome.common.converter.common

import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToLastChangeConverter : IJsonDataConverter<Calendar> {
    private val tag = JsonDataToLastChangeConverter::class.java.simpleName

    override fun parseStringToList(jsonResponse: String): ArrayList<Calendar> {
        val list: ArrayList<Calendar> = ArrayList()

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return list
        }

        try {
            val jsonObject = JSONObject(jsonResponse)
            val dataArray: JSONArray = jsonObject.getJSONArray("Data")

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val lastChangeDateTimeLong: Long = value.getLong("LastChange")
                val lastChangeDateTime: Calendar = Calendar.getInstance()
                lastChangeDateTime.timeInMillis = lastChangeDateTimeLong
                list.add(lastChangeDateTime)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}