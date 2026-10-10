package app.plain.launcher.core

import android.content.Context
import android.content.SharedPreferences

interface KeyValueStore {
    fun getString(key: String, default: String?): String?
    fun putString(key: String, value: String?)
    fun getLong(key: String, default: Long): Long
    fun putLong(key: String, value: Long)
    fun getInt(key: String, default: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
}

class SharedPreferencesStore(context: Context, name: String) : KeyValueStore {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun getString(key: String, default: String?): String? = prefs.getString(key, default)

    override fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)

    override fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
