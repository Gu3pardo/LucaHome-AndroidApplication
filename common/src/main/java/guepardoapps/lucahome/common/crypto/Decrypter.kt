package guepardoapps.lucahome.common.crypto

internal class Decrypter : IDecrypter {
    override fun decrypt(key: String?, value: String?): Pair<Boolean, String> {
        if (key.isNullOrEmpty()) {
            return Pair(false, "Key is null or empty")
        }

        if (value.isNullOrEmpty()) {
            return Pair(false, "Value is null or empty")
        }

        // TODO Implement Decryption
        return Pair(true, value!!)
    }
}