package guepardoapps.lucahome.bixby.models.actions

import guepardoapps.lucahome.bixby.models.shared.IBixbyEntity

class ApplicationAction : IBixbyEntity {
    private val tag = ApplicationAction::class.java.simpleName

    lateinit var packageName: String

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
        return "{" +
                "\"Class\":\"$tag\"," +
                "\"PackageName\":\"$packageName\"" +
                "}"
    }
}