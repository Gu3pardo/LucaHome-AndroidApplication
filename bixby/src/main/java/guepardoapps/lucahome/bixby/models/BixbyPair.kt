package guepardoapps.lucahome.bixby.models

import guepardoapps.lucahome.bixby.enums.DatabaseAction
import guepardoapps.lucahome.bixby.models.actions.BixbyAction
import guepardoapps.lucahome.bixby.models.requirements.BixbyRequirement

import java.io.Serializable

import kotlin.collections.ArrayList

class BixbyPair : Serializable {
    private val tag: String = BixbyPair::class.java.simpleName

    var actionId: Int = -1
    lateinit var action: BixbyAction
    lateinit var requirementList: List<BixbyRequirement>
    lateinit var databaseAction: DatabaseAction

    override fun toString(): String {
        val requirementString = StringBuilder()
        requirementList.forEach { x -> requirementString.append(x.toString()) }
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"ActionId\":$actionId," +
                "\"Action\":\"$action\"," +
                "\"Requirements\":\"$requirementString\"," +
                "\"DatabaseAction\":\"$databaseAction\"" +
                "}"
    }
}
