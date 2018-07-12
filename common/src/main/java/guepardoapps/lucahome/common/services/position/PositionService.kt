package guepardoapps.lucahome.common.services.position

import android.annotation.SuppressLint
import guepardoapps.lucahome.common.models.common.RxOptional
import guepardoapps.lucahome.common.models.position.Position
import io.reactivex.subjects.PublishSubject

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

    override val currentPositionPublishSubject: PublishSubject<RxOptional<Position>> = PublishSubject.create<RxOptional<Position>>()!!

    override fun updatePosition() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}