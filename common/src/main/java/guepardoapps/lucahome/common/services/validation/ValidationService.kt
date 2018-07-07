package guepardoapps.lucahome.common.services.validation

import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.models.user.User
import guepardoapps.lucahome.common.services.user.UserService
import guepardoapps.lucahome.common.utils.Logger

class ValidationService : IValidationService {
    private val tag: String = ValidationService::class.java.simpleName

    private val noUser: String = "No user available!"
    private val userMayNotPerform: String = "User may not perform action!"

    override fun mayPerform(action: ServerAction): Pair<Boolean, String> {
        val user: User? = UserService.instance.get()
        if (user == null) {
            Logger.instance.warning(tag, noUser)
            return Pair(false, noUser)
        }

        if (user.role < action.getNeededUserRole().role) {
            Logger.instance.warning(tag, userMayNotPerform)
            return Pair(false, userMayNotPerform)
        }

        return Pair(true, "")
    }
}