package guepardoapps.lucahome.bixby.models

import android.support.annotation.NonNull

import guepardoapps.lucahome.bixby.classes.actions.BixbyAction
import guepardoapps.lucahome.bixby.enums.DatabaseAction
import guepardoapps.lucahome.bixby.models.requirements.BixbyRequirement

import java.io.Serializable

import kotlin.collections.ArrayList

class BixbyPair(var actionId: Int,
                @NonNull var action: BixbyAction,
                @NonNull var requirementList: ArrayList<BixbyRequirement>,
                @NonNull var databaseAction: DatabaseAction) : Serializable {
    private val tag: String = BixbyPair::class.java.simpleName

    override fun toString(): String {
        val requirementString = StringBuilder()

        for (index in 0 until requirementList.size step 1) {
            requirementString.append(requirementList[index].toString())
        }

        return "{\"Class\":\"$tag\",\"ActionId\":$actionId,\"Action\":\"$action\",\"Requirements\":\"$requirementString\",\"DatabaseAction\":\"$databaseAction\"}"
    }
}
