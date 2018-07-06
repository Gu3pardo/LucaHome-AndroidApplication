package guepardoapps.lucahome.common.controller

import android.content.Context

interface ITtsController {
    var enabled: Boolean
    var context: Context?

    fun speak(text: String)
    fun dispose()
}