package app.plain.launcher.settings

import app.plain.launcher.core.Config
import app.plain.launcher.core.Clock
import app.plain.launcher.core.KeyValueStore

class AdminSession(
    private val store: KeyValueStore,
    private val clock: Clock,
) {
    fun start(now: Long = clock.elapsedRealtime()) {
        store.putLong(KEY_UNTIL, now + Config.ADMIN_SESSION_MS)
    }

    fun clear() {
        store.putLong(KEY_UNTIL, 0L)
    }

    fun isActive(now: Long = clock.elapsedRealtime()): Boolean {
        return now < store.getLong(KEY_UNTIL, 0L)
    }

    fun grantSystemSettingsPass(now: Long = clock.elapsedRealtime()) {
        store.putLong(KEY_SETTINGS_PASS, now + Config.ADMIN_SETTINGS_PASS_MS)
    }

    fun isSystemSettingsPassActive(now: Long = clock.elapsedRealtime()): Boolean {
        return now < store.getLong(KEY_SETTINGS_PASS, 0L)
    }

    fun clearSystemSettingsPass() {
        store.putLong(KEY_SETTINGS_PASS, 0L)
    }

    companion object {
        private const val KEY_UNTIL = "admin_session_until"
        private const val KEY_SETTINGS_PASS = "admin_settings_pass_until"
    }
}
