package guepardoapps.lucahome.bixby.models.requirements

import guepardoapps.lucahome.bixby.models.shared.IBixbyEntity
import guepardoapps.lucahome.common.utils.Logger
import java.util.*

class PositionRequirement : IBixbyEntity {
    private val tag = LightRequirement::class.java.simpleName

    lateinit var puckJsUuid: UUID
    lateinit var puckJsName: String

    override fun getDatabaseString(): String {
        return "$puckJsUuid:$puckJsName"
    }

    override fun getInformationString(): String {
        return "$tag: $puckJsName"
    }

    override fun parseFromDb(databaseString: String) {
        val data: Array<String> = databaseString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (data.size == 2) {
            try {
                puckJsUuid = UUID.fromString(data[0])
                puckJsName = data[1]

            } catch (exception: Exception) {
                Logger.instance.error(tag, exception.toString())
                puckJsUuid = UUID.randomUUID()
                puckJsName = ""
            }

        } else {
            Logger.instance.error(tag, "Invalid data size ${data.size}")
            puckJsUuid = UUID.randomUUID()
            puckJsName = ""
        }
    }

    override fun toString(): String {
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"PuckJsUuid\":\"$puckJsUuid\"," +
                "\"PuckJsName\":\"$puckJsName\"" +
                "}"
    }
}