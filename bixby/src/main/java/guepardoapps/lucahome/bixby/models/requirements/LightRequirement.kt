package guepardoapps.lucahome.bixby.models.requirements

import guepardoapps.lucahome.bixby.enums.LightCompareType
import guepardoapps.lucahome.bixby.models.shared.IBixbyEntity
import guepardoapps.lucahome.common.extensions.doubleFormat

import guepardoapps.lucahome.common.utils.Logger

class LightRequirement : IBixbyEntity {
    private val tag = LightRequirement::class.java.simpleName

    lateinit var compareType: LightCompareType
    var compareValue: Double = 0.0
    var toleranceInPercent: Double = 0.0
    lateinit var lightArea: String

    override fun getDatabaseString(): String {
        return "%${compareType.ordinal}:${compareValue.doubleFormat(2)}:${toleranceInPercent.doubleFormat(1)}:$lightArea"
    }

    override fun getInformationString(): String {
        return "$tag: $compareType with ${compareValue.doubleFormat(2)} +/- ${toleranceInPercent.doubleFormat(1)}%%\n$lightArea"
    }

    fun validateActualValue(actualValue: Double): Boolean {
        return when (compareType) {
            LightCompareType.Below -> actualValue < compareValue
            LightCompareType.Above -> actualValue > compareValue
            LightCompareType.Near -> compareValue * (1 + toleranceInPercent / 100) > actualValue && actualValue > compareValue * (1 - toleranceInPercent / 100)
            LightCompareType.Null -> true
        }
    }

    override fun parseFromDb(databaseString: String) {
        val data: Array<String> = databaseString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (data.size == 4) {
            try {
                compareType = LightCompareType.values()[Integer.parseInt(data[0])]
                compareValue = java.lang.Double.parseDouble(data[1])
                toleranceInPercent = java.lang.Double.parseDouble(data[2])
                lightArea = data[3]

            } catch (exception: Exception) {
                Logger.instance.error(tag, exception.toString())
                compareType = LightCompareType.Null
                compareValue = 0.0
                toleranceInPercent = 0.0
                lightArea = ""
            }

        } else {
            Logger.instance.error(tag, "Invalid data size ${data.size}")
            compareType = LightCompareType.Null
            compareValue = 0.0
            toleranceInPercent = 0.0
            lightArea = ""
        }
    }

    override fun toString(): String {
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"CompareType\":\"$compareType\"," +
                "\"CompareValue\":${compareValue.doubleFormat(2)}," +
                "\"ToleranceInPercent\":${toleranceInPercent.doubleFormat(1)}," +
                "\"LightArea\":\"$lightArea\"" +
                "}"
    }
}
