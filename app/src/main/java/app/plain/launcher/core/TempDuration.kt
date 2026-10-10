package app.plain.launcher.core

import app.plain.launcher.BuildConfig
import app.plain.launcher.core.KeyValueStore

object TempDuration {
    private const val DEBUG_OVERRIDE_KEY = "debug_temp_duration_ms"

    fun currentMs(store: KeyValueStore): Long {
        if (BuildConfig.DEBUG) {
            val override = store.getLong(DEBUG_OVERRIDE_KEY, -1L)
            if (override > 0L) return override
        }
        return Config.TEMP_DURATION_MS
    }

    fun setDebugOverrideMs(store: KeyValueStore, ms: Long) {
        if (BuildConfig.DEBUG) {
            store.putLong(DEBUG_OVERRIDE_KEY, ms)
        }
    }
}
