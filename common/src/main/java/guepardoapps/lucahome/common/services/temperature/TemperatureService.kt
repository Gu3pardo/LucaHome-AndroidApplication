package guepardoapps.lucahome.common.services.temperature

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import guepardoapps.lucahome.common.constants.Labels
import guepardoapps.lucahome.common.models.common.RxResponse
import guepardoapps.lucahome.common.receiver.PeriodicActionReceiver
import guepardoapps.lucahome.common.services.change.ChangeService
import guepardoapps.lucahome.common.utils.Logger
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.collections.ArrayList

class TemperatureService private constructor() : ITemperatureService {
    private val tag = TemperatureService::class.java.simpleName

    private var converter: JsonDataToTemperatureConverter = JsonDataToTemperatureConverter()

    private var dbHandler: DbTemperature? = null
    private var downloadAdapter: DownloadAdapter? = null

    private lateinit var notificationController: NotificationController

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: TemperatureService = TemperatureService()
    }

    companion object {
        val instance: TemperatureService by lazy { Holder.instance }
        const val requestCode: Int = 239219912
        const val notificationId = 438502136
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
            dbHandler = DbTemperature(this.context!!)
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

    override fun get(): MutableList<Temperature> {
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

    override fun get(uuid: UUID): Temperature? {
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

    override fun search(searchValue: String): MutableList<Temperature> {
        if (!initialized) {
            Logger.instance.error(tag, Labels.Services.notInitialized)
            return ArrayList()
        }

        val list = get()
        val searchResultList = ArrayList<Temperature>()

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
            responsePublishSubject.onNext(RxResponse(false, Labels.Services.notInitialized, ServerAction.TemperatureGet))
            return
        }

        val lastChange = ChangeService.instance.get(Temperature::class.java.simpleName)
        if (lastChange != null) {
            val savedLastChange = dbHandler?.getLastChangeDateTime()
            dbHandler?.setLastChangeDateTime(lastChange.time)

            if (savedLastChange != null
                    && (savedLastChange == lastChange.time || savedLastChange.after(lastChange))) {
                responsePublishSubject.onNext(RxResponse(true, Labels.Services.nothingNewOnServer, ServerAction.TemperatureGet))
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
                                responsePublishSubject.onNext(RxResponse(false, message, ServerAction.TemperatureGet))
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

                            responsePublishSubject.onNext(RxResponse(true, message, ServerAction.TemperatureGet))
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

    private fun scheduleReload() {
        if (serviceSettings.reloadEnabled) {
            val intent = Intent(context?.applicationContext, PeriodicActionReceiver::class.java)
            intent.putExtra(PeriodicActionReceiver.intentKey, ServerAction.TemperatureGet)
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

        val notificationContent = NotificationContent(
                notificationId,
                "Temperature",
                get().first().temperatureString,
                R.drawable.temperature,
                R.drawable.temperature,
                receiverActivity!!,
                true)

        notificationController.create(notificationContent)
    }

    private fun closeNotification() {
        notificationController.close(notificationId)
    }
}