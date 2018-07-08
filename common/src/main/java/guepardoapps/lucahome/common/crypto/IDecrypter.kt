package guepardoapps.lucahome.common.crypto

interface IDecrypter {
    fun decrypt(key: String?, value: String?): Pair<Boolean, String>
}