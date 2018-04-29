package guepardoapps.lucahome.voicerecognition.services;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.lucahome.voicerecognition.utils.IRelationshipHelper;

@SuppressWarnings({"unused"})
public interface IVoiceRecognitionService {
    enum InitializeResult {AlreadyInitialized, ContextNotOfTypeActivity, InitializeSuccess}

    enum MediaState {PlayYoutube, PlayRadio, Pause, Stop}

    String BroadcastException = "gueopardoapps.lucahome.voicerecognition.services.broadcast.exception";
    String BroadcastPermissionRecordAudioResult = "gueopardoapps.lucahome.voicerecognition.services.broadcast.permission_record_audio_result";
    String BroadcastSetupSpeechRecognizerResult = "gueopardoapps.lucahome.voicerecognition.services.broadcast.setup_broadcast_speech_recognizer_result";
    String BroadcastAudioMedia = "gueopardoapps.lucahome.voicerecognition.services.broadcast.audio_media";
    String BroadcastTtsSpeak = "gueopardoapps.lucahome.voicerecognition.services.broadcast.tts_speak";

    String BundleException = "BundleException";
    String BundlePermissionRecordAudioResult = "BundlePermissionRecordAudioResult";
    String BundleSetupSpeechRecognizerResult = "BundleSetupSpeechRecognizerResult";
    String BundleAudioMedia = "BundleAudioMedia";
    String BundleTtsSpeak = "BundleTtsSpeak";

    VoiceRecognitionService getInstance();

    InitializeResult Initialize(@NonNull Context context, @NonNull IRelationshipHelper relationshipHelper);

    void Dispose();

    boolean RequestRecordAudioPermission();
}
