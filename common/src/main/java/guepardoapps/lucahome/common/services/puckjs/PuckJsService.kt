package guepardoapps.lucahome.common.services.puckjs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.constants.Labels
import guepardoapps.lucahome.common.converter.puckjs.JsonDataToPuckJsConverter
import guepardoapps.lucahome.common.databases.puckjs.DbPuckJs
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.puckjs.PuckJs
import guepardoapps.lucahome.common.receiver.PeriodicActionReceiver
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.utils.Logger
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.collections.ArrayList

class PuckJsService private constructor() : IPuckJsService {
    private val tag = PuckJsService::class.java.simpleName

    private var converter: JsonDataToPuckJsConverter = JsonDataToPuckJsConverter()

    private var dbHandler: DbPuckJs? = null
    private var downloadAdapter: DownloadAdapter? = null

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: PuckJsService = PuckJsService()
    }

    companion object {
        val instance: PuckJsService by lazy { Holder.instance }
        const val requestCode: Int = 239219910
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
        }

    @Deprecated("Do not use receiverActivity in PuckJsService")
    override var receiverActivity: Class<*>? = null

    override fun initialize(context: Context) {
        if (initialized) {
            return
        }

        this.context = context
        downloadAdapter = DownloadAdapter(this.context!!)

        if (dbHandler == null) {
            dbHandler = DbPuckJs(this.context!!)
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

    override fun get(): MutableList<PuckJs> {
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

    override fun get(uuid: UUID): PuckJs? {
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

    override fun search(searchValue: String): MutableList<PuckJs> {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return ArrayList()
        }

        val list = get()
        val searchResultList = ArrayList<PuckJs>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    override fun load() {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.PuckJsGet))
            return
        }

        val savedList = dbHandler?.getList()!!
        val updateList = savedList.filter { puckJs -> !puckJs.isOnServer }
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

        val lastChange = ChangeService.instance.get(PuckJs::class.java.simpleName)
        if (lastChange != null) {
            val savedLastChange = dbHandler?.getLastChangeDateTime()
            dbHandler?.setLastChangeDateTime(lastChange.time)

            if (savedLastChange != null
                    && (savedLastChange == lastChange.time || savedLastChange.after(lastChange))) {
                responsePublishSubject.onNext(RxResponse(true, Labels.Services.nothingNewOnServer, ServerAction.PuckJsGet))
                return
            }
        }

        downloadAdapter?.send(
                ServerAction.PuckJsGet.command,
                ServerAction.PuckJsGet,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.PuckJsGet) {
                            val successGet = state == DownloadState.Success
                            if (!successGet) {
                                responsePublishSubject.onNext(RxResponse(false, message, ServerAction.PuckJsGet))
                                return
                            }

                            val loadedList = converter.parse(message)
                            var deleteList: List<PuckJs> = List(0) { PuckJs() }

                            // Check if puckJs is already saved, then update, otherwise add
                            for (loadedEntry in loadedList) {
                                if (savedList.any { puckJs -> puckJs.uuid == loadedEntry.uuid }) {
                                    dbHandler?.update(loadedEntry)
                                    // Filter updated puckJs from list
                                    deleteList = savedList.filter { it.uuid != loadedEntry.uuid }
                                    continue
                                }

                                dbHandler?.add(loadedEntry)
                            }

                            // Check if any puckJs was not yet updated, then remove it from the database, because it does not longer exist on server
                            for (deleteEntry in deleteList) {
                                dbHandler?.delete(deleteEntry)
                            }

                            responsePublishSubject.onNext(RxResponse(true, message, ServerAction.PuckJsGet))
                        }
                    }
                }
        )

        cancelReload()
        if (serviceSettings.reloadEnabled) {
            scheduleReload()
        }
    }

    override fun add(entry: PuckJs, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.PuckJsAdd))
            return
        }

        downloadAdapter?.send(
                entry.commandAdd,
                ServerAction.PuckJsAdd,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.PuckJsAdd) {
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.PuckJsAdd))
                        }
                    }
                }
        )
    }

    override fun update(entry: PuckJs, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.PuckJsUpdate))
            return
        }

        downloadAdapter?.send(
                entry.commandUpdate,
                ServerAction.PuckJsUpdate,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.PuckJsUpdate) {
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.PuckJsUpdate))
                        }
                    }
                }
        )
    }

    override fun delete(entry: PuckJs, reload: Boolean) {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.PuckJsDelete))
            return
        }

        downloadAdapter?.send(
                entry.commandDelete,
                ServerAction.PuckJsDelete,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.PuckJsDelete) {
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

                            responsePublishSubject.onNext(RxResponse(success, message, ServerAction.PuckJsDelete))
                        }
                    }
                }
        )
    }

    private fun scheduleReload() {
        if (serviceSettings.reloadEnabled) {
            val intent = Intent(context?.applicationContext, PeriodicActionReceiver::class.java)
            intent.putExtra(PeriodicActionReceiver.intentKey, ServerAction.PuckJsGet)
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
}