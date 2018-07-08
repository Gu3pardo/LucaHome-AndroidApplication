package guepardoapps.lucahome.voicerecognition.services

interface OnVoiceRecognitionService {
    fun onPermissionGranted(result: Boolean)
    fun onInitializationFinished(result: Boolean)
    fun onError(exception: Exception)
}