package guepardoapps.lucahome.common.models.puckjs

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.common.NetworkType
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.user.UserRole
import java.util.*

@JsonKey("Data", "PuckJs")
data class PuckJs(
        @JsonKey("", "Uuid")
        var uuid: UUID = UUID.randomUUID(),

        @JsonKey("", "RoomUuid")
        var roomUuid: UUID = UUID.randomUUID(),

        @JsonKey("", "Name")
        var name: String = "",

        @JsonKey("", "Mac")
        var mac: String = "",

        var isOnServer: Boolean = true,
        var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null) {

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandAdd: String = "${ServerAction.PuckJsAdd.command}$uuid&roomuuid=$roomUuid&name=$name&mac=$mac"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandUpdate: String = "${ServerAction.PuckJsUpdate.command}$uuid&roomuuid=$roomUuid&name=$name&mac=$mac"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandDelete: String = "${ServerAction.PuckJsDelete.command}$uuid"
}