package guepardoapps.lucahome.common.services.validation

import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.extensions.enums.getNeededUserRole
import guepardoapps.lucahome.common.models.user.User
import guepardoapps.lucahome.common.services.user.UserService
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONObject

class ValidationService : IValidationService {
    private val tag: String = ValidationService::class.java.simpleName

    companion object {
        private const val noUser: String = "No user available!"
        private const val userMayNotPerform: String = "User may not perform action!"
        private const val noJsonData: String = "No Json data received!"

        private const val keySuccess: String = "Success"
        private const val keyError: String = "Error"
    }

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

    override fun validateDownloadResponse(jsonResponse: String?): Pair<Boolean, String> {
        if (jsonResponse.isNullOrEmpty()) {
            return Pair(false, noJsonData)
        }

        val jsonObject = JSONObject(jsonResponse)
        val success: Boolean = jsonObject.getBoolean(keySuccess)
        val error: String = jsonObject.getString(keyError)

        // val category: String = jsonObject.getString("Category")
        // val action: String = jsonObject.getString("Action")

        return Pair(success, error)
    }
}