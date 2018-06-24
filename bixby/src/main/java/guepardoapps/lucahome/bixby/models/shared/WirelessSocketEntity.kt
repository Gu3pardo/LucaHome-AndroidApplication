package guepardoapps.lucahome.bixby.models.shared

import android.support.annotation.NonNull
import guepardoapps.lucahome.bixby.enums.StateType
import guepardoapps.lucahome.common.utils.Logger
import java.util.*

class WirelessSocketEntity(
        @NonNull var uuid: UUID = UUID.randomUUID(),
        @NonNull var name: String = "",
        @NonNull var stateType: StateType = StateType.Null) : IBixbyEntity {
    private val tag = WirelessSocketEntity::class.java.simpleName

    override fun getDatabaseString(): String {
        return "${stateType.ordinal}:$uuid:$name"
    }

    override fun getInformationString(): String {
        return "$tag: $name -> $stateType"
    }

    override fun parseFromDb(databaseString: String) {
        val data: Array<String> = databaseString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (data.size == 3) {
            try {
                stateType = StateType.values()[Integer.parseInt(data[0])]
                uuid = UUID.fromString(data[1])
                name = data[2]

            } catch (exception: Exception) {
                Logger.instance.error(tag, exception.toString())
                stateType = StateType.Null
                uuid = UUID.randomUUID()
                name = ""
            }

        } else {
            Logger.instance.error(tag, "Invalid data size ${data.size}")
            stateType = StateType.Null
            uuid = UUID.randomUUID()
            name = ""
        }
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"StateType\":$stateType,\"Uuid\":\"$uuid\",\"Name\":\"$name\"}"
    }
}