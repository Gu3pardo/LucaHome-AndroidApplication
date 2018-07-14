package guepardoapps.lucahome.common.services.common

import android.content.Context
import android.support.annotation.NonNull
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.models.common.ServiceSettings
import io.reactivex.subjects.PublishSubject
import java.util.*

internal interface ILucaService<T> {
    var initialized: Boolean
    var context: Context?

    var serviceSettings: ServiceSettings
    var receiverActivity: Class<*>?

    val responsePublishSubject: PublishSubject<RxResponse>

    fun initialize(context: Context)
    fun dispose()

    fun get(): MutableList<T>
    fun get(uuid: UUID): T?
    fun search(@NonNull searchValue: String): MutableList<T>

    fun load()

    fun add(@NonNull entry: T, reload: Boolean)
    fun update(@NonNull entry: T, reload: Boolean)
    fun delete(@NonNull entry: T, reload: Boolean)
}