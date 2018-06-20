package guepardoapps.lucahome.common.models

import guepardoapps.lucahome.common.annotations.DatabaseKey
import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.enums.LucaServerActionTypes
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import java.util.*

@JsonKey("", "WirelessSocket", WirelessSocket::class)
class WirelessSocket(
        @DatabaseKey("uuid", String::class)
        @JsonKey("", "Uuid", UUID::class)
        override val uuid: UUID,

        @DatabaseKey("roomUuid", String::class)
        @JsonKey("", "RoomUuid", UUID::class)
        override var roomUuid: UUID,

        @DatabaseKey("name", String::class)
        @JsonKey("", "Name", String::class)
        var name: String,

        @DatabaseKey("code", String::class)
        @JsonKey("", "Code", String::class)
        var code: String,

        @DatabaseKey("state", Boolean::class)
        @JsonKey("", "State", Boolean::class)
        var state: Boolean,

        @DatabaseKey("lastTriggerDateTime", String::class)
        @JsonKey("LastTrigger", "", Calendar::class)
        val lastTriggerDateTime: Calendar,

        @DatabaseKey("lastTriggerUser", String::class)
        @JsonKey("LastTrigger", "UserName", String::class)
        val lastTriggerUser: String,

        @DatabaseKey("isOnServer", Boolean::class)
        override var isOnServer: Boolean,

        @DatabaseKey("serverDatabaseAction", Int::class)
        override var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null,

        @DatabaseKey("changeCount", Int::class)
        var changeCount: Int = 0,

        @DatabaseKey("showInNotification", Boolean::class)
        var showInNotification: Boolean = true) : ILucaClass {
    private val tag: String = WirelessSocket::class.java.simpleName

    var commandSetState: String
    override lateinit var commandAdd: String
    override lateinit var commandUpdate: String
    override lateinit var commandDelete: String

    init {
        commandSetState = "${LucaServerActionTypes.SET_WIRELESS_SOCKET.command}$uuid&state=${if (state) "1" else "0"}"
        commandAdd = "${LucaServerActionTypes.ADD_WIRELESS_SOCKET.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code"
        commandUpdate = "${LucaServerActionTypes.UPDATE_WIRELESS_SOCKET.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code&isactivated=${if (state) "1" else "0"}"
        commandDelete = "${LucaServerActionTypes.DELETE_WIRELESS_SOCKET.command}$uuid"
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"Uuid\":\"$uuid\",\"RoomUuid\":\"$roomUuid\",\"Name\":\"$name\",\"Code\":\"$code\",\"State\":\"${if (state) "1" else "0"}\"}"
    }
}
