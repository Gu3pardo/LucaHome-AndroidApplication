package guepardoapps.lucahome.common.controller;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class TTSController implements ITTSController {
    private static final String Tag = TTSController.class.getSimpleName();

    private boolean _enabled;

    private boolean _ttsInitialized;
    private TextToSpeech _ttsSpeaker;

    public TTSController() {
    }

    @Override
    public void Initialize(@NonNull Context context, boolean enabled) {
        if (_ttsInitialized) {
            Logger.getInstance().Warning(Tag, "Already initialized!");
            return;
        }

        _enabled = enabled;

        _ttsSpeaker = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = _ttsSpeaker.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Logger.getInstance().Error(Tag, "This Language is not supported!");
                } else {
                    _ttsInitialized = true;
                }
            } else {
                Logger.getInstance().Error(Tag, "Initialization failed!");
            }
        });
    }

    @Override
    public void SetEnabled(boolean enabled) {
        _enabled = enabled;
    }

    @Override
    public void Speak(@NonNull String text) {
        if (!_enabled) {
            Logger.getInstance().Warning(Tag, "TTS is disabled!");
            return;
        }

        if (_ttsInitialized) {
            _ttsSpeaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
        } else {
            Logger.getInstance().Warning(Tag, "TTSSpeaker not initialized!");
        }
    }

    @Override
    public void Dispose() {
        if (_ttsSpeaker != null) {
            _ttsSpeaker.stop();
            _ttsSpeaker.shutdown();
            _ttsSpeaker = null;
        }
        _ttsInitialized = false;
    }
}
