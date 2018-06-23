package guepardoapps.lucahome.common.models

import guepardoapps.lucahome.common.enums.ServerDatabaseAction

import java.io.Serializable
import java.util.UUID

interface ILucaClass : Serializable {
    val uuid: UUID
    var roomUuid: UUID

    val commandAdd: String
    val commandUpdate: String
    val commandDelete: String

    var isOnServer: Boolean
    var serverDatabaseAction: ServerDatabaseAction
}
