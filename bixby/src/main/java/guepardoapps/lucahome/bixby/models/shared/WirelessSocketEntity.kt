package guepardoapps.lucahome.bixby.models.shared

import android.support.annotation.NonNull
import guepardoapps.lucahome.bixby.enums.StateType
import guepardoapps.lucahome.common.utils.Logger

class WirelessSocketEntity(
        @NonNull var wirelessSocketName: String = "",
        @NonNull var stateType: StateType = StateType.Null) : IBixbyEntity {
    private val tag = WirelessSocketEntity::class.java.simpleName

    override fun getDatabaseString(): String {
        return "${stateType.ordinal}:$wirelessSocketName"
    }

    override fun getInformationString(): String {
        return "$tag: $wirelessSocketName -> $stateType"
    }

    override fun parseFromDb(databaseString: String) {
        val data: Array<String> = databaseString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (data.size == 4) {
            try {
                stateType = StateType.values()[Integer.parseInt(data[0])]
                wirelessSocketName = data[1]

            } catch (exception: Exception) {
                Logger.instance.error(tag, exception.toString())
                stateType = StateType.Null
                wirelessSocketName = ""
            }

        } else {
            Logger.instance.error(tag, "Invalid data size ${data.size}")
            stateType = StateType.Null
            wirelessSocketName = ""
        }
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"StateType\":$stateType,\"WirelessSocketName\":\"$wirelessSocketName\"}"
    }
}