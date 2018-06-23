package guepardoapps.lucahome.common.services.common

interface OnLucaStateService {
    fun loadFinished(success: Boolean, message: String)
    fun addFinished(success: Boolean, message: String)
    fun updateFinished(success: Boolean, message: String)
    fun deleteFinished(success: Boolean, message: String)
}