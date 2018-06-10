package guepardoapps.lucahome.bixby.services

import guepardoapps.lucahome.bixby.models.BixbyPair

interface OnBixbyServiceListener {
    fun onLoad(bixbyList: ArrayList<BixbyPair>)

    fun onAddFinished(success: Boolean, errorMessage: String? = null)

    fun onUpdateFinished(success: Boolean, bixbyPair: BixbyPair, errorMessage: String? = null)

    fun onDeleteFinished(success: Boolean, bixbyPair: BixbyPair, errorMessage: String? = null)
}