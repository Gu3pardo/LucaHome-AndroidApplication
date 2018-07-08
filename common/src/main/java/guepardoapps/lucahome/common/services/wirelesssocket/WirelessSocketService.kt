package guepardoapps.lucahome.common.services.wirelesssocket

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.controller.NotificationController
import guepardoapps.lucahome.common.converter.common.JsonDataToLastChangeConverter
import guepardoapps.lucahome.common.converter.wirelesssocket.JsonDataToWirelessSocketConverter
import guepardoapps.lucahome.common.databases.wirelesssocket.DbWirelessSocket
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.utils.Logger
import guepardoapps.lucahome.common.worker.wirelesssocket.WirelessSocketWorker
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class WirelessSocketService private constructor() : IWirelessSocketService {
    private val tag = WirelessSocketService::class.java.simpleName

    private val notificationId = 438502134

    private var converter: JsonDataToWirelessSocketConverter = JsonDataToWirelessSocketConverter()
    private var lastChangeConverter: JsonDataToLastChangeConverter = JsonDataToLastChangeConverter()

    private var dbHandler: DbWirelessSocket? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var notificationController: NotificationController

    private lateinit var reloadWork: PeriodicWorkRequest
    private lateinit var reloadWorkId: UUID

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: WirelessSocketService = WirelessSocketService()
    }

    companion object {
        val instance: WirelessSocketService by lazy { Holder.instance }
    }

    override var initialized: Boolean = false
        get() = this.context != null && this.dbHandler != null
    override var context: Context? = null
    override var onWirelessSocketService: OnWirelessSocketService? = null

    override var serviceSettings: ServiceSettings
        get() = this.dbHandler!!.getServiceSettings()!!
        set(value) {
            this.dbHandler!!.setServiceSettings(value)

            if (value.reloadEnabled) {
                WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
                this.reloadWork = PeriodicWorkRequestBuilder<WirelessSocketWorker>(value.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
                this.reloadWorkId = this.reloadWork.id
                WorkManager.getInstance()?.enqueue(this.reloadWork)
            } else {
                WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
            }

            if (value.notificationEnabled && receiverActivity != null) {
                this.showNotification()
            } else {
                this.closeNotification()
            }
        }

    override var receiverActivity: Class<*>? = null
        set(value) {
            if (value != null && dbHandler?.getServiceSettings()!!.notificationEnabled) {
                this.showNotification()
            } else {
                this.closeNotification()
            }
        }

    override fun initialize(context: Context) {
        if (this.initialized) {
            return
        }

        this.context = context
        this.downloadAdapter = DownloadAdapter(this.context!!)
        this.notificationController = NotificationController(this.context!!)

        if (this.dbHandler == null) {
            this.dbHandler = DbWirelessSocket(this.context!!, null)
        }

        if (this.serviceSettings.reloadEnabled) {
            this.reloadWork = PeriodicWorkRequestBuilder<WirelessSocketWorker>(this.serviceSettings.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
            this.reloadWorkId = this.reloadWork.id
            WorkManager.getInstance()?.enqueue(this.reloadWork)
        }
    }

    override fun dispose() {
        WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
        this.dbHandler?.close()

        this.context = null
        this.dbHandler = null
    }

    override fun get(): MutableList<WirelessSocket> {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return ArrayList()
        }

        return try {
            this.dbHandler!!.getList()
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            ArrayList()
        }
    }

    override fun get(uuid: UUID): WirelessSocket? {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return null
        }

        return try {
            this.dbHandler!!.get(uuid)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            null
        }
    }

    override fun search(searchValue: String): MutableList<WirelessSocket> {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return ArrayList()
        }

        val list = this.get()
        val searchResultList = ArrayList<WirelessSocket>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    override fun setState(entry: WirelessSocket, newState: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        entry.state = newState
        this.downloadAdapter?.send(
                entry.commandSetState,
                ServerAction.WirelessSocketSet,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketSet) {
                            onWirelessSocketService!!.setFinished(state == DownloadState.Success, message)

                            entry.changeCount++
                            dbHandler?.update(entry)
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun deactivateAll() {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                ServerAction.WirelessSocketDeactivateAll.command,
                ServerAction.WirelessSocketDeactivateAll,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketDeactivateAll) {
                            onWirelessSocketService!!.setFinished(state == DownloadState.Success, message)

                            get().forEach { value -> value.changeCount++; dbHandler?.update(value) }
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun load() {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        val savedList = dbHandler?.getList()!!
        val updateList = savedList.filter { wirelessSocket -> !wirelessSocket.isOnServer }
        for (entry in updateList) {
            when (entry.serverDatabaseAction) {
                ServerDatabaseAction.Add -> add(entry, false)
                ServerDatabaseAction.Update -> update(entry, false)
                ServerDatabaseAction.Delete -> delete(entry, false)
                ServerDatabaseAction.Null -> {
                    Logger.instance.warning(tag, "Entry is marked as not on server, but has action Null: $entry")
                }
            }
        }

        this.downloadAdapter?.send(
                ServerAction.WirelessSocketLastChange.command,
                ServerAction.WirelessSocketLastChange,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketLastChange) {
                            val success = state == DownloadState.Success
                            if (!success) {
                                onWirelessSocketService!!.loadFinished(false, "Loading failed!")
                                return
                            }

                            val savedLastChange = dbHandler?.getLastChangeDateTime()
                            val lastChange = lastChangeConverter.parse(message)

                            if (lastChange != null) {
                                if (savedLastChange != null
                                        && (savedLastChange == lastChange || savedLastChange.after(lastChange))) {
                                    onWirelessSocketService!!.loadFinished(true, "Nothing new on server!")
                                    return
                                }

                                dbHandler?.setLastChangeDateTime(lastChange)
                            } else {
                                dbHandler?.setLastChangeDateTime(Calendar.getInstance())
                            }

                            downloadAdapter?.send(
                                    ServerAction.WirelessSocketGet.command,
                                    ServerAction.WirelessSocketGet,
                                    object : OnDownloadAdapter {
                                        override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                                            if (serverAction == ServerAction.WirelessSocketGet) {
                                                val successGet = state == DownloadState.Success
                                                if (!successGet) {
                                                    onWirelessSocketService!!.loadFinished(false, "Loading failed!")
                                                    return
                                                }

                                                val loadedList = converter.parse(message)
                                                var deleteList: List<WirelessSocket> = List(0) { WirelessSocket() }

                                                // Check if wireless socket is already saved, then update, otherwise add
                                                for (loadedEntry in loadedList) {
                                                    if (savedList.any { wirelessSocket -> wirelessSocket.uuid == loadedEntry.uuid }) {
                                                        dbHandler?.update(loadedEntry)
                                                        // Filter updated wireless socket from list
                                                        deleteList = savedList.filter { it.uuid != loadedEntry.uuid }
                                                        continue
                                                    }

                                                    dbHandler?.add(loadedEntry)
                                                }

                                                // Check if any wireless socket was not yet updated, then remove it from the database, because it does not longer exist on server
                                                for (deleteEntry in deleteList) {
                                                    dbHandler?.delete(deleteEntry)
                                                }

                                                onWirelessSocketService!!.loadFinished(true, "")
                                                showNotification()
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
        )
    }

    override fun add(entry: WirelessSocket, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandAdd,
                ServerAction.WirelessSocketAdd,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketAdd) {
                            val success = state == DownloadState.Success

                            if (success) {
                                if (reload) {
                                    load()
                                }
                            } else {
                                entry.isOnServer = false
                                entry.serverDatabaseAction = ServerDatabaseAction.Add
                                dbHandler?.add(entry)
                            }

                            onWirelessSocketService!!.addFinished(success, message)
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun update(entry: WirelessSocket, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandUpdate,
                ServerAction.WirelessSocketUpdate,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketUpdate) {
                            val success = state == DownloadState.Success

                            if (success) {
                                if (reload) {
                                    load()
                                }
                            } else {
                                entry.isOnServer = false
                                entry.serverDatabaseAction = ServerDatabaseAction.Update
                                dbHandler?.update(entry)
                            }

                            onWirelessSocketService!!.updateFinished(success, message)
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun delete(entry: WirelessSocket, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandDelete,
                ServerAction.WirelessSocketDelete,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketDelete) {
                            val success = state == DownloadState.Success

                            if (success) {
                                if (reload) {
                                    load()
                                }
                            } else {
                                entry.isOnServer = false
                                entry.serverDatabaseAction = ServerDatabaseAction.Delete
                                dbHandler?.update(entry)
                            }

                            onWirelessSocketService!!.deleteFinished(success, message)
                            showNotification()
                        }
                    }
                }
        )
    }

    private fun showNotification() {
        if (!this.serviceSettings.notificationEnabled
                || this.receiverActivity == null) {
            return
        }

        notificationController.wirelessSocketNotification(this.notificationId, this.get(), this.receiverActivity!!)
    }

    private fun closeNotification() {
        this.notificationController.close(this.notificationId)
    }
}