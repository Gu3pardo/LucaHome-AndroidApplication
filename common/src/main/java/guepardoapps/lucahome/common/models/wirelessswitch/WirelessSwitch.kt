package guepardoapps.lucahome.common.models.wirelessswitch

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.common.NetworkType
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.user.UserRole
import java.io.Serializable
import java.util.*

@JsonKey("Data", "WirelessSwitch")
class WirelessSwitch : Serializable {
    private val tag: String = WirelessSwitch::class.java.simpleName

    @JsonKey("", "Uuid")
    var uuid: UUID = UUID.randomUUID()

    @JsonKey("", "RoomUuid")
    var roomUuid: UUID = UUID.randomUUID()

    @JsonKey("", "Name")
    var name: String = ""

    @JsonKey("", "Code")
    var code: String = ""

    @JsonKey("LastTrigger", "DateTime")
    var lastTriggerDateTime: Calendar = Calendar.getInstance()

    @JsonKey("LastTrigger", "User")
    var lastTriggerUser: String = ""

    var isOnServer: Boolean = true
    var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null
    var changeCount: Int = 0
    var showInNotification: Boolean = true

    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandToggleState: String = "${ServerAction.WirelessSwitchToggle.command}$uuid"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandAdd: String = "${ServerAction.WirelessSwitchAdd.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandUpdate: String = "${ServerAction.WirelessSwitchUpdate.command}$uuid&roomuuid=$roomUuid&name=$name&code=$code"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandDelete: String = "${ServerAction.WirelessSwitchDelete.command}$uuid"

    override fun toString(): String {
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"Uuid\":\"$uuid\"," +
                "\"RoomUuid\":\"$roomUuid\"," +
                "\"Name\":\"$name\"," +
                "\"Code\":\"$code\"," +
                "}"
    }
}
