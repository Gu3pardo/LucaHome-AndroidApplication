package guepardoapps.lucahome.common.models.temperature

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.enums.temperature.TemperatureType
import guepardoapps.lucahome.common.extensions.common.doubleFormat
import guepardoapps.lucahome.common.R
import java.util.*

@JsonKey("Data", "Temperature")
class Temperature {
    @JsonKey("", "Uuid")
    var uuid: UUID = UUID.randomUUID()

    @JsonKey("", "Value")
    var value: Double = 0.0

    @JsonKey("", "Area")
    var area: String = ""

    @JsonKey("", "SensorPath")
    var sensorPath: String = ""

    @JsonKey("", "GraphPath")
    var graphPath: String = ""

    var type: TemperatureType = TemperatureType.Dummy

    var dateTime: Calendar = Calendar.getInstance()

    val temperatureString = "${value.doubleFormat(2)} ${0x00B0.toChar()}C"

    val drawable: Int
        get() {
            return when {
                value < 12 -> R.drawable.xml_circle_blue
                value in 12.0..15.0 -> R.drawable.xml_circle_red
                value in 15.0..18.0 -> R.drawable.xml_circle_yellow
                value in 18.0..25.0 -> R.drawable.xml_circle_green
                value in 25.0..30.0 -> R.drawable.xml_circle_yellow
                value >= 30 -> R.drawable.xml_circle_red
                else -> R.drawable.xml_circle_white
            }
        }
}