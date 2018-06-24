package guepardoapps.lucahome.common.models.common

import guepardoapps.lucahome.common.enums.ServerDatabaseAction

import java.io.Serializable
import java.util.UUID

interface ILucaClass : Serializable {
    val uuid: UUID

    val commandAdd: String
    val commandUpdate: String
    val commandDelete: String

    var isOnServer: Boolean
    var serverDatabaseAction: ServerDatabaseAction
}
