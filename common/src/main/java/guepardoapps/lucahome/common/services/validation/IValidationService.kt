package guepardoapps.lucahome.common.services.validation

import guepardoapps.lucahome.common.enums.common.ServerAction

interface IValidationService {
    fun mayPerform(action: ServerAction): Pair<Boolean, String>
}