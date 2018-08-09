package guepardoapps.lucahome.common.controller

interface ISharedPreferenceController {
    fun <T> save(key: String, value: T)
    fun <T> load(key: String, defaultValue: T): Any
    fun remove(key: String)
    fun erase()
}