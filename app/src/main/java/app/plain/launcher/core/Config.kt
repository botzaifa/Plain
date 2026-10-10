package app.plain.launcher.core

object Config {
    const val TEMP_DURATION_MS = 10L * 60L * 1000L
    const val WARNING_BEFORE_MS = 60L * 1000L
    const val ADMIN_SESSION_MS = 2L * 60L * 1000L
    const val ADMIN_SETTINGS_PASS_MS = 3L * 60L * 1000L

    val LOCKOUT_STAGES_MS = longArrayOf(30_000L, 60_000L, 300_000L, 900_000L, 3_600_000L)
    const val LOCKOUT_ATTEMPTS_BEFORE_FIRST = 5

    const val MIN_LEN_TEMP = 4
    const val MIN_LEN_FULL = 6
    const val MIN_LEN_ADMIN = 6

    const val LOOP_GUARD_WINDOW_MS = 3_000L
    const val LOOP_GUARD_MAX_SENDS = 5
    const val LOOP_GUARD_PAUSE_MS = 10_000L

    const val PBKDF2_ITER_MIN = 10_000
    const val PBKDF2_ITER_MAX = 300_000
    const val PBKDF2_TARGET_MS = 120L
    const val PBKDF2_DK_LEN = 32
    const val SALT_BYTES = 16

    const val RECOVERY_CODE_LENGTH = 12
    const val PACKAGE_ABSENCE_DELAY_MS = 2_000L
}
