package app.plain.launcher.auth

import app.plain.launcher.core.Config
import app.plain.launcher.core.Clock
import app.plain.launcher.core.KeyValueStore

enum class PasswordLevel {
    TEMP,
    FULL,
    ADMIN,
    NONE,
}

class AuthRepository(
    private val store: KeyValueStore,
    private val clock: Clock,
    private val tempDurationMs: () -> Long,
) {
    val lockout = Lockout(store, clock)
    val stateMachine = UnlockStateMachine(store, clock, tempDurationMs)

    fun isSetupComplete(): Boolean = store.getBoolean(KEY_SETUP_DONE, false)

    fun markSetupComplete() {
        store.putBoolean(KEY_SETUP_DONE, true)
    }

    fun setPasswords(temp: CharArray, full: CharArray, admin: CharArray, iterations: Int) {
        store.putString(KEY_HASH_TEMP, PasswordHasher.hash(temp, iterations).serialize())
        store.putString(KEY_HASH_FULL, PasswordHasher.hash(full, iterations).serialize())
        store.putString(KEY_HASH_ADMIN, PasswordHasher.hash(admin, iterations).serialize())
        store.putInt(KEY_ITERATIONS, iterations)
        val allDigits =
            temp.all { it.isDigit() } && full.all { it.isDigit() } && admin.all { it.isDigit() }
        store.putBoolean(KEY_PASSWORDS_NUMERIC, allDigits)
    }

    fun setAdminPassword(admin: CharArray) {
        val iterations = store.getInt(KEY_ITERATIONS, Config.PBKDF2_ITER_MIN)
        store.putString(KEY_HASH_ADMIN, PasswordHasher.hash(admin, iterations).serialize())
    }

    fun setRecoveryCode(code: String, iterations: Int? = null) {
        val iter = iterations ?: store.getInt(KEY_ITERATIONS, Config.PBKDF2_ITER_MIN)
        val canonical = RecoveryCode.canonicalize(code)
        store.putString(KEY_HASH_RECOVERY, PasswordHasher.hash(canonical.toCharArray(), iter).serialize())
    }

    fun calibrateAndStoreIterations(): Int {
        val iter = PasswordHasher.calibrateIterations()
        store.putInt(KEY_ITERATIONS, iter)
        return iter
    }

    fun storedIterations(): Int = store.getInt(KEY_ITERATIONS, Config.PBKDF2_ITER_MIN)

    fun verifyPassword(input: CharArray): PasswordLevel {
        val temp = StoredHash.parse(store.getString(KEY_HASH_TEMP, null))
        val full = StoredHash.parse(store.getString(KEY_HASH_FULL, null))
        val admin = StoredHash.parse(store.getString(KEY_HASH_ADMIN, null))
        if (temp != null && PasswordHasher.verify(input, temp)) return PasswordLevel.TEMP
        if (full != null && PasswordHasher.verify(input, full)) return PasswordLevel.FULL
        if (admin != null && PasswordHasher.verify(input, admin)) return PasswordLevel.ADMIN
        return PasswordLevel.NONE
    }

    fun verifyAdminOnly(input: CharArray): Boolean =
        verifyPassword(input) == PasswordLevel.ADMIN

    fun verifyRecoveryCode(input: String): Boolean {
        val stored = StoredHash.parse(store.getString(KEY_HASH_RECOVERY, null)) ?: return false
        val canonical = RecoveryCode.canonicalize(input)
        return PasswordHasher.verify(canonical.toCharArray(), stored)
    }

    fun allPasswordsDigitsOnly(): Boolean = store.getBoolean(KEY_PASSWORDS_NUMERIC, false)

    fun validateDistinct(temp: CharArray, full: CharArray, admin: CharArray): Boolean {
        val t = temp.concatToString()
        val f = full.concatToString()
        val a = admin.concatToString()
        return t != f && t != a && f != a
    }

    fun validateLengths(temp: CharArray, full: CharArray, admin: CharArray): Boolean {
        return temp.size >= Config.MIN_LEN_TEMP &&
            full.size >= Config.MIN_LEN_FULL &&
            admin.size >= Config.MIN_LEN_ADMIN
    }

    companion object {
        private const val KEY_SETUP_DONE = "setup_done"
        private const val KEY_HASH_TEMP = "hash_temp"
        private const val KEY_HASH_FULL = "hash_full"
        private const val KEY_HASH_ADMIN = "hash_admin"
        private const val KEY_HASH_RECOVERY = "hash_recovery"
        private const val KEY_ITERATIONS = "pbkdf2_iterations"
        private const val KEY_PASSWORDS_NUMERIC = "passwords_numeric"
    }
}
