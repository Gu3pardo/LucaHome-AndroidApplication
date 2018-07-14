package guepardoapps.lucahome.common.crypto

internal interface IDecrypter {
    fun decrypt(key: String?, value: String?): Pair<Boolean, String>
}