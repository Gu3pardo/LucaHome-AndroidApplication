package guepardoapps.lucahome.common.services.wirelessswitch

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.controller.NotificationController
import guepardoapps.lucahome.common.converter.wirelessswitch.JsonDataToWirelessSwitchConverter
import guepardoapps.lucahome.common.databases.wirelessswitch.DbWirelessSwitch
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.utils.Logger
import guepardoapps.lucahome.common.worker.wirelessswitch.WirelessSwitchWorker
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class WirelessSwitchService private constructor() : IWirelessSwitchService {
    private val tag = WirelessSwitchService::class.java.simpleName

    private val notificationId = 438502135

    private var converter: JsonDataToWirelessSwitchConverter = JsonDataToWirelessSwitchConverter()

    private var dbHandler: DbWirelessSwitch? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var notificationController: NotificationController

    private lateinit var reloadWork: PeriodicWorkRequest
    private lateinit var reloadWorkId: UUID

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: WirelessSwitchService = WirelessSwitchService()
    }

    companion object {
        val instance: WirelessSwitchService by lazy { Holder.instance }
    }

    override var initialized: Boolean = false
        get() = this.context != null && this.dbHandler != null
    override var context: Context? = null
    override var onWirelessSwitchService: OnWirelessSwitchService? = null

    override var serviceSettings: ServiceSettings
        get() = this.dbHandler!!.getServiceSettings()!!
        set(value) {
            this.dbHandler!!.setServiceSettings(value)

            if (value.reloadEnabled) {
                WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
                this.reloadWork = PeriodicWorkRequestBuilder<WirelessSwitchWorker>(value.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
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
            this.dbHandler = DbWirelessSwitch(this.context!!, null)
        }

        if (this.serviceSettings.reloadEnabled) {
            this.reloadWork = PeriodicWorkRequestBuilder<WirelessSwitchWorker>(this.serviceSettings.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
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

    override fun get(): MutableList<WirelessSwitch> {
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

    override fun get(uuid: UUID): WirelessSwitch? {
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

    override fun search(searchValue: String): MutableList<WirelessSwitch> {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return ArrayList()
        }

        val list = this.get()
        val searchResultList = ArrayList<WirelessSwitch>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    override fun toggle(entry: WirelessSwitch) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandToggleState,
                ServerAction.WirelessSwitchToggle,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSwitchToggle) {
                            onWirelessSwitchService!!.toggleFinished(state == DownloadState.Success, message)

                            entry.changeCount++
                            dbHandler?.update(entry)
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
        val updateList = savedList.filter { wirelessSwitch -> !wirelessSwitch.isOnServer }
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

        ChangeService.instance.onChangeService = object {
            override fun loadFinished(success: Boolean, message: String) {
                if (!success) {
                    onWirelessSwitchService!!.loadFinished(false, "Loading for last change failed!")
                    return
                }

                val lastChange = ChangeService.instance.get("WirelessSwitch")
                if (lastChange != null) {
                    val savedLastChange = dbHandler?.getLastChangeDateTime()
                    dbHandler?.setLastChangeDateTime(lastChange.time)

                    if (savedLastChange != null
                            && (savedLastChange == lastChange.time || savedLastChange.after(lastChange))) {
                        onWirelessSwitchService!!.loadFinished(true, "Nothing new on server!")
                        return
                    }
                }

                downloadAdapter?.send(
                        ServerAction.WirelessSwitchGet.command,
                        ServerAction.WirelessSwitchGet,
                        object : OnDownloadAdapter {
                            override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                                if (serverAction == ServerAction.WirelessSwitchGet) {
                                    val successGet = state == DownloadState.Success
                                    if (!successGet) {
                                        onWirelessSwitchService!!.loadFinished(false, "Loading failed!")
                                        return
                                    }

                                    val loadedList = converter.parse(message)
                                    var deleteList: List<WirelessSwitch> = List(0) { WirelessSwitch() }

                                    // Check if wireless switch is already saved, then update, otherwise add
                                    for (loadedEntry in loadedList) {
                                        if (savedList.any { wirelessSwitch -> wirelessSwitch.uuid == loadedEntry.uuid }) {
                                            dbHandler?.update(loadedEntry)
                                            // Filter updated wireless switch from list
                                            deleteList = savedList.filter { it.uuid != loadedEntry.uuid }
                                            continue
                                        }

                                        dbHandler?.add(loadedEntry)
                                    }

                                    // Check if any wireless switch was not yet updated, then remove it from the database, because it does not longer exist on server
                                    for (deleteEntry in deleteList) {
                                        dbHandler?.delete(deleteEntry)
                                    }

                                    onWirelessSwitchService!!.loadFinished(true, "")
                                    showNotification()
                                }
                            }
                        }
                )
            }
        }
        ChangeService.instance.load()
    }

    override fun add(entry: WirelessSwitch, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandAdd,
                ServerAction.WirelessSwitchAdd,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSwitchAdd) {
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

                            onWirelessSwitchService!!.addFinished(success, message)
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun update(entry: WirelessSwitch, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandUpdate,
                ServerAction.WirelessSwitchUpdate,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSwitchUpdate) {
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

                            onWirelessSwitchService!!.updateFinished(success, message)
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun delete(entry: WirelessSwitch, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandDelete,
                ServerAction.WirelessSwitchDelete,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSwitchDelete) {
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

                            onWirelessSwitchService!!.deleteFinished(success, message)
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

        notificationController.wirelessSwitchNotification(this.notificationId, this.get(), this.receiverActivity!!)
    }

    private fun closeNotification() {
        this.notificationController.close(this.notificationId)
    }
}