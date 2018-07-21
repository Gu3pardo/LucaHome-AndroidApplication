package guepardoapps.lucahome.common.services.user

import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.databases.user.DbUser
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.models.user.User
import io.reactivex.subjects.PublishSubject

interface IUserService {
    var initialized: Boolean
    val responsePublishSubject: PublishSubject<RxResponse>

    fun initialize(downloadAdapter: DownloadAdapter, dbHandler: DbUser)
    fun dispose()

    fun validate(entry: User)
    fun get(): User?
    fun save(entry: User): Long
    fun update(entry: User): Int
    fun delete(entry: User): Int

    fun isValidUserName(userName: String): Boolean
    fun isValidPassword(password: String): Boolean
}