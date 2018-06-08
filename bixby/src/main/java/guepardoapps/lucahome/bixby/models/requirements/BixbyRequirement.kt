package guepardoapps.lucahome.bixby.models.requirements

import guepardoapps.lucahome.bixby.enums.RequirementType
import guepardoapps.lucahome.bixby.models.shared.*

class BixbyRequirement(
        val id: Int,
        val actionId: Int,
        val requirementType: RequirementType = RequirementType.Null,
        val puckJsPosition: String = "",
        val lightRequirement: LightRequirement = LightRequirement(),
        val networkRequirement: NetworkEntity = NetworkEntity(),
        val wirelessSocketRequirement: WirelessSocketEntity = WirelessSocketEntity()) : IBixbyEntity {
    private val tag = BixbyRequirement::class.java.simpleName

    @Throws(NoSuchMethodException::class)
    override fun getDatabaseString(): String {
        throw NoSuchMethodException("This method is not available!")
    }

    @Throws(NoSuchMethodException::class)
    override fun parseFromDb(databaseString: String) {
        throw NoSuchMethodException("This method is not available!")
    }

    override fun getInformationString(): String {
        return when (requirementType) {
            RequirementType.Light -> lightRequirement.getInformationString()
            RequirementType.Network -> networkRequirement.getInformationString()
            RequirementType.Position -> "Position: $puckJsPosition"
            RequirementType.WirelessSocket -> wirelessSocketRequirement.getInformationString()
            RequirementType.Null -> "No information available!"
        }
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\",\"Id\":$id,\"ActionId\":$actionId,\"RequirementType\":\"$requirementType\",\"PuckJsPosition\":\"$puckJsPosition\",\"LightRequirement\":\"$lightRequirement\",\"NetworkRequirement\":\"$networkRequirement\",\"WirelessSocketRequirement\":\"$wirelessSocketRequirement\"}"
    }
}
