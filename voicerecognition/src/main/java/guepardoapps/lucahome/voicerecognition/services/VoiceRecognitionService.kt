package guepardoapps.lucahome.voicerecognition.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import edu.cmu.pocketsphinx.*
import guepardoapps.lib.openweather.enums.WeatherCondition
import guepardoapps.lib.openweather.extensions.*
import guepardoapps.lib.openweather.models.RxOptional
import guepardoapps.lib.openweather.models.WeatherCurrent
import guepardoapps.lib.openweather.models.WeatherForecast
import guepardoapps.lib.openweather.services.openweather.OpenWeatherService
import guepardoapps.lucahome.common.controller.TtsController
import guepardoapps.lucahome.common.extensions.common.doubleFormat
import guepardoapps.lucahome.common.models.puckjs.PuckJs
import guepardoapps.lucahome.common.models.temperature.Temperature
import guepardoapps.lucahome.common.services.position.PositionService
import guepardoapps.lucahome.common.services.puckjs.PuckJsService
import guepardoapps.lucahome.common.services.room.RoomService
import guepardoapps.lucahome.common.services.temperature.TemperatureService
import guepardoapps.lucahome.common.services.wirelesssocket.WirelessSocketService
import guepardoapps.lucahome.common.services.wirelessswitch.WirelessSwitchService
import guepardoapps.lucahome.common.utils.Logger
import guepardoapps.lucahome.voicerecognition.enums.Action
import guepardoapps.lucahome.voicerecognition.enums.InitializeResult
import guepardoapps.lucahome.voicerecognition.enums.MediaState
import guepardoapps.lucahome.voicerecognition.models.RelationAction
import guepardoapps.lucahome.voicerecognition.helper.*
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.ref.WeakReference

class VoiceRecognitionService private constructor() : RecognitionListener, ActivityCompat.OnRequestPermissionsResultCallback, IVoiceRecognitionService {
    private val tag: String = VoiceRecognitionService::class.java.simpleName

    private val permissionRequestRecordAudio = 345243854

    private val kwsSearch = "wakeup"
    private val keyPhrase = "hey luca"

    private var isInitialized: Boolean = false
    private var permissionRecordAudioGranted: Boolean = false

    private var context: Context? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var ttsController: TtsController? = null

    private var onVoiceRecognitionService: OnVoiceRecognitionService? = null

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: VoiceRecognitionService = VoiceRecognitionService()
    }

    companion object {
        val instance: VoiceRecognitionService by lazy { Holder.instance }

        const val broadcastAudioMedia = "gueopardoapps.lucahome.voicerecognition.services.broadcast.audio_media"
        const val bundleAudioMedia = "bundleAudioMedia"
    }

    override fun initialize(context: Context, onVoiceRecognitionService: OnVoiceRecognitionService): InitializeResult {
        if (isInitialized) {
            Logger.instance.debug(tag, "Already initialized!")
            return InitializeResult.AlreadyInitialized
        }

        if (context !is Activity) {
            Logger.instance.debug(tag, "Context is not of type Activity!")
            return InitializeResult.ContextNotOfTypeActivity
        }

        Logger.instance.debug(tag, "Initialize")

        this.context = context
        this.onVoiceRecognitionService = onVoiceRecognitionService
        this.ttsController = TtsController()

        try {
            permissionRecordAudioGranted = checkRecordAudioPermission()
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            this.onVoiceRecognitionService?.onPermissionGranted(false)
        }

        if (permissionRecordAudioGranted) {
            SetupTask(this).execute()
        }

        isInitialized = true
        return InitializeResult.InitializeSuccess
    }

    override fun dispose() {
        Logger.instance.debug(tag, "Dispose")
        if (speechRecognizer != null) {
            speechRecognizer!!.cancel()
            speechRecognizer!!.shutdown()
        }
        isInitialized = false
    }

    override fun requestRecordAudioPermission(): Boolean {
        Logger.instance.verbose(tag, "RequestRecordAudioPermission")
        if (permissionRecordAudioGranted) {
            Logger.instance.verbose(tag, "Already granted!")
            return true
        }

        return try {
            permissionRecordAudioGranted = checkRecordAudioPermission()
            permissionRecordAudioGranted
        } catch (exception: Exception) {
            Logger.instance.error(tag, exception)
            false
        }
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onEndOfSpeech() {
        if (speechRecognizer!!.searchName != kwsSearch) {
            switchSearch(kwsSearch)
        }
    }

    override fun onPartialResult(hypothesis: Hypothesis?) {
        if (hypothesis != null) {
            val result = hypothesis.hypstr
            if (result != null) {
                val relationActionClass = getRelationActionClass(result)
                try {
                    handleSpeechResult(relationActionClass)
                } catch (exception: Exception) {
                    Logger.instance.error(tag, exception)
                }
            }
        }
    }

    override fun onResult(hypothesis: Hypothesis?) {
        if (hypothesis != null) {
            val result = hypothesis.hypstr
            if (result != null) {
                val relationActionClass = getRelationActionClass(result)
                try {
                    handleSpeechResult(relationActionClass)
                    return
                } catch (exception: Exception) {
                    Logger.instance.error(tag, exception)
                }

            }
        }
    }

    override fun onError(exception: Exception?) {
        Logger.instance.error(tag, exception)
    }

    override fun onTimeout() {
        switchSearch(kwsSearch)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permissionRequestRecordAudio) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionRecordAudioGranted = true
                // Recognizer initialization is a time-consuming and it involves IO, so we execute it in async task
                SetupTask(this).execute()
            } else {
                permissionRecordAudioGranted = false
            }
        }
        onVoiceRecognitionService?.onPermissionGranted(permissionRecordAudioGranted)
    }

    private fun checkRecordAudioPermission(): Boolean {
        Logger.instance.debug(tag, "checkRecordAudioPermission")
        val permissionCheck = ContextCompat.checkSelfPermission(context!!.applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), permissionRequestRecordAudio)
        }

        if (isInitialized) {
            onVoiceRecognitionService?.onPermissionGranted(permissionCheck == PackageManager.PERMISSION_GRANTED)
        }

        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun switchSearch(searchValue: String?) {
        if (searchValue == null) {
            return
        }
        speechRecognizer?.stop()
        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        when (searchValue) {
            kwsSearch -> speechRecognizer?.startListening(searchValue)
            else -> speechRecognizer?.startListening(searchValue, 10000)
        }
    }

    @Throws(IOException::class)
    private fun setupRecognizer(assetsDir: File) {
        // The recognizer can be configured to perform multiple searches of different kind and switch between them
        speechRecognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(File(assetsDir, "en-us-ptm"))
                .setDictionary(File(assetsDir, "cmudict-en-us.dict"))
                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .recognizer
        speechRecognizer?.addListener(this)

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one. */

        // Create keyword-activation search.
        speechRecognizer?.addKeyphraseSearch(kwsSearch, keyPhrase)
    }

    @Throws(Exception::class)
    private fun handleSpeechResult(relationAction: RelationAction) {
        val action = relationAction.action
        val parameter = relationAction.parameter

        when (action) {
            Action.WirelessSocketOn -> {
                val wirelessSocketOn = getWirelessSocketByName(parameter, WirelessSocketService.instance.get())
                WirelessSocketService.instance.setState(wirelessSocketOn!!, true)
            }
            Action.WirelessSocketOff -> {
                val wirelessSocketOff = getWirelessSocketByName(parameter, WirelessSocketService.instance.get())
                WirelessSocketService.instance.setState(wirelessSocketOff!!, false)
            }
            Action.WirelessSocketToggle -> {
                val wirelessSocketToggle = getWirelessSocketByName(parameter, WirelessSocketService.instance.get())
                WirelessSocketService.instance.setState(wirelessSocketToggle!!, !wirelessSocketToggle.state)
            }

            Action.WirelessSwitchToggle -> {
                val wirelessSwitch = getWirelessSwitchByName(parameter, WirelessSwitchService.instance.get())
                WirelessSwitchService.instance.toggle(wirelessSwitch!!)
            }

            Action.PlayYoutube -> {
                val bundle = Bundle()
                bundle.putInt(bundleAudioMedia, MediaState.PlayYoutube.ordinal)
                val intent = Intent(broadcastAudioMedia)
                intent.putExtras(bundle)
                context?.sendBroadcast(intent)
            }
            Action.PlayRadio -> {
                val bundle = Bundle()
                bundle.putInt(bundleAudioMedia, MediaState.PlayRadio.ordinal)
                val intent = Intent(broadcastAudioMedia)
                intent.putExtras(bundle)
                context?.sendBroadcast(intent)
            }
            Action.Pause -> {
                val bundle = Bundle()
                bundle.putInt(bundleAudioMedia, MediaState.Pause.ordinal)
                val intent = Intent(broadcastAudioMedia)
                intent.putExtras(bundle)
                context?.sendBroadcast(intent)
            }
            Action.Stop -> {
                val bundle = Bundle()
                bundle.putInt(bundleAudioMedia, MediaState.Stop.ordinal)
                val intent = Intent(broadcastAudioMedia)
                intent.putExtras(bundle)
                context?.sendBroadcast(intent)
            }

            Action.WeatherCurrent -> {
                OpenWeatherService.instance.weatherCurrentPublishSubject
                        .subscribeOn(Schedulers.io())
                        .first(RxOptional(WeatherCurrent()))
                        .subscribe(
                                { response ->
                                    Logger.instance.verbose(tag, "Received weather current in subscribe!")

                                    if (response != null) {
                                        val value = response.value as WeatherCurrent
                                        if (value.weatherCondition != WeatherCondition.Null) {
                                            this.ttsController?.speak(createTtsSpeak(value))
                                        } else {
                                            this.ttsController?.speak("Sorry, no current weather data")
                                        }
                                    } else {
                                        this.ttsController?.speak("Sorry, no current weather data")
                                    }
                                },
                                { responseError ->
                                    Logger.instance.error(tag, responseError)
                                    this.ttsController?.speak("Sorry, no current weather data")
                                })
            }

            Action.WeatherForecast -> {
                OpenWeatherService.instance.weatherForecastPublishSubject
                        .subscribeOn(Schedulers.io())
                        .first(RxOptional(WeatherForecast()))
                        .subscribe(
                                { response ->
                                    Logger.instance.verbose(tag, "Received weather forecast in subscribe!")

                                    if (response != null) {
                                        val value = response.value as WeatherForecast
                                        if (value.list.isNotEmpty()) {
                                            this.ttsController?.speak(createTtsSpeak(value))
                                        } else {
                                            this.ttsController?.speak("Sorry, no forecast weather data")
                                        }
                                    } else {
                                        this.ttsController?.speak("Sorry, no forecast weather data")
                                    }
                                },
                                { responseError ->
                                    Logger.instance.error(tag, responseError)
                                    this.ttsController?.speak("Sorry, no forecast weather data")
                                })
            }

            Action.GetLight -> {
                val puckJs = getPuckJsByArea(parameter, PuckJsService.instance.get())
                this.ttsController?.speak(createTtsSpeak(puckJs))
            }

            Action.GetTemperature -> {
                val temperature = getTemperatureByArea(parameter, TemperatureService.instance.get())
                this.ttsController?.speak(createTtsSpeak(temperature))
            }

            Action.Unknown -> {
                val exception = NullPointerException("Unknown action for result $parameter")
                Logger.instance.error(tag, exception)
                onVoiceRecognitionService?.onError(exception)
            }
        }
    }

    private fun createTtsSpeak(weatherCurrent: WeatherCurrent?): String {
        if (weatherCurrent == null) {
            return "No current weather available"
        }
        return "Current weather in ${weatherCurrent.city} is ${weatherCurrent.weatherCondition.description}. It has ${weatherCurrent.temperature.doubleFormat(2)} degree."
    }

    private fun createTtsSpeak(weatherForecast: WeatherForecast?): String {
        if (weatherForecast == null) {
            return "No forecast weather available"
        }
        return "It will be " + weatherForecast.getMostWeatherCondition().description + "." +
                "Temperature will be from " + weatherForecast.getMinTemperature().doubleFormat(1) + "${0x00B0.toChar()}C to " + weatherForecast.getMaxTemperature().doubleFormat(1) + "${0x00B0.toChar()}C." +
                "Air pressure will be from " + weatherForecast.getMinPressure().doubleFormat(1) + "mBar to " + weatherForecast.getMaxPressure().doubleFormat(1) + "mBar." +
                "Humidity will be from " + weatherForecast.getMinHumidity().doubleFormat(1) + "% to " + weatherForecast.getMaxHumidity().doubleFormat(1) + "%"
    }

    private fun createTtsSpeak(puckJs: PuckJs?): String {
        if (puckJs == null) {
            return "No puck js found"
        }
        val position = PositionService.instance.currentPosition
        val room = RoomService.instance.get(puckJs.roomUuid)
        return "There is a relative light value of ${position.lightValue.doubleFormat(2)} in the ${room?.name}"
    }

    private fun createTtsSpeak(temperature: Temperature?): String {
        if (temperature == null) {
            return "No temperature found"
        }
        return "It has ${temperature.value.doubleFormat(2)} degree in the ${temperature.area}"
    }

    private class SetupTask internal constructor(voiceRecognitionService: VoiceRecognitionService) : AsyncTask<Void, Void, Exception>() {
        private val tag: String = SetupTask::class.java.simpleName

        private val _voiceRecognitionServiceWeakReference: WeakReference<VoiceRecognitionService> = WeakReference(voiceRecognitionService)

        override fun doInBackground(vararg voids: Void): Exception? {
            try {
                val assets = Assets(_voiceRecognitionServiceWeakReference.get()!!.context)
                val assetDir = assets.syncAssets()
                _voiceRecognitionServiceWeakReference.get()!!.setupRecognizer(assetDir)
            } catch (exception: IOException) {
                Logger.instance.error(tag, exception.toString())
                return exception
            }

            return null
        }

        override fun onPostExecute(result: Exception?) {
            _voiceRecognitionServiceWeakReference.get()!!.onVoiceRecognitionService?.onInitializationFinished(result == null)
        }
    }
}