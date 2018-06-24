package guepardoapps.lucahome.common.services.common

import android.content.Context
import android.support.annotation.NonNull
import guepardoapps.lucahome.common.models.common.ServiceSettings

interface ILucaService<T> {
    var initialized: Boolean
    var context: Context?

    var serviceSettings: ServiceSettings
    var receiverActivity: Class<*>?

    fun initialize(context: Context)
    fun dispose()

    fun get(): MutableList<T>
    fun search(@NonNull searchValue: String): MutableList<T>

    fun load()

    fun add(@NonNull entry: T, reload: Boolean)
    fun update(@NonNull entry: T, reload: Boolean)
    fun delete(@NonNull entry: T, reload: Boolean)
}