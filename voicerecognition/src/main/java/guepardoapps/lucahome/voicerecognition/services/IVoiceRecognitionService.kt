package guepardoapps.lucahome.voicerecognition.services

import android.content.Context
import guepardoapps.lucahome.voicerecognition.enums.InitializeResult

internal interface IVoiceRecognitionService {
    fun initialize(context: Context, onVoiceRecognitionService: OnVoiceRecognitionService): InitializeResult
    fun dispose()
    fun requestRecordAudioPermission(): Boolean
}