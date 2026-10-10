package app.plain.launcher.fakes

import app.plain.launcher.core.KeyValueStore

class InMemoryKeyValueStore : KeyValueStore {
    private val data = mutableMapOf<String, Any?>()

    override fun getString(key: String, default: String?): String? = data[key] as? String ?: default

    override fun putString(key: String, value: String?) {
        if (value == null) data.remove(key) else data[key] = value
    }

    override fun getLong(key: String, default: Long): Long = data[key] as? Long ?: default

    override fun putLong(key: String, value: Long) {
        data[key] = value
    }

    override fun getInt(key: String, default: Int): Int = data[key] as? Int ?: default

    override fun putInt(key: String, value: Int) {
        data[key] = value
    }

    override fun getBoolean(key: String, default: Boolean): Boolean = data[key] as? Boolean ?: default

    override fun putBoolean(key: String, value: Boolean) {
        data[key] = value
    }

    override fun remove(key: String) {
        data.remove(key)
    }
}
