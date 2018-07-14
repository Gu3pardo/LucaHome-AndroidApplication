package guepardoapps.lucahome.common.models.common

import guepardoapps.lucahome.common.enums.common.ServerAction

data class RxResponse(val success: Boolean, val message: String, val action: ServerAction)