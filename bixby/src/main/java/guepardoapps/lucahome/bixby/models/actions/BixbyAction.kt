package guepardoapps.lucahome.bixby.models.actions

import guepardoapps.lucahome.bixby.enums.ActionType
import guepardoapps.lucahome.bixby.models.shared.*

class BixbyAction : IBixbyEntity {
    private val tag = BixbyAction::class.java.simpleName

    var id: Int = -1
    var actionId: Int = -1
    lateinit var actionType: ActionType
    lateinit var applicationAction: ApplicationAction
    lateinit var networkAction: NetworkEntity
    lateinit var wirelessSocketAction: WirelessSocketEntity
    lateinit var wirelessSwitchAction: WirelessSwitchAction

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
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"Id\":$id," +
                "\"ActionId\":$actionId," +
                "\"ActionType\":\"$actionType\"," +
                "\"ApplicationAction\":\"$applicationAction\"," +
                "\"NetworkAction\":\"$networkAction\"," +
                "\"WirelessSocketAction\":\"$wirelessSocketAction\"," +
                "\"WirelessSwitchAction\":\"$wirelessSwitchAction\"" +
                "}"
    }
}
