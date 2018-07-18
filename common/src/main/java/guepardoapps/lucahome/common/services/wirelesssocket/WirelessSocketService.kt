package guepardoapps.lucahome.common.services.wirelesssocket

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.constants.Labels
import guepardoapps.lucahome.common.controller.NotificationController
import guepardoapps.lucahome.common.converter.wirelesssocket.JsonDataToWirelessSocketConverter
import guepardoapps.lucahome.common.databases.wirelesssocket.DbWirelessSocket
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.wirelesssocket.WirelessSocket
import guepardoapps.lucahome.common.receiver.PeriodicActionReceiver
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.utils.Logger
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.collections.ArrayList

class WirelessSocketService private constructor() : IWirelessSocketService {
    private val tag = WirelessSocketService::class.java.simpleName

    private var converter: JsonDataToWirelessSocketConverter = JsonDataToWirelessSocketConverter()

    private var dbHandler: DbWirelessSocket? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var notificationController: NotificationController

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: WirelessSocketService = WirelessSocketService()
    }

    companion object {
        val instance: WirelessSocketService by lazy { Holder.instance }
        const val requestCode: Int = 239219913
        const val notificationId = 438502134
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
            dbHandler = DbWirelessSocket(this.context!!)
        }

        if (serviceSettings.reloadEnabled) {
            scheduleReload()
        }
    }

    override fun dispose() {
        initialized = false
        cancelReload()
        dbHandler?.close()
        context = null
        dbHandler = null
    }

    override fun get(): MutableList<WirelessSocket> {
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

    override fun get(uuid: UUID): WirelessSocket? {
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

    override fun search(searchValue: String): MutableList<WirelessSocket> {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return ArrayList()
        }

        val list = get()
        val searchResultList = ArrayList<WirelessSocket>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    override fun setState(entry: WirelessSocket, newState: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSocketSet))
            return
        }

        entry.state = newState
        downloadAdapter?.send(
                entry.commandSetState,
                ServerAction.WirelessSocketSet,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketSet) {
                            responsePublishSubject.onNext(RxResponse(state == DownloadState.Success, message, ServerAction.WirelessSocketSet))
                            entry.changeCount++
                            dbHandler?.update(entry)
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun deactivateAll() {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSocketSet))
            return
        }

        downloadAdapter?.send(
                ServerAction.WirelessSocketDeactivateAll.command,
                ServerAction.WirelessSocketDeactivateAll,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketDeactivateAll) {
                            responsePublishSubject.onNext(RxResponse(state == DownloadState.Success, message, ServerAction.WirelessSocketDeactivateAll))
                            get().forEach { value -> value.changeCount++; dbHandler?.update(value) }
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun load() {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSocketGet))
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

        val lastChange = ChangeService.instance.get(WirelessSocket::class.java.simpleName)
        if (lastChange != null) {
            val savedLastChange = dbHandler?.getLastChangeDateTime()
            dbHandler?.setLastChangeDateTime(lastChange.time)

            if (savedLastChange != null
                    && (savedLastChange == lastChange.time || savedLastChange.after(lastChange))) {
                responsePublishSubject.onNext(RxResponse(true, Labels.Services.nothingNewOnServer, ServerAction.WirelessSocketGet))
                return
            }
        }

        downloadAdapter?.send(
                ServerAction.WirelessSocketGet.command,
                ServerAction.WirelessSocketGet,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.WirelessSocketGet) {
                            val successGet = state == DownloadState.Success
                            if (!successGet) {
                                responsePublishSubject.onNext(RxResponse(false, message, ServerAction.WirelessSocketGet))
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

                            responsePublishSubject.onNext(RxResponse(true, message, ServerAction.WirelessSocketGet))
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

    override fun add(entry: WirelessSocket, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSocketAdd))
            return
        }

        downloadAdapter?.send(
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.WirelessSocketAdd))
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun update(entry: WirelessSocket, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSocketUpdate))
            return
        }

        downloadAdapter?.send(
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.WirelessSocketUpdate))
                            showNotification()
                        }
                    }
                }
        )
    }

    override fun delete(entry: WirelessSocket, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.WirelessSocketDelete))
            return
        }

        downloadAdapter?.send(
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.WirelessSocketDelete))
                            showNotification()
                        }
                    }
                }
        )
    }

    private fun scheduleReload() {
        if (serviceSettings.reloadEnabled) {
            val intent = Intent(context?.applicationContext, PeriodicActionReceiver::class.java)
            intent.putExtra(PeriodicActionReceiver.intentKey, ServerAction.WirelessSocketGet)
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

        notificationController.wirelessSocketNotification(notificationId, get(), receiverActivity!!)
    }

    private fun closeNotification() {
        notificationController.close(notificationId)
    }
}