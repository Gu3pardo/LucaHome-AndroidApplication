package guepardoapps.lucahome.bixby.models.shared

import java.io.Serializable

interface IBixbyEntity : Serializable {
    fun getDatabaseString(): String
    fun parseFromDb(databaseString: String)
    fun getInformationString(): String
}
