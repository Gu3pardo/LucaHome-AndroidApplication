package guepardoapps.lucahome.common.models.user

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.annotations.NeededUserRole
import guepardoapps.lucahome.common.enums.NetworkType
import guepardoapps.lucahome.common.enums.ServerAction
import guepardoapps.lucahome.common.enums.ServerDatabaseAction
import guepardoapps.lucahome.common.enums.UserRole
import guepardoapps.lucahome.common.models.common.ILucaClass
import java.util.*

@JsonKey("Data", "User")
class User(
        @JsonKey("", "Uuid")
        override val uuid: UUID,

        @JsonKey("", "Name")
        val name: String,

        @JsonKey("", "Password")
        val password: String,

        @JsonKey("", "Role")
        val role: UserRole,

        override var isOnServer: Boolean = true,
        override var serverDatabaseAction: ServerDatabaseAction = ServerDatabaseAction.Null)
    : ILucaClass {
    private val tag = User::class.java.simpleName

    @NeededUserRole(UserRole.Guest)
    @NeededNetwork(NetworkType.HomeWifi)
    val commandValidate: String = ServerAction.UserValidate.command

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    override val commandAdd: String = "${ServerAction.UserAdd.command}$uuid&name=$name&role=$role"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    override val commandUpdate: String = "${ServerAction.UserUpdate.command}$uuid&name=$name&role=$role"

    @NeededUserRole(UserRole.Administrator)
    @NeededNetwork(NetworkType.HomeWifi)
    override val commandDelete: String = "${ServerAction.UserDelete.command}$uuid"

    constructor() : this(UUID.randomUUID(), "", "", UserRole.Guest)

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"Uuid\":\"$uuid\",\"Name\":\"$name\",\"Password\":\"${password.replace("\\.".toRegex(), "*")}\",\"Role\":\"$role\"}"
    }
}