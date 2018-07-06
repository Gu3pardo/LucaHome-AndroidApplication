package guepardoapps.lucahome.common.controller

import android.content.Context
import android.speech.tts.TextToSpeech
import guepardoapps.lucahome.common.utils.Logger
import java.util.*

class TtsController : ITtsController {
    private val tag: String = TtsController::class.java.simpleName

    override var enabled: Boolean = false
    override var context: Context? = null
    private var ttsSpeaker: TextToSpeech? = null

    init {
        this.ttsSpeaker = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = this.ttsSpeaker?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Logger.instance.error(tag, "This Language is not supported!")
                }
            } else {
                Logger.instance.error(tag, "Initialization failed!")
            }
        }
    }

    override fun speak(text: String) {
        if (!enabled) {
            Logger.instance.info(tag, "TTS is disabled!")
            return
        }

        ttsSpeaker?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun dispose() {
        if (this.ttsSpeaker != null) {
            this.ttsSpeaker?.stop()
            this.ttsSpeaker?.shutdown()
            this.ttsSpeaker = null
        }
    }
}