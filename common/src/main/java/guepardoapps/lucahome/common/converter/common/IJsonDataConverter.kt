package guepardoapps.lucahome.common.converter.common

interface IJsonDataConverter<T> {
    fun parseStringToList(jsonResponse: String): ArrayList<T>
}