package guepardoapps.lucahome.common.models.user

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.NetworkType
import guepardoapps.lucahome.common.enums.ServerAction
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.UserRole
import java.util.*

@JsonKey("Data", "User")
class User {
    private val tag = User::class.java.simpleName

    @JsonKey("", "Uuid")
    lateinit var uuid: UUID

    @JsonKey("", "Name")
    lateinit var name: String

    @JsonKey("", "Password")
    lateinit var password: String

    @JsonKey("", "Role")
    lateinit var role: UserRole

    var isOnServer: Boolean = true
    var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null

    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandValidate: String = ServerAction.UserValidate.command

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