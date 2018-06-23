package guepardoapps.lucahome.common.services.wirelesssocket

import android.content.Context
import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.databases.DbWirelessSocket
import guepardoapps.lucahome.common.enums.DownloadState
import guepardoapps.lucahome.common.enums.DownloadType
import guepardoapps.lucahome.common.enums.LucaServerActionTypes
import guepardoapps.lucahome.common.models.User
import guepardoapps.lucahome.common.models.WirelessSocket
import guepardoapps.lucahome.common.services.common.ILucaService
import guepardoapps.lucahome.common.services.user.UserService
import guepardoapps.lucahome.common.utils.Logger
import java.util.*
import kotlin.collections.ArrayList

class WirelessSocketService private constructor() : ILucaService<WirelessSocket> {

    private val tag = WirelessSocketService::class.java.simpleName

    private var dbHandler: DbWirelessSocket? = null
    private var downloadAdapter: DownloadAdapter? = null

    init {
    }

    private object Holder {
        val instance: WirelessSocketService = WirelessSocketService()
    }

    companion object {
        val instance: WirelessSocketService by lazy { Holder.instance }
    }

    override var initialized: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var context: Context
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var lastUpdate: Calendar
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var reloadEnabled: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var reloadTimeoutMs: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var notificationEnabled: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var receiverActivity: Class<*>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    var onWirelessSocketService: OnWirelessSocketService
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun initialize(context: Context) {
        if (this.initialized) {
            return
        }

        this.context = context

        if (dbHandler == null) {
            dbHandler = DbWirelessSocket(this.context, null)
        }

        this.downloadAdapter = DownloadAdapter(this.context)

        this.initialized = true
    }

    override fun dispose() {
        this.dbHandler?.close()
        this.initialized = false
    }

    override fun get(): MutableList<WirelessSocket> {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return ArrayList()
        }

        return try {
            this.dbHandler!!.loadList()
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            ArrayList()
        }
    }

    override fun search(searchValue: String): MutableList<WirelessSocket> {
        val list = this.get()
        val searchResultList = ArrayList<WirelessSocket>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    fun setState(entry: WirelessSocket, newState: Boolean) {

    }

    fun deactivateAll() {
        val user: User? = UserService.instance.get()
        if (user == null) {
            Logger.instance.warning(tag, "No user!")
            return
        }

        val serverIp = context.getString(R.string.server_ip)
        val libActionPath = context.getString(R.string.raspberry_pi_lib_action)
        val requestUrl = "$serverIp$libActionPath${user.name}&password=${user.password}&action=${LucaServerActionTypes.DEACTIVATE_ALL_WIRELESS_SOCKETS}"

        this.downloadAdapter?.send(
                requestUrl,
                DownloadType.WirelessSocketSet,
                // TODO get from extension and annotation
                true,
                object : OnDownloadAdapter {
                    override fun onFinished(type: DownloadType, state: DownloadState, message: String) {
                        if (type == DownloadType.WirelessSocketSet) {
                            onWirelessSocketService.setFinished(state == DownloadState.Success, message)
                        }
                    }
                }
        )
    }

    override fun load() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(entry: WirelessSocket): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(entry: WirelessSocket): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(entry: WirelessSocket): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}