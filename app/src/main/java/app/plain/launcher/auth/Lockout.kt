package app.plain.launcher.auth

import app.plain.launcher.core.Clock
import app.plain.launcher.core.Config
import app.plain.launcher.core.KeyValueStore

class Lockout(
    private val store: KeyValueStore,
    private val clock: Clock,
) {
    enum class Result {
        OK,
        LOCKED,
    }

    data class Status(val locked: Boolean, val remainingMs: Long)

    fun status(now: Long = clock.elapsedRealtime()): Status {
        val until = store.getLong(KEY_UNTIL, 0L)
        val bootAtLock = store.getInt(KEY_BOOT, -1)
        val currentBoot = clock.bootCount()
        val effectiveUntil = if (bootAtLock >= 0 && currentBoot != null && currentBoot != bootAtLock) {
            val stage = store.getInt(KEY_STAGE, 0)
            now + stageDurationMs(stage)
        } else {
            until
        }
        if (effectiveUntil <= now) return Status(false, 0L)
        return Status(true, effectiveUntil - now)
    }

    fun onWrongAttempt(now: Long = clock.elapsedRealtime()): Status {
        val current = status(now)
        if (current.locked) return current
        var fails = store.getInt(KEY_FAILS, 0) + 1
        store.putInt(KEY_FAILS, fails)
        if (fails < Config.LOCKOUT_ATTEMPTS_BEFORE_FIRST) {
            return Status(false, 0L)
        }
        var stage = store.getInt(KEY_STAGE, 0)
        if (fails > Config.LOCKOUT_ATTEMPTS_BEFORE_FIRST) {
            stage = (stage + 1).coerceAtMost(Config.LOCKOUT_STAGES_MS.lastIndex)
            store.putInt(KEY_STAGE, stage)
        }
        val duration = stageDurationMs(stage)
        store.putLong(KEY_UNTIL, now + duration)
        clock.bootCount()?.let { store.putInt(KEY_BOOT, it) }
        return Status(true, duration)
    }

    fun onSuccess() {
        store.putInt(KEY_FAILS, 0)
        store.putInt(KEY_STAGE, 0)
        store.putLong(KEY_UNTIL, 0L)
        store.putInt(KEY_BOOT, -1)
    }

    private fun stageDurationMs(stage: Int): Long {
        val idx = stage.coerceIn(0, Config.LOCKOUT_STAGES_MS.lastIndex)
        return Config.LOCKOUT_STAGES_MS[idx]
    }

    companion object {
        private const val KEY_FAILS = "lockout_fails"
        private const val KEY_STAGE = "lockout_stage"
        private const val KEY_UNTIL = "lockout_until_elapsed"
        private const val KEY_BOOT = "lockout_boot_count"
    }
}
