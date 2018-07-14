package guepardoapps.lucahome.common.crypto

internal interface IEncrypter {
    fun encrypt(key: String?, value: String?): Pair<Boolean, String>
}