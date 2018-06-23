package guepardoapps.lucahome.common.services.common

import android.content.Context
import android.support.annotation.NonNull
import java.util.*

interface ILucaService<T> {
    var initialized: Boolean
    var context: Context

    var lastUpdate: Calendar

    var reloadEnabled: Boolean
    var reloadTimeoutMs: Int

    var notificationEnabled: Boolean
    var receiverActivity: Class<*>

    fun initialize(context: Context)
    fun dispose()

    fun get(): MutableList<T>
    fun search(@NonNull searchValue: String): MutableList<T>

    fun load()

    fun add(@NonNull entry: T): Long
    fun update(@NonNull entry: T): Int
    fun delete(@NonNull entry: T): Int
}