package guepardoapps.lucahome.common.converter.temperature

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.enums.temperature.TemperatureType
import guepardoapps.lucahome.common.extensions.temperature.getJsonKey
import guepardoapps.lucahome.common.extensions.temperature.getPropertyJsonKey
import guepardoapps.lucahome.common.models.temperature.Temperature
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToTemperatureConverter {
    private val tag = JsonDataToTemperatureConverter::class.java.simpleName

    fun parse(jsonResponse: String): ArrayList<Temperature> {
        val list: ArrayList<Temperature> = ArrayList()

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return list
        }

        try {
            val jsonObject = JSONObject(jsonResponse)

            val temperature = Temperature()
            val jsonKey: JsonKey = temperature.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(Temperature::class.java.simpleName)

                val uuidJsonKey = temperature.getPropertyJsonKey(temperature::uuid.name)
                val valueJsonKey = temperature.getPropertyJsonKey(temperature::value.name)
                val areaJsonKey = temperature.getPropertyJsonKey(temperature::area.name)
                val sensorPathJsonKey = temperature.getPropertyJsonKey(temperature::sensorPath.name)
                val graphPathJsonKey = temperature.getPropertyJsonKey(temperature::graphPath.name)

                temperature.uuid = UUID.fromString(data.getString(uuidJsonKey.key))
                temperature.value = data.getDouble(valueJsonKey.key)
                temperature.area = data.getString(areaJsonKey.key)
                temperature.sensorPath = data.getString(sensorPathJsonKey.key)
                temperature.graphPath = data.getString(graphPathJsonKey.key)

                temperature.type = TemperatureType.Raspberry
                temperature.dateTime = Calendar.getInstance()

                list.add(temperature)
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}