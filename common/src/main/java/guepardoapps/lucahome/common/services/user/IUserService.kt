package guepardoapps.lucahome.common.services.user

import android.content.Context
import guepardoapps.lucahome.common.models.user.User

interface IUserService {
    var initialized: Boolean
    var context: Context?
    var onUserService: OnUserService?

    fun initialize(context: Context)
    fun dispose()

    fun validate(entry: User)
    fun get(): User?
    fun save(entry: User): Long
    fun update(entry: User): Int
    fun delete(entry: User): Int
}