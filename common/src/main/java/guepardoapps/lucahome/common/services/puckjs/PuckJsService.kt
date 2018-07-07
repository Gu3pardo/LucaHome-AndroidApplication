package guepardoapps.lucahome.common.services.puckjs

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.converter.common.JsonDataToLastChangeConverter
import guepardoapps.lucahome.common.converter.puckjs.JsonDataToPuckJsConverter
import guepardoapps.lucahome.common.databases.puckjs.DbPuckJs
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.puckjs.PuckJs
import guepardoapps.lucahome.common.services.validation.ValidationService
import guepardoapps.lucahome.common.utils.Logger
import guepardoapps.lucahome.common.worker.puckjs.PuckJsWorker
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PuckJsService private constructor() : IPuckJsService {
    private val tag = PuckJsService::class.java.simpleName

    private var converter: JsonDataToPuckJsConverter = JsonDataToPuckJsConverter()
    private var lastChangeConverter: JsonDataToLastChangeConverter = JsonDataToLastChangeConverter()
    private var validationService: ValidationService = ValidationService()

    private var dbHandler: DbPuckJs? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var reloadWork: PeriodicWorkRequest
    private lateinit var reloadWorkId: UUID

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: PuckJsService = PuckJsService()
    }

    companion object {
        val instance: PuckJsService by lazy { Holder.instance }
    }

    override var initialized: Boolean = false
        get() = this.context != null && this.dbHandler != null
    override var context: Context? = null
    override var onPuckJsService: OnPuckJsService? = null

    override var serviceSettings: ServiceSettings
        get() = this.dbHandler!!.getServiceSettings()!!
        set(value) {
            this.dbHandler!!.setServiceSettings(value)

            if (value.reloadEnabled) {
                WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
                this.reloadWork = PeriodicWorkRequestBuilder<PuckJsWorker>(value.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
                this.reloadWorkId = this.reloadWork.id
                WorkManager.getInstance()?.enqueue(this.reloadWork)
            } else {
                WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
            }
        }

    @Deprecated("Do not use receiverActivity in RoomService")
    override var receiverActivity: Class<*>? = null

    override fun initialize(context: Context) {
        if (this.initialized) {
            return
        }

        this.context = context
        this.downloadAdapter = DownloadAdapter(this.context!!)

        if (this.dbHandler == null) {
            this.dbHandler = DbPuckJs(this.context!!, null)
        }

        if (this.serviceSettings.reloadEnabled) {
            this.reloadWork = PeriodicWorkRequestBuilder<PuckJsWorker>(this.serviceSettings.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
            this.reloadWorkId = this.reloadWork.id
            WorkManager.getInstance()?.enqueue(this.reloadWork)
        }
    }

    override fun dispose() {
        WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
        this.dbHandler?.close()
    }

    override fun get(): MutableList<PuckJs> {
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

    override fun get(uuid: UUID): PuckJs? {
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

    override fun search(searchValue: String): MutableList<PuckJs> {
        val list = this.get()
        val searchResultList = ArrayList<PuckJs>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    override fun load() {
        val actionLastChange = ServerAction.PuckJsLastChange
        val validationResult = this.validationService.mayPerform(actionLastChange)
        if (!validationResult.first) {
            onPuckJsService!!.loadFinished(false, validationResult.second)
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

        this.downloadAdapter?.send(
                actionLastChange.command,
                actionLastChange,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == actionLastChange) {
                            val success = state == DownloadState.Success
                            if (!success) {
                                onPuckJsService!!.loadFinished(false, "Loading failed!")
                                return
                            }

                            val savedLastChange = dbHandler?.getLastChangeDateTime()
                            val lastChange = lastChangeConverter.parse(message)

                            if (lastChange != null) {
                                if (savedLastChange != null
                                        && (savedLastChange == lastChange || savedLastChange.after(lastChange))) {
                                    onPuckJsService!!.loadFinished(true, "Nothing new on server!")
                                    return
                                }

                                dbHandler?.setLastChangeDateTime(lastChange)
                            } else {
                                dbHandler?.setLastChangeDateTime(Calendar.getInstance())
                            }
                            val action = ServerAction.PuckJsGet
                            val actionValidationResult = validationService.mayPerform(action)
                            if (!actionValidationResult.first) {
                                onPuckJsService!!.loadFinished(false, validationResult.second)
                                return
                            }

                            downloadAdapter?.send(
                                    action.command,
                                    action,
                                    object : OnDownloadAdapter {
                                        override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                                            if (serverAction == action) {
                                                val successGet = state == DownloadState.Success
                                                if (!successGet) {
                                                    onPuckJsService!!.loadFinished(false, "Loading failed!")
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

                                                onPuckJsService!!.loadFinished(true, "")
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
        )
    }

    override fun add(entry: PuckJs, reload: Boolean) {
        val action = ServerAction.PuckJsAdd
        val validationResult = this.validationService.mayPerform(action)
        if (!validationResult.first) {
            onPuckJsService!!.addFinished(false, validationResult.second)
            return
        }

        this.downloadAdapter?.send(
                entry.commandAdd,
                action,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == action) {
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

                            onPuckJsService!!.addFinished(success, message)
                        }
                    }
                }
        )
    }

    override fun update(entry: PuckJs, reload: Boolean) {
        val action = ServerAction.PuckJsUpdate
        val validationResult = this.validationService.mayPerform(action)
        if (!validationResult.first) {
            onPuckJsService!!.updateFinished(false, validationResult.second)
            return
        }

        this.downloadAdapter?.send(
                entry.commandUpdate,
                action,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == action) {
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

                            onPuckJsService!!.updateFinished(success, message)
                        }
                    }
                }
        )
    }

    override fun delete(entry: PuckJs, reload: Boolean) {
        val action = ServerAction.PuckJsDelete
        val validationResult = this.validationService.mayPerform(action)
        if (!validationResult.first) {
            onPuckJsService!!.deleteFinished(false, validationResult.second)
            return
        }

        this.downloadAdapter?.send(
                entry.commandDelete,
                action,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == action) {
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

                            onPuckJsService!!.deleteFinished(success, message)
                        }
                    }
                }
        )
    }
}