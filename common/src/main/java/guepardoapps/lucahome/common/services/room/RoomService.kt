package guepardoapps.lucahome.common.services.room

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.converter.room.JsonDataToRoomConverter
import guepardoapps.lucahome.common.databases.room.DbRoom
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.enums.common.ServerDatabaseAction
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.room.Room
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.services.change.OnChangeService
import guepardoapps.lucahome.common.utils.Logger
import guepardoapps.lucahome.common.worker.room.RoomWorker
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class RoomService private constructor() : IRoomService {
    private val tag = RoomService::class.java.simpleName

    private var converter: JsonDataToRoomConverter = JsonDataToRoomConverter()

    private var dbHandler: DbRoom? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var reloadWork: PeriodicWorkRequest
    private lateinit var reloadWorkId: UUID

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: RoomService = RoomService()
    }

    companion object {
        val instance: RoomService by lazy { Holder.instance }
    }

    override var initialized: Boolean = false
        get() = this.context != null && this.dbHandler != null
    override var context: Context? = null
    override var onRoomService: OnRoomService? = null

    override var serviceSettings: ServiceSettings
        get() = this.dbHandler!!.getServiceSettings()!!
        set(value) {
            this.dbHandler!!.setServiceSettings(value)

            if (value.reloadEnabled) {
                WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
                this.reloadWork = PeriodicWorkRequestBuilder<RoomWorker>(value.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
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
            this.dbHandler = DbRoom(this.context!!, null)
        }

        if (this.serviceSettings.reloadEnabled) {
            this.reloadWork = PeriodicWorkRequestBuilder<RoomWorker>(this.serviceSettings.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
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

    override fun get(): MutableList<Room> {
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

    override fun get(uuid: UUID): Room? {
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

    override fun search(searchValue: String): MutableList<Room> {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return ArrayList()
        }

        val list = this.get()
        val searchResultList = ArrayList<Room>()

        for (entry in list) {
            if (entry.toString().contains(searchValue)) {
                searchResultList.add(entry)
            }
        }

        return searchResultList
    }

    override fun load() {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        val savedList = dbHandler?.getList()!!
        val updateList = savedList.filter { room -> !room.isOnServer }
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

        ChangeService.instance.onChangeService = object : OnChangeService {
            override fun loadFinished(success: Boolean, message: String) {
                if (!success) {
                    onRoomService!!.loadFinished(false, "Loading for last change failed!")
                    return
                }

                val lastChange = ChangeService.instance.get("PuckJs")
                if (lastChange != null) {
                    val savedLastChange = dbHandler?.getLastChangeDateTime()
                    dbHandler?.setLastChangeDateTime(lastChange.time)

                    if (savedLastChange != null
                            && (savedLastChange == lastChange.time || savedLastChange.after(lastChange))) {
                        onRoomService!!.loadFinished(true, "Nothing new on server!")
                        return
                    }
                }

                downloadAdapter?.send(
                        ServerAction.RoomGet.command,
                        ServerAction.RoomGet,
                        object : OnDownloadAdapter {
                            override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                                if (serverAction == ServerAction.RoomGet) {
                                    val successGet = state == DownloadState.Success
                                    if (!successGet) {
                                        onRoomService!!.loadFinished(false, "Loading failed!")
                                        return
                                    }

                                    val loadedList = converter.parse(message)
                                    var deleteList: List<Room> = List(0) { Room() }

                                    // Check if room is already saved, then update, otherwise add
                                    for (loadedEntry in loadedList) {
                                        if (savedList.any { room -> room.uuid == loadedEntry.uuid }) {
                                            dbHandler?.update(loadedEntry)
                                            // Filter updated room from list
                                            deleteList = savedList.filter { it.uuid != loadedEntry.uuid }
                                            continue
                                        }

                                        dbHandler?.add(loadedEntry)
                                    }

                                    // Check if any room was not yet updated, then remove it from the database, because it does not longer exist on server
                                    for (deleteEntry in deleteList) {
                                        dbHandler?.delete(deleteEntry)
                                    }

                                    onRoomService!!.loadFinished(true, "")
                                }
                            }
                        }
                )
            }
        }
        ChangeService.instance.load()
    }

    override fun add(entry: Room, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandAdd,
                ServerAction.RoomAdd,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.RoomAdd) {
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

                            onRoomService!!.addFinished(success, message)
                        }
                    }
                }
        )
    }

    override fun update(entry: Room, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandUpdate,
                ServerAction.RoomUpdate,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.RoomUpdate) {
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

                            onRoomService!!.updateFinished(success, message)
                        }
                    }
                }
        )
    }

    override fun delete(entry: Room, reload: Boolean) {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return
        }

        this.downloadAdapter?.send(
                entry.commandDelete,
                ServerAction.RoomDelete,
                object : OnDownloadAdapter {
                    override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                        if (serverAction == ServerAction.RoomDelete) {
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

                            onRoomService!!.deleteFinished(success, message)
                        }
                    }
                }
        )
    }
}