package app.plain.launcher.auth

import app.plain.launcher.core.Clock
import app.plain.launcher.core.KeyValueStore

enum class UnlockState {
    LOCKED,
    TEMP_UNLOCKED,
    FULL_UNLOCKED,
}

class UnlockStateMachine(
    private val store: KeyValueStore,
    private val clock: Clock,
    private val tempDurationMs: () -> Long,
) {
    fun effectiveState(now: Long = clock.elapsedRealtime()): UnlockState {
        val raw = store.getString(KEY_STATE, UnlockState.LOCKED.name) ?: UnlockState.LOCKED.name
        val base = runCatching { UnlockState.valueOf(raw) }.getOrDefault(UnlockState.LOCKED)
        return when (base) {
            UnlockState.TEMP_UNLOCKED -> if (isTempExpired(now)) UnlockState.LOCKED else base
            else -> base
        }
    }

    fun unlockTemp(now: Long = clock.elapsedRealtime()) {
        store.putString(KEY_STATE, UnlockState.TEMP_UNLOCKED.name)
        store.putLong(KEY_TEMP_EXPIRY, now + tempDurationMs())
        clock.bootCount()?.let { store.putInt(KEY_TEMP_BOOT, it) }
            ?: store.putLong(KEY_TEMP_START, now)
    }

    fun unlockFull() {
        store.putString(KEY_STATE, UnlockState.FULL_UNLOCKED.name)
        clearTemp()
    }

    fun lock() {
        store.putString(KEY_STATE, UnlockState.LOCKED.name)
        clearTemp()
    }

    fun remainingMs(now: Long = clock.elapsedRealtime()): Long {
        if (effectiveState(now) != UnlockState.TEMP_UNLOCKED) return 0L
        val expiry = store.getLong(KEY_TEMP_EXPIRY, 0L)
        return (expiry - now).coerceAtLeast(0L)
    }

    fun shouldWarn(now: Long = clock.elapsedRealtime(), warningBeforeMs: Long): Boolean {
        val rem = remainingMs(now)
        return rem in 1..warningBeforeMs
    }

    fun expireIfNeeded(now: Long = clock.elapsedRealtime()): Boolean {
        val raw = store.getString(KEY_STATE, UnlockState.LOCKED.name)
        if (raw != UnlockState.TEMP_UNLOCKED.name) return false
        if (!isTempExpired(now)) return false
        lock()
        return true
    }

    private fun isTempExpired(now: Long): Boolean {
        val storedBoot = store.getInt(KEY_TEMP_BOOT, -1)
        val currentBoot = clock.bootCount()
        if (storedBoot >= 0 && currentBoot != null && currentBoot != storedBoot) {
            return true
        }
        val start = store.getLong(KEY_TEMP_START, -1L)
        if (storedBoot < 0 && start >= 0 && now < start) {
            return true
        }
        return now >= store.getLong(KEY_TEMP_EXPIRY, 0L)
    }

    private fun clearTemp() {
        store.putLong(KEY_TEMP_EXPIRY, 0L)
        store.putInt(KEY_TEMP_BOOT, -1)
        store.putLong(KEY_TEMP_START, -1L)
    }

    companion object {
        private const val KEY_STATE = "unlock_state"
        private const val KEY_TEMP_EXPIRY = "temp_expiry_elapsed"
        private const val KEY_TEMP_BOOT = "temp_boot_count"
        private const val KEY_TEMP_START = "temp_start_elapsed"
    }
}
