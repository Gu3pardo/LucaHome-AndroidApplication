package guepardoapps.lucahome.bixby.models.shared

import android.support.annotation.NonNull
import guepardoapps.lucahome.bixby.enums.NetworkType
import guepardoapps.lucahome.bixby.enums.StateType
import guepardoapps.lucahome.common.utils.Logger

class NetworkEntity(
        @NonNull var networkType: NetworkType = NetworkType.Null,
        @NonNull var stateType: StateType = StateType.Null,
        @NonNull var wifiSsid: String = "") : IBixbyEntity {
    private val tag = NetworkEntity::class.java.simpleName

    override fun getDatabaseString(): String {
        return "${networkType.ordinal}:${stateType.ordinal}:$wifiSsid"
    }

    override fun getInformationString(): String {
        return "$tag: $networkType -> $wifiSsid and $stateType"
    }

    override fun parseFromDb(databaseString: String) {
        val data: Array<String> = databaseString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (data.size == 3) {
            try {
                networkType = NetworkType.values()[Integer.parseInt(data[0])]
                stateType = StateType.values()[Integer.parseInt(data[1])]
                wifiSsid = data[2]

            } catch (exception: Exception) {
                Logger.instance.error(tag, exception.toString())
                networkType = NetworkType.Null
                stateType = StateType.Null
                wifiSsid = ""
            }

        } else {
            Logger.instance.error(tag, "Invalid data size ${data.size}")
            networkType = NetworkType.Null
            stateType = StateType.Null
            wifiSsid = ""
        }
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"NetworkType\":\"$networkType\",\"StateType\":$stateType,\"WifiSsid\":\"$wifiSsid\"}"
    }
}