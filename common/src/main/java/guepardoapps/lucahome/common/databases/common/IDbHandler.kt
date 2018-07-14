package guepardoapps.lucahome.common.databases.common

import guepardoapps.lucahome.common.models.common.ServiceSettings
import java.util.*

interface IDbHandler<T> {
    fun getList(): MutableList<T>
    fun get(uuid: UUID): T?
    fun add(entity: T): Long
    fun update(entity: T): Int
    fun delete(entity: T): Int

    fun getLastChangeDateTime(): Calendar?
    fun setLastChangeDateTime(entity: Calendar): Long

    fun getServiceSettings(): ServiceSettings?
    fun setServiceSettings(entity: ServiceSettings): Long
}