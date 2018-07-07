package guepardoapps.lucahome.common.services.position

import android.annotation.SuppressLint
import guepardoapps.lucahome.common.models.position.Position

class PositionService private constructor() : IPositionService {
    private val tag: String = PositionService::class.java.simpleName

    init {
    }

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance: PositionService = PositionService()
    }

    companion object {
        val instance: PositionService by lazy { Holder.instance }
    }

    override var currentPosition: Position
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun updatePosition() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}