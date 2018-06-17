package guepardoapps.lucahome.common.models

import guepardoapps.lucahome.common.enums.ServerDatabaseAction

import java.io.Serializable
import java.util.UUID

interface ILucaClass : Serializable {
    val uuid: UUID
    var roomUuid: UUID

    var commandAdd: String
    var commandUpdate: String
    var commandDelete: String

    var isOnServer: Boolean
    var serverDatabaseAction: ServerDatabaseAction
}
