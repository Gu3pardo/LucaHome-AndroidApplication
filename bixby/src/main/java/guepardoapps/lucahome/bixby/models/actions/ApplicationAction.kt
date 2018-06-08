package guepardoapps.lucahome.bixby.models.actions

import android.support.annotation.NonNull
import guepardoapps.lucahome.bixby.enums.StateType
import guepardoapps.lucahome.bixby.models.shared.IBixbyEntity
import guepardoapps.lucahome.common.utils.Logger

class ApplicationAction(
        @NonNull var packageName: String = "") : IBixbyEntity {
    private val tag = ApplicationAction::class.java.simpleName

    override fun getDatabaseString(): String {
        return packageName
    }

    override fun getInformationString(): String {
        return "$tag: $packageName"
    }

    @Throws(NoSuchMethodException::class)
    override fun parseFromDb(databaseString: String) {
        throw NoSuchMethodException("This method is not available!")
    }

    override fun toString(): String {
        return "{\"Class\":\"$tag\"\"PackageName\":\"$packageName\"}"
    }
}