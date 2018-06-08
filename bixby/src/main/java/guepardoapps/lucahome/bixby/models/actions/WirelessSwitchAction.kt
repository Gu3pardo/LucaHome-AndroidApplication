package guepardoapps.lucahome.bixby.models.actions

import android.support.annotation.NonNull
import guepardoapps.lucahome.bixby.enums.StateType
import guepardoapps.lucahome.bixby.models.shared.IBixbyEntity
import guepardoapps.lucahome.common.utils.Logger

class WirelessSwitchAction(
        @NonNull var wirelessSwitchName: String = "") : IBixbyEntity {
    private val tag = WirelessSwitchAction::class.java.simpleName

    override fun getDatabaseString(): String {
        return wirelessSwitchName
    }

    override fun getInformationString(): String {
        return "$tag: $wirelessSwitchName"
    }

    @Throws(NoSuchMethodException::class)
    override fun parseFromDb(databaseString: String) {
        throw NoSuchMethodException("This method is not available!")
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\"\"WirelessSwitchName\":\"$wirelessSwitchName\"}"
    }
}