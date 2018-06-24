package guepardoapps.lucahome.common.converter.user

import guepardoapps.lucahome.common.annotations.JsonKey
import guepardoapps.lucahome.common.converter.common.IJsonDataConverter
import guepardoapps.lucahome.common.enums.UserRole
import guepardoapps.lucahome.common.extensions.getJsonKey
import guepardoapps.lucahome.common.extensions.getPropertyJsonKey
import guepardoapps.lucahome.common.models.user.User
import guepardoapps.lucahome.common.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class JsonDataToUserConverter : IJsonDataConverter<User> {
    private val tag = JsonDataToUserConverter::class.java.simpleName

    override fun parseStringToList(jsonResponse: String): ArrayList<User> {
        val list: ArrayList<User> = ArrayList()

        if (jsonResponse.contains("Error")) {
            Logger.instance.error(tag, "Found error parsing: $jsonResponse")
            return list
        }

        try {
            val jsonObject = JSONObject(jsonResponse)

            val c = User()
            val jsonKey: JsonKey = c.getJsonKey()
            val dataArray: JSONArray = jsonObject.getJSONArray(jsonKey.parent)

            for (index: Int in 0..dataArray.length()) {
                val value: JSONObject = dataArray.getJSONObject(index)
                val data: JSONObject = value.getJSONObject(User::class.java.simpleName)

                val uuidJsonKey = c.getPropertyJsonKey(c::uuid.name)
                val nameJsonKey = c.getPropertyJsonKey(c::name.name)
                val passwordJsonKey = c.getPropertyJsonKey(c::password.name)
                val roleJsonKey = c.getPropertyJsonKey(c::role.name)

                val uuid: UUID = UUID.fromString(data.getString(uuidJsonKey.key))
                val name: String = data.getString(nameJsonKey.key)
                val password: String = data.getString(passwordJsonKey.key)
                val role: UserRole = UserRole.values()[data.getInt(roleJsonKey.key)]

                list.add(User(uuid, name, password, role))
            }
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
        } finally {
            return list
        }
    }
}