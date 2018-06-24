package guepardoapps.lucahome.bixby.models.actions

import android.support.annotation.NonNull
import guepardoapps.lucahome.bixby.models.shared.IBixbyEntity
import guepardoapps.lucahome.common.utils.Logger
import java.util.*

class WirelessSwitchAction(
        @NonNull var uuid: UUID = UUID.randomUUID(),
        @NonNull var name: String = "") : IBixbyEntity {
    private val tag = WirelessSwitchAction::class.java.simpleName

    override fun getDatabaseString(): String {
        return "$uuid:$name"
    }

    override fun getInformationString(): String {
        return "$tag: $name"
    }

    override fun parseFromDb(databaseString: String) {
        val data: Array<String> = databaseString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (data.size == 2) {
            try {
                uuid = UUID.fromString(data[0])
                name = data[1]

            } catch (exception: Exception) {
                Logger.instance.error(tag, exception.toString())
                uuid = UUID.randomUUID()
                name = ""
            }

        } else {
            Logger.instance.error(tag, "Invalid data size ${data.size}")
            uuid = UUID.randomUUID()
            name = ""
        }
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"Uuid\":\"$uuid\",\"Name\":\"$name\"}"
    }
}