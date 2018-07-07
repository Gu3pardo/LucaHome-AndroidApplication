package guepardoapps.lucahome.common.converter.user

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.enums.user.UserRole
import guepardoapps.lucahome.common.extensions.user.getJsonKey
import guepardoapps.lucahome.common.extensions.user.getPropertyJsonKey
import guepardoapps.lucahome.common.models.user.User
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToUserConverter {
    private val tag = JsonDataToUserConverter::class.java.simpleName

    fun parse(jsonResponse: String): User? {
        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return null
        }

        val user = User()
        try {
            val jsonObject = JSONObject(jsonResponse)

            val jsonKey: JsonKey = user.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(User::class.java.simpleName)

                val uuidJsonKey = user.getPropertyJsonKey(user::uuid.name)
                val nameJsonKey = user.getPropertyJsonKey(user::name.name)
                val passwordJsonKey = user.getPropertyJsonKey(user::password.name)
                val roleJsonKey = user.getPropertyJsonKey(user::role.name)

                user.uuid = UUID.fromString(data.getString(uuidJsonKey.key))
                user.name = data.getString(nameJsonKey.key)
                user.password = data.getString(passwordJsonKey.key)
                user.role = UserRole.values()[data.getInt(roleJsonKey.key)]
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return user
        }
    }
}