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

    override fun loadBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    override fun loadFloat(key: String): Float {
        return sharedPreferences.getFloat(key, 0.0f)
    }

    override fun loadInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    override fun loadLong(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }

    override fun loadString(key: String): String {
        return sharedPreferences.getString(key, "")
    }

    override fun removeAll(): Boolean {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear()
        return editor.commit()
    }
}