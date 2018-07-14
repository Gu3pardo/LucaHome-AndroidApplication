package guepardoapps.lucahome.common.controller

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import guepardoapps.lucahome.common.utils.Logger

class SharedPreferenceController(context: Context) : ISharedPreferenceController {
    private val tag: String = SharedPreferenceController::class.java.simpleName

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    override fun <T> save(key: String, value: T): Boolean {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        when (value) {
            Boolean::class.java -> editor.putBoolean(key, value as Boolean)
            Float::class.java -> editor.putFloat(key, value as Float)
            Int::class.java -> editor.putInt(key, value as Int)
            Long::class.java -> editor.putLong(key, value as Long)
            String::class.java -> editor.putString(key, value as String)
            else -> {
                Logger.instance.error(tag, "Invalid generic type of $value")
                return false
            }
        }

        return editor.commit()
    }

    override fun <T> load(key: String, defaultValue: T): Any {
        return when (defaultValue) {
            Boolean::class.java -> sharedPreferences.getBoolean(key, defaultValue as Boolean)
            Float::class.java -> sharedPreferences.getFloat(key, defaultValue as Float)
            Int::class.java -> sharedPreferences.getInt(key, defaultValue as Int)
            Long::class.java -> sharedPreferences.getLong(key, defaultValue as Long)
            String::class.java -> sharedPreferences.getString(key, defaultValue as String)
            else -> {
                Logger.instance.error(tag, "Invalid generic type of $defaultValue")
                return {}
            }
        }
    }

    override fun removeAll(): Boolean {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear()
        return editor.commit()
    }
}