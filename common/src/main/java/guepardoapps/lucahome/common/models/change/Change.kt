package guepardoapps.lucahome.common.models.change

import guepardoapps.lucahome.common.annotations.JsonKey
import java.util.*

@JsonKey("Data", "Change")
data class Change(
        @JsonKey("", "Uuid")
        var uuid: UUID = UUID.randomUUID(),
        @JsonKey( "", "Type")
        var type: String = "",
        @JsonKey("", "UserName")
        var userName: String = "",
        @JsonKey("", "Time")
        var time: Calendar = Calendar.getInstance())