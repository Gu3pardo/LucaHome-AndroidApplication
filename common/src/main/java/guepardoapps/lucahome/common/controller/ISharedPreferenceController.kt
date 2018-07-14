package guepardoapps.lucahome.common.controller

interface ISharedPreferenceController {
    fun contains(key: String): Boolean

    fun <T> save(key: String, value: T): Boolean
    fun <T> load(key: String, defaultValue: T): Any

    fun removeAll(): Boolean
}