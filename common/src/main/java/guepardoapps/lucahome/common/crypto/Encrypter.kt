package guepardoapps.lucahome.common.crypto

internal class Encrypter : IEncrypter {
    override fun encrypt(key: String?, value: String?): Pair<Boolean, String> {
        if (key.isNullOrEmpty()) {
            return Pair(false, "Key is null or empty")
        }

        if (value.isNullOrEmpty()) {
            return Pair(false, "Value is null or empty")
        }

        // TODO Implement Encryption
        return Pair(true, value!!)
    }
}