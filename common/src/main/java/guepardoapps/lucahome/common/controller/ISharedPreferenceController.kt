package guepardoapps.lucahome.common.controller

interface ISharedPreferenceController {
    fun contains(key: String): Boolean

    fun <T> save(key: String, value: T): Boolean

    fun loadBoolean(key: String): Boolean
    fun loadFloat(key: String): Float
    fun loadInt(key: String): Int
    fun loadLong(key: String): Long
    fun loadString(key: String): String

    fun removeAll(): Boolean
}