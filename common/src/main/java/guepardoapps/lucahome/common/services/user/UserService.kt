package guepardoapps.lucahome.common.services.user

import android.annotation.SuppressLint
import android.content.Context
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.constants.Labels
import guepardoapps.lucahome.common.converter.user.JsonDataToUserConverter
import guepardoapps.lucahome.common.databases.user.DbUser
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.models.user.User
import guepardoapps.lucahome.common.utils.Logger
import io.reactivex.subjects.PublishSubject

class UserService private constructor() : IUserService {
    private val tag = UserService::class.java.simpleName

    private var converter: JsonDataToUserConverter = JsonDataToUserConverter()
    private var dbHandler: DbUser? = null
    private var downloadAdapter: DownloadAdapter? = null

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: UserService = UserService()
    }

    companion object {
        val instance: UserService by lazy { Holder.instance }

        const val minUserNameLength: Int = 4
        const val minPasswordLength: Int = 6

        val userNameRegex: Regex = Regex("[a-zA-Z]+")
        val passwordRegex: Regex = Regex("[a-zA-Z0-9]+")
    }

    override var initialized: Boolean = false
        get() = context != null && dbHandler != null
    override var context: Context? = null

    override val responsePublishSubject: PublishSubject<RxResponse> = PublishSubject.create<RxResponse>()!!

    override fun initialize(context: Context) {
        if (initialized) {
            return
        }

        this.context = context
        downloadAdapter = DownloadAdapter(this.context!!)

        if (dbHandler == null) {
            dbHandler = DbUser(this.context!!)
        }
    }

    override fun dispose() {
        dbHandler?.close()
        context = null
        dbHandler = null
    }

    override fun validate(entry: User) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.UserValidate))
            return
        }

        downloadAdapter?.send(
                ServerAction.UserValidate.command,
                ServerAction.UserValidate,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.UserValidate) {
                            val success = state == DownloadState.Success
                            if (!success) {
                                responsePublishSubject.onNext(RxResponse(false, message, ServerAction.UserValidate))
                                return
                            }

                            val user = converter.parse(message)
                            if (user == null) {
                                responsePublishSubject.onNext(RxResponse(false, Labels.Converter.conversionFailed, ServerAction.UserValidate))
                                return
                            }

                            val savedUser = get()
                            if (savedUser == null) {
                                save(user)
                                responsePublishSubject.onNext(RxResponse(true, message, ServerAction.UserValidate))
                                return
                            }

                            if (savedUser.uuid == user.uuid) {
                                update(user)
                                responsePublishSubject.onNext(RxResponse(true, message, ServerAction.UserValidate))
                                return
                            }

                            delete(savedUser)
                            save(user)
                            responsePublishSubject.onNext(RxResponse(true, message, ServerAction.UserValidate))
                            return
                        }
                    }
                }
        )
    }

    override fun get(): User? {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return null
        }

        return try {
            dbHandler!!.get()
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            null
        }
    }

    override fun save(entry: User): Long {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return -1
        }

        return try {
            dbHandler!!.add(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    override fun update(entry: User): Int {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return -1
        }

        return try {
            dbHandler!!.update(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    override fun delete(entry: User): Int {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return -1
        }

        return try {
            dbHandler!!.delete(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    override fun isValidUserName(userName: String): Boolean {
        return userName.length >= minUserNameLength && userName.matches(userNameRegex)
    }

    override fun isValidPassword(password: String): Boolean {
        return password.length >= minPasswordLength && password.matches(passwordRegex)
    }
}