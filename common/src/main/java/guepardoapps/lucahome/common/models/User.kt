package guepardoapps.lucahome.common.models

import guepardoapps.lucahome.common.annotations.NeededNetwork
import guepardoapps.lucahome.common.enums.NetworkType
import guepardoapps.lucahome.common.enums.UserRole
import java.util.*

@NeededNetwork(NetworkType.HomeWifi)
class User(val uuid: UUID, val name: String, val password: String, val role: UserRole)