package guepardoapps.lucahome.bixby.models.actions

import guepardoapps.lucahome.bixby.enums.ActionType
import guepardoapps.lucahome.bixby.models.shared.*

class BixbyAction(
        val id: Int,
        val actionId: Int,
        val actionType: ActionType = ActionType.Null,
        val applicationAction: ApplicationAction = ApplicationAction(),
        val networkAction: NetworkEntity = NetworkEntity(),
        val wirelessSocketAction: WirelessSocketEntity = WirelessSocketEntity(),
        val wirelessSwitchAction: WirelessSwitchAction = WirelessSwitchAction()) : IBixbyEntity {
    private val tag = BixbyAction::class.java.simpleName

    @Throws(NoSuchMethodException::class)
    override fun getDatabaseString(): String {
        throw NoSuchMethodException("This method is not available!")
    }

    @Throws(NoSuchMethodException::class)
    override fun parseFromDb(databaseString: String) {
        throw NoSuchMethodException("This method is not available!")
    }

    override fun getInformationString(): String {
        return when (actionType) {
            ActionType.Application -> applicationAction.getInformationString()
            ActionType.Network -> networkAction.getInformationString()
            ActionType.WirelessSocket -> wirelessSocketAction.getInformationString()
            ActionType.WirelessSwitch -> wirelessSwitchAction.getInformationString()
            ActionType.Null -> "No information available!"
        }
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"Id\":$id,\"ActionId\":$actionId,\"ActionType\":\"$actionType\",\"ApplicationAction\":\"$applicationAction\",\"NetworkAction\":\"$networkAction\",\"WirelessSocketAction\":\"$wirelessSocketAction\",\"WirelessSwitchAction\":\"$wirelessSwitchAction\"}"
    }
}
