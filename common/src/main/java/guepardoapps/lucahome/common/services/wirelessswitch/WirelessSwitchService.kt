package guepardoapps.lucahome.common.services.wirelessswitch

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.constants.Labels
import guepardoapps.lucahome.common.controller.NotificationController
import guepardoapps.lucahome.common.converter.wirelessswitch.JsonDataToWirelessSwitchConverter
import guepardoapps.lucahome.common.databases.wirelessswitch.DbWirelessSwitch
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.wirelessswitch.WirelessSwitch
import guepardoapps.lucahome.common.receiver.PeriodicActionReceiver
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.utils.Logger
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.collections.ArrayList

class WirelessSwitchService private constructor() : IWirelessSwitchService {
    private val tag = WirelessSwitchService::class.java.simpleName

    private var converter: JsonDataToWirelessSwitchConverter = JsonDataToWirelessSwitchConverter()

    private var dbHandler: DbWirelessSwitch? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var notificationController: NotificationController

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: WirelessSwitchService = WirelessSwitchService()
    }

    companion object {
        val instance: WirelessSwitchService by lazy { Holder.instance }
        const val requestCode: Int = 239219914
        const val notificationId = 438502135
    }

    override var initialized: Boolean = false
        get() = context != null && dbHandler != null
    override var context: Context? = null

    override val responsePublishSubject: PublishSubject<RxResponse> = PublishSubject.create<RxResponse>()!!

    override var serviceSettings: ServiceSettings
        get() = dbHandler!!.getServiceSettings()!!
        set(value) {
            dbHandler!!.setServiceSettings(value)
            cancelReload()
            if (value.reloadEnabled) {
                scheduleReload()
            }
            closeNotification()
            if (value.notificationEnabled && receiverActivity != null) {
                showNotification()
            }
        }

    override var receiverActivity: Class<*>? = null
        set(value) {
            if (value != null && dbHandler?.getServiceSettings()!!.notificationEnabled) {
                showNotification()
            } else {
                closeNotification()
            }
        }

    override fun initialize(context: Context) {
        if (initialized) {
            return
        }

        this.context = context
        downloadAdapter = DownloadAdapter(this.context!!)
        notificationController = NotificationController(this.context!!)

        if (dbHandler == null) {
            dbHandler = DbWirelessSwitch(this.context!!)
        }

        if (serviceSettings.reloadEnabled) {
            scheduleReload()
        }
    }

    override fun dispose() {
        cancelReload()
        dbHandler?.close()
        context = null
        dbHandler = null
    }

    override fun get(): MutableList<WirelessSwitch> {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return ArrayList()
        }

        return try {
            dbHandler!!.getList()
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            ArrayList()
        }
    }

    override fun get(uuid: UUID): WirelessSwitch? {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return null
        }

        return try {
            dbHandler!!.get(uuid)
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            null
        }
    }

    override fun search(searchValue: String): MutableList<WirelessSwitch> {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return ArrayList()
        }

        val list = get()
        val searchResultList = ArrayList<WirelessSwitch>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    override fun toggle(entry: WirelessSwitch) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSwitchToggle))
            return
        }

        downloadAdapter?.send(
                entry.commandToggleState,
                ServerAction.WirelessSwitchToggle,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSwitchToggle) {
                            responsePublishSubject.onNext(RxResponse(state == DownloadState.Success, message, ServerAction.WirelessSwitchToggle))
                            entry.changeCount++
                            dbHandler?.update(entry)
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun load() {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSwitchGet))
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

        val lastChange = ChangeService.instance.get(WirelessSwitch::class.java.simpleName)
        if (lastChange != null) {
            val savedLastChange = dbHandler?.getLastChangeDateTime()
            dbHandler?.setLastChangeDateTime(lastChange.time)

            if (savedLastChange != null
                    && (savedLastChange == lastChange.time || savedLastChange.after(lastChange))) {
                responsePublishSubject.onNext(RxResponse(true, Labels.Services.nothingNewOnServer, ServerAction.WirelessSwitchGet))
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
                                responsePublishSubject.onNext(RxResponse(false, message, ServerAction.WirelessSwitchGet))
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

                            responsePublishSubject.onNext(RxResponse(true, message, ServerAction.WirelessSwitchGet))
                            showNotification()
                        }
                    }
                }
        )

        cancelReload()
        if (serviceSettings.reloadEnabled) {
            scheduleReload()
        }
    }

    override fun add(entry: WirelessSwitch, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSwitchAdd))
            return
        }

        downloadAdapter?.send(
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.WirelessSwitchAdd))
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun update(entry: WirelessSwitch, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSwitchUpdate))
            return
        }

        downloadAdapter?.send(
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.WirelessSwitchUpdate))
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun delete(entry: WirelessSwitch, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSwitchDelete))
            return
        }

        downloadAdapter?.send(
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.WirelessSwitchDelete))
                            showNotification()
                        }
                    }
                }
        )
    }

    private fun scheduleReload() {
        if (serviceSettings.reloadEnabled) {
            val intent = Intent(context?.applicationContext, PeriodicActionReceiver::class.java)
            intent.putExtra(PeriodicActionReceiver.intentKey, ServerAction.WirelessSwitchGet)
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarm = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), serviceSettings.reloadTimeoutMs.toLong(), pendingIntent)
        }
    }

    private fun cancelReload() {
        val intent = Intent(context?.applicationContext, PeriodicActionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarm = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent)
    }

    private fun showNotification() {
        if (!serviceSettings.notificationEnabled || receiverActivity == null) {
            return
        }

        notificationController.wirelessSwitchNotification(notificationId, get(), receiverActivity!!)
    }

    private fun closeNotification() {
        notificationController.close(notificationId)
    }
}