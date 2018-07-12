package guepardoapps.lucahome.common.services.temperature

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import guepardoapps.lucahome.common.adapter.DownloadAdapter
import guepardoapps.lucahome.common.adapter.OnDownloadAdapter
import guepardoapps.lucahome.common.controller.NotificationController
import guepardoapps.lucahome.common.converter.temperature.JsonDataToTemperatureConverter
import guepardoapps.lucahome.common.databases.temperature.DbTemperature
import guepardoapps.lucahome.common.enums.common.DownloadState
import guepardoapps.lucahome.common.enums.common.ServerAction
import guepardoapps.lucahome.common.models.common.NotificationContent
import guepardoapps.lucahome.common.models.common.ServiceSettings
import guepardoapps.lucahome.common.models.temperature.Temperature
import guepardoapps.lucahome.common.R
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.utils.Logger
import guepardoapps.lucahome.common.worker.temperature.TemperatureWorker
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class TemperatureService private constructor() : ITemperatureService {
    private val tag = TemperatureService::class.java.simpleName

    private val notificationId = 438502136

    private var converter: JsonDataToTemperatureConverter = JsonDataToTemperatureConverter()

    private var dbHandler: DbTemperature? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var notificationController: NotificationController

    private lateinit var reloadWork: PeriodicWorkRequest
    private lateinit var reloadWorkId: UUID

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: TemperatureService = TemperatureService()
    }

    companion object {
        val instance: TemperatureService by lazy { Holder.instance }
    }

    override var initialized: Boolean = false
        get() = this.context != null && this.dbHandler != null
    override var context: Context? = null
    override var onTemperatureService: OnTemperatureService? = null

    override var serviceSettings: ServiceSettings
        get() = this.dbHandler!!.getServiceSettings()!!
        set(value) {
            this.dbHandler!!.setServiceSettings(value)

            if (value.reloadEnabled) {
                WorkManager.getInstance()?.cancelWorkById(this.reloadWorkId)
                this.reloadWork = PeriodicWorkRequestBuilder<TemperatureWorker>(value.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
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
            this.dbHandler = DbTemperature(this.context!!, null)
        }

        if (this.serviceSettings.reloadEnabled) {
            this.reloadWork = PeriodicWorkRequestBuilder<TemperatureWorker>(this.serviceSettings.reloadTimeoutMs.toLong(), TimeUnit.MILLISECONDS).build()
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

    override fun get(): MutableList<Temperature> {
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

    override fun get(uuid: UUID): Temperature? {
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

    override fun search(searchValue: String): MutableList<Temperature> {
        if (!this.initialized) {
            Logger.instance.error(tag, "Service not initialized")
            return ArrayList()
        }

        val list = this.get()
        val searchResultList = ArrayList<Temperature>()

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

        ChangeService.instance.onChangeService = object {
            override fun loadFinished(success: Boolean, message: String) {
                if (!success) {
                    onTemperatureService!!.loadFinished(false, "Loading for last change failed!")
                    return
                }

                val lastChange = ChangeService.instance.get("Temperature")
                if (lastChange != null) {
                    val savedLastChange = dbHandler?.getLastChangeDateTime()
                    dbHandler?.setLastChangeDateTime(lastChange.time)

                    if (savedLastChange != null
                            && (savedLastChange == lastChange.time || savedLastChange.after(lastChange))) {
                        onTemperatureService!!.loadFinished(true, "Nothing new on server!")
                        return
                    }
                }

                downloadAdapter?.send(
                        ServerAction.TemperatureGet.command,
                        ServerAction.TemperatureGet,
                        object : OnDownloadAdapter {
                            override fun onFinished(serverAction: ServerAction, state: DownloadState, message: String) {
                                if (serverAction == ServerAction.TemperatureGet) {
                                    val successGet = state == DownloadState.Success
                                    if (!successGet) {
                                        onTemperatureService!!.loadFinished(false, "Loading failed!")
                                        return
                                    }

                                    val savedList = dbHandler?.getList()!!
                                    val loadedList = converter.parse(message)
                                    var deleteList: List<Temperature> = List(0) { Temperature() }

                                    // Check if temperature is already saved, then update, otherwise add
                                    for (loadedEntry in loadedList) {
                                        if (savedList.any { temperature -> temperature.uuid == loadedEntry.uuid }) {
                                            dbHandler?.update(loadedEntry)
                                            // Filter updated wireless switch from list
                                            deleteList = savedList.filter { it.uuid != loadedEntry.uuid }
                                            continue
                                        }

                                        dbHandler?.add(loadedEntry)
                                    }

                                    // Check if any temperature was not yet updated, then remove it from the database, because it does not longer exist on server
                                    for (deleteEntry in deleteList) {
                                        dbHandler?.delete(deleteEntry)
                                    }

                                    onTemperatureService!!.loadFinished(true, "")
                                    showNotification()
                                }
                            }
                        }
                )
            }
        }
        ChangeService.instance.load()
    }

    @Throws(NotImplementedError::class)
    override fun add(entry: Temperature, reload: Boolean) {
        throw NotImplementedError("Add for temperature not available")
    }

    @Throws(NotImplementedError::class)
    override fun update(entry: Temperature, reload: Boolean) {
        throw NotImplementedError("Update for temperature not available")
    }

    @Throws(NotImplementedError::class)
    override fun delete(entry: Temperature, reload: Boolean) {
        throw NotImplementedError("Delete for temperature not available")
    }

    private fun showNotification() {
        if (!this.serviceSettings.notificationEnabled
                || this.receiverActivity == null) {
            return
        }

        val notificationContent = NotificationContent(
                notificationId,
                "Temperature",
                this.get().first().temperatureString,
                R.drawable.temperature,
                R.drawable.temperature,
                this.receiverActivity!!,
                true)

        notificationController.create(notificationContent)
    }

    private fun closeNotification() {
        this.notificationController.close(this.notificationId)
    }
}