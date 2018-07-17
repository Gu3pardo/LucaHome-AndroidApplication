package guepardoapps.lucahome.common.services.user

import android.content.Context
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.models.user.User
import io.reactivex.subjects.PublishSubject

interface IUserService {
    var initialized: Boolean
    var context: Context?

    val responsePublishSubject: PublishSubject<RxResponse>

    fun initialize(context: Context)
    fun dispose()

    fun validate(entry: User)
    fun get(): User?
    fun save(entry: User): Long
    fun update(entry: User): Int
    fun delete(entry: User): Int

    fun isValidUserName(userName: String): Boolean
    fun isValidPassword(password: String): Boolean
}