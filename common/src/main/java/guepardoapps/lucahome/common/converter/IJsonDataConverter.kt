package guepardoapps.lucahome.common.converter

interface IJsonDataConverter<T> {
    fun parseStringToList(jsonResponse: String): ArrayList<T>
}