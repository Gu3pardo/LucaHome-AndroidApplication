package guepardoapps.lucahome.common.crypto

interface IEncrypter {
    fun encrypt(key: String?, value: String?): Pair<Boolean, String>
}