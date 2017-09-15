package guepardoapps.lucahome.basic.controller;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.basic.utils.Logger;

public class TTSController {
    private static final String TAG = TTSController.class.getSimpleName();
    private Logger _logger;

    public static final String TTS_SPEAK_TEXT_BUNDLE = "TTS_SPEAK_TEXT_BUNDLE";
    public static final String TTS_SPEAK_TEXT_BROADCAST = "guepardoapps.lucahome.basic.controller.ttscontroller.speak.text";

    private Context _context;
    private boolean _enabled;

    private ReceiverController _receiverController;

    private boolean _ttsInitialized;
    private TextToSpeech _ttsSpeaker;

    private BroadcastReceiver _speakReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_speakReceiver onReceive");
            String text = intent.getStringExtra(TTS_SPEAK_TEXT_BUNDLE);
            if (text != null) {
                speak(text);
            }
        }
    };

    public TTSController(
            @NonNull Context context,
            boolean enabled) {
        _logger = new Logger(TAG);
        _context = context;
        _enabled = enabled;
        _receiverController = new ReceiverController(_context);
    }

    public void Init() {
        if (_ttsInitialized) {
            _logger.Warning(TAG + " is already initialized!");
            return;
        }

        _ttsSpeaker = new TextToSpeech(_context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = _ttsSpeaker.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        _logger.Error("This Language is not supported!");
                    } else {
                        _receiverController.RegisterReceiver(_speakReceiver, new String[]{TTS_SPEAK_TEXT_BROADCAST});
                        _ttsInitialized = true;
                    }
                } else {
                    _logger.Error("Initialization failed!");
                }
            }
        });
    }

    public void SetEnabled(boolean enabled) {
        _logger.Debug(String.format("SetEnabled from %s to %s!", _enabled, enabled));
        _enabled = enabled;
    }

    public void Dispose() {
        _logger.Debug("Dispose");

        if (_ttsSpeaker != null) {
            _ttsSpeaker.stop();
            _ttsSpeaker.shutdown();
            _ttsSpeaker = null;
        }

        _receiverController.UnregisterReceiver(_speakReceiver);

        _ttsInitialized = false;
    }

    private void speak(@NonNull String text) {
        _logger.Debug("Speak: " + text);
        if (!_enabled) {
            _logger.Warning("TTS is disabled!");
            return;
        }

        if (_ttsInitialized) {
            _ttsSpeaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
        } else {
            _logger.Warning("TTSSpeaker not initialized!");
        }
    }
}
