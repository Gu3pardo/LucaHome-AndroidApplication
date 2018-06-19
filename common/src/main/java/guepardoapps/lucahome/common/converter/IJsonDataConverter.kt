package guepardoapps.lucahome.common.converter

interface IJsonDataConverter<T> {
    fun getList(jsonResponse: String): ArrayList<T>
}