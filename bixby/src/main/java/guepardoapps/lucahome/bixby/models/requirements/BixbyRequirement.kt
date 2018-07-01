package guepardoapps.lucahome.bixby.models.requirements

import guepardoapps.lucahome.bixby.enums.RequirementType
import guepardoapps.lucahome.bixby.models.shared.*

class BixbyRequirement : IBixbyEntity {
    private val tag = BixbyRequirement::class.java.simpleName

    var id: Int = -1
    var actionId: Int = -1
    lateinit var requirementType: RequirementType
    lateinit var positionRequirement: PositionRequirement
    lateinit var lightRequirement: LightRequirement
    lateinit var networkRequirement: NetworkEntity
    lateinit var wirelessSocketRequirement: WirelessSocketEntity

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
            RequirementType.Position -> positionRequirement.getInformationString()
            RequirementType.WirelessSocket -> wirelessSocketRequirement.getInformationString()
            RequirementType.Null -> "No information available!"
        }
    }

    override fun toString(): String {
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"Id\":$id," +
                "\"ActionId\":$actionId," +
                "\"RequirementType\":\"$requirementType\"," +
                "\"PositionRequirement\":\"$positionRequirement\"," +
                "\"LightRequirement\":\"$lightRequirement\"," +
                "\"NetworkRequirement\":\"$networkRequirement\"," +
                "\"WirelessSocketRequirement\":\"$wirelessSocketRequirement\"" +
                "}"
    }
}
