package guepardoapps.lucahome.common.services.change

interface OnChangeService {
    fun loadFinished(success: Boolean, message: String)
}