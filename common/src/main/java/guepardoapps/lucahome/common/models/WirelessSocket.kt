package guepardoapps.lucahome.common.models

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.LucaServerActionTypes
import guepardoapps.lucahome.common.enums.NetworkType
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.UserRole
import java.util.*

@JsonKey("Data", "WirelessSocket")
@NeededNetwork(NetworkType.HomeWifi)
class WirelessSocket(
        @JsonKey("", "Uuid")
        override val uuid: UUID,

        @JsonKey("", "RoomUuid")
        override var roomUuid: UUID,

        @JsonKey("", "Name")
        var name: String,

        @JsonKey("", "Code")
        var code: String,

        @JsonKey("", "State")
        var state: Boolean,

        @JsonKey("LastTrigger", "")
        val lastTriggerDateTime: Calendar,

        @JsonKey("LastTrigger", "UserName")
        val lastTriggerUser: String,

        override var isOnServer: Boolean = true,
        override var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null,
        var changeCount: Int = 0,
        var showInNotification: Boolean = true)
    : ILucaClass {

    private val tag: String = WirelessSocket::class.java.simpleName

    @NeededUserRole(UserRole.Guest)
    val commandSetState: String = "${LucaServerActionTypes.SET_WIRELESS_SOCKET.command}$uuid&state=${if (state) "1" else "0"}"

    @NeededUserRole(UserRole.User)
    override val commandAdd: String = "${LucaServerActionTypes.ADD_WIRELESS_SOCKET.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code"

    @NeededUserRole(UserRole.User)
    override val commandUpdate: String = "${LucaServerActionTypes.UPDATE_WIRELESS_SOCKET.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code&isactivated=${if (state) "1" else "0"}"

    @NeededUserRole(UserRole.Administrator)
    override val commandDelete: String = "${LucaServerActionTypes.DELETE_WIRELESS_SOCKET.command}$uuid"

    constructor() : this(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "",
            "",
            false,
            Calendar.getInstance(),
            "")

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"Uuid\":\"$uuid\",\"RoomUuid\":\"$roomUuid\",\"Name\":\"$name\",\"Code\":\"$code\",\"State\":\"${if (state) "1" else "0"}\"}"
    }
}
