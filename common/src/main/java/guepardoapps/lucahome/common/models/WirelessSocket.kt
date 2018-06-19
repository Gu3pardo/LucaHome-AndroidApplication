package guepardoapps.lucahome.common.models

import guepardoapps.lucahome.common.enums.LucaServerActionTypes
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import java.util.*

class WirelessSocket(override val uuid: UUID,
                     override var roomUuid: UUID,
                     var name: String,
                     var code: String,
                     var state: Boolean,
                     val lastTriggerDateTime: Calendar,
                     val lastTriggerUser: String,
                     override var isOnServer: Boolean,
                     override var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null) : ILucaClass {
    private val tag: String = WirelessSocket::class.java.simpleName

    private val settingsHeader: String = "SharedPref_Notification_Socket_"
    var settingsKey: String

    var commandSetState: String
    override lateinit var commandAdd: String
    override lateinit var commandUpdate: String
    override lateinit var commandDelete: String

    init {
        settingsKey = "$settingsHeader$uuid"
        commandSetState = "${LucaServerActionTypes.SET_WIRELESS_SOCKET.command}$uuid&state=${if (state) "1" else "0"}"
        commandAdd = "${LucaServerActionTypes.ADD_WIRELESS_SOCKET.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code"
        commandUpdate = "${LucaServerActionTypes.UPDATE_WIRELESS_SOCKET.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code&isactivated=${if (state) "1" else "0"}"
        commandDelete = "${LucaServerActionTypes.DELETE_WIRELESS_SOCKET.command}$uuid"
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"Uuid\":\"$uuid\",\"RoomUuid\":\"$roomUuid\",\"Name\":\"$name\",\"Code\":\"$code\",\"State\":\"${if (state) "1" else "0"}\"}"
    }
}
