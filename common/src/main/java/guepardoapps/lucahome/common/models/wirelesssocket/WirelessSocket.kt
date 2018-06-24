package guepardoapps.lucahome.common.models.wirelesssocket

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.NetworkType
import guepardoapps.lucahome.common.enums.ServerAction
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.UserRole
import guepardoapps.lucahome.common.models.common.ILucaClass
import java.util.*

@JsonKey("Data", "WirelessSocket")
class WirelessSocket(
        @JsonKey("", "Uuid")
        override val uuid: UUID,

        @JsonKey("", "RoomUuid")
        var roomUuid: UUID,

        @JsonKey("", "Name")
        var name: String,

        @JsonKey("", "Code")
        var code: String,

        @JsonKey("", "State")
        var state: Boolean,

        @JsonKey("LastTrigger", "DateTime")
        val lastTriggerDateTime: Calendar,

        @JsonKey("LastTrigger", "User")
        val lastTriggerUser: String,

        override var isOnServer: Boolean = true,
        override var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null,
        var changeCount: Int = 0,
        var showInNotification: Boolean = true)
    : ILucaClass {

    private val tag: String = WirelessSocket::class.java.simpleName

    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandSetState: String = "${ServerAction.WirelessSocketSet.command}$uuid&state=${if (state) "1" else "0"}"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    override val commandAdd: String = "${ServerAction.WirelessSocketAdd.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    override val commandUpdate: String = "${ServerAction.WirelessSocketUpdate.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code&isactivated=${if (state) "1" else "0"}"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    override val commandDelete: String = "${ServerAction.WirelessSocketDelete.command}$uuid"

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
