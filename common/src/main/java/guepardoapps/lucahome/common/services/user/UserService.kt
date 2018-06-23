package guepardoapps.lucahome.common.services.user

import android.content.Context
import guepardoapps.lucahome.common.databases.DbUser
import guepardoapps.lucahome.common.models.User
import guepardoapps.lucahome.common.utils.Logger

class UserService private constructor() {

    private val tag = UserService::class.java.simpleName

    private var dbHandler: DbUser? = null

    init {
    }

    private object Holder {
        val instance: UserService = UserService()
    }

    companion object {
        val instance: UserService by lazy { Holder.instance }
    }

    var initialized: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    var context: Context
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    fun initialize(context: Context) {
        if (this.initialized) {
            return
        }

        this.context = context

        if (dbHandler == null) {
            dbHandler = DbUser(this.context, null)
        }

        this.initialized = true
    }

    fun dispose() {
        this.dbHandler?.close()
        this.initialized = false
    }

    fun get(): User? {
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

    fun add(entry: User): Long {
        return try {
            this.dbHandler!!.add(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    fun update(entry: User): Int {
        return try {
            this.dbHandler!!.update(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }

    fun delete(entry: User): Int {
        return try {
            this.dbHandler!!.delete(entry)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            -1
        }
    }
}