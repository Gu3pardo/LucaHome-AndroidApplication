package guepardoapps.lucahome.common.controller

import android.content.Context
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import guepardoapps.lucahome.common.utils.Logger

class SharedPreferenceController(context: Context, fileName: String, key: String) : ISharedPreferenceController {
    private val tag: String = SharedPreferenceController::class.java.simpleName

    private val cryptoPrefs: CryptoPrefs = CryptoPrefs(context, fileName, key)

    override fun <T> save(key: String, value: T) {
        when (value) {
            Boolean::class.java -> cryptoPrefs.put(key, value as Boolean)
            Byte::class.java -> cryptoPrefs.put(key, value as Byte)
            Double::class.java -> cryptoPrefs.put(key, value as Double)
            Float::class.java -> cryptoPrefs.put(key, value as Float)
            Int::class.java -> cryptoPrefs.put(key, value as Int)
            Long::class.java -> cryptoPrefs.put(key, value as Long)
            Short::class.java -> cryptoPrefs.put(key, value as Short)
            String::class.java -> cryptoPrefs.put(key, value as String)
            else -> {
                Logger.instance.error(tag, "Invalid generic type of $value")
            }
        }
    }

    override fun <T> load(key: String, defaultValue: T): Any {
        return when (defaultValue) {
            Boolean::class.java -> cryptoPrefs.getBoolean(key, defaultValue as Boolean)
            Byte::class.java -> cryptoPrefs.getByte(key, defaultValue as Byte)
            Double::class.java -> cryptoPrefs.getDouble(key, defaultValue as Double)
            Float::class.java -> cryptoPrefs.getFloat(key, defaultValue as Float)
            Int::class.java -> cryptoPrefs.getInt(key, defaultValue as Int)
            Long::class.java -> cryptoPrefs.getLong(key, defaultValue as Long)
            Short::class.java -> cryptoPrefs.getShort(key, defaultValue as Short)
            String::class.java -> cryptoPrefs.getString(key, defaultValue as String)
            else -> {
                Logger.instance.error(tag, "Invalid generic type of $defaultValue")
                return {}
            }
        }
    }

    override fun remove(key: String) {
        cryptoPrefs.remove(key)
    }

    override fun erase() {
        cryptoPrefs.erase()
    }
}