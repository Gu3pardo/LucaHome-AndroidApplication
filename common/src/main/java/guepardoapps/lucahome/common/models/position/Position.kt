package guepardoapps.lucahome.common.models.position

import java.util.*

data class Position(
        var roomUuid: UUID,
        var lightValue: Double,
        var dateTime: Calendar)