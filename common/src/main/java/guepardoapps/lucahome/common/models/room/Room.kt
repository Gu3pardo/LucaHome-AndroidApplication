package guepardoapps.lucahome.common.models.room

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.common.NetworkType
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.room.RoomType
import guepardoapps.lucahome.common.enums.user.UserRole
import java.util.*

@JsonKey("Data", "Room")
class Room {
    @JsonKey("", "Uuid")
    var uuid: UUID = UUID.randomUUID()

    @JsonKey("", "Name")
    var name: String = ""

    @JsonKey("", "Type")
    var type: RoomType = RoomType.Null

    var isOnServer: Boolean = true
    var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandAdd: String = "${ServerAction.RoomAdd.command}$uuid&name=$name&type=${type.ordinal}"

    @NeededUserRole(UserRole.User)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandUpdate: String = "${ServerAction.RoomUpdate.command}$uuid&name=$name&type=${type.ordinal}"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandDelete: String = "${ServerAction.RoomDelete.command}$uuid"
}