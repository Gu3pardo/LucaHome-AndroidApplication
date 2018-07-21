package guepardoapps.lucahome.common.models.user

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.common.NetworkType
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.user.UserRole
import java.util.*

@JsonKey("Data", "User")
class User {
    private val tag = User::class.java.simpleName

    @JsonKey("", "Uuid")
    var uuid: UUID = UUID.randomUUID()

    @JsonKey("", "Name")
    var name: String = ""

    @JsonKey("", "Password")
    var password: String = ""

    @JsonKey("", "Role")
    var role: UserRole = UserRole.Null

    var isOnServer: Boolean = true
    var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandAdd: String = "${ServerAction.UserAdd.command}$uuid&name=$name&role=$role"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandUpdate: String = "${ServerAction.UserUpdate.command}$uuid&name=$name&role=$role"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandDelete: String = "${ServerAction.UserDelete.command}$uuid"

    override fun toString(): String {
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"Uuid\":\"$uuid\"," +
                "\"Name\":\"$name\"," +
                "\"Password\":\"${password.replace("\\.".toRegex(), "*")}\"," +
                "\"Role\":\"$role\"" +
                "}"
    }
}