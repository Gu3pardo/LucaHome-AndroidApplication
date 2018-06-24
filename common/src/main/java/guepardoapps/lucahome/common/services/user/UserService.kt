package guepardoapps.lucahome.common.services.user

import android.annotation.SuppressLint
import android.content.Context
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.converter.user.JsonDataToUserConverter
import guepardoapps.lucahome.common.databases.user.DbUser
import guepardoapps.lucahome.common.enums.DownloadState
import guepardoapps.lucahome.common.enums.ServerAction
import guepardoapps.lucahome.common.models.user.User
import guepardoapps.lucahome.common.utils.Logger

class UserService private constructor() : IUserService {

    private val tag = UserService::class.java.simpleName

    private var converter: JsonDataToUserConverter = JsonDataToUserConverter()
    private var dbHandler: DbUser? = null
    private var downloadAdapter: DownloadAdapter? = null

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: UserService = UserService()
    }

    companion object {
        val instance: UserService by lazy { Holder.instance }
    }

    override var initialized: Boolean = false
        get() = this.context != null && this.dbHandler != null
    override var context: Context? = null
    override var onUserService: OnUserService? = null

    override fun initialize(context: Context) {
        if (this.initialized) {
            return
        }

        this.context = context
        this.downloadAdapter = DownloadAdapter(this.context!!)

        if (dbHandler == null) {
            dbHandler = DbUser(this.context!!, null)
        }
    }

    override fun dispose() {
        this.dbHandler?.close()
    }

    override fun validate(entry: User) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        val actionPath = "${entry.name}&password=${entry.password}&action=${entry.commandValidate}"
        val action = ServerAction.UserValidate

        this.downloadAdapter?.send(
                actionPath,
                action,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == action) {
                            val success = state == DownloadState.Success
                            if (!success) {
                                onUserService!!.validateFinished(false, message)
                                return
                            }

                            val user = converter.parseStringToList(message).firstOrNull()
                            if (user == null) {
                                onUserService!!.validateFinished(false, "Conversion failed")
                                return
                            }

                            val savedUser = get()
                            if (savedUser == null) {
                                save(user)
                                onUserService!!.validateFinished(true, "")
                                return
                            }

                            if (savedUser.uuid == user.uuid) {
                                update(user)
                                onUserService!!.validateFinished(true, "")
                                return
                            }

                            delete(savedUser)
                            save(user)
                            onUserService!!.validateFinished(true, "")
                            return
                        }
                    }
                }
        )
    }

    override fun get(): User? {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return null
        }

        return try {
            this.dbHandler!!.get()
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            null
        }
    }

    override fun save(entry: User): Long {
        return try {
            this.dbHandler!!.add(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    override fun update(entry: User): Int {
        return try {
            this.dbHandler!!.update(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    override fun delete(entry: User): Int {
        return try {
            this.dbHandler!!.delete(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }
}