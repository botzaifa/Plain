package app.plain.launcher.auth

import app.plain.launcher.core.Config
import java.security.SecureRandom

data class StoredHash(
    val iterations: Int,
    val salt: ByteArray,
    val hash: ByteArray,
) {
    fun serialize(): String {
        val saltHex = salt.toHex()
        val hashHex = hash.toHex()
        return "pbkdf2-sha256$$iterations$$saltHex$$hashHex"
    }

    companion object {
        fun parse(raw: String?): StoredHash? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.split('$')
            if (parts.size != 4 || parts[0] != "pbkdf2-sha256") return null
            val iter = parts[1].toIntOrNull() ?: return null
            val salt = parts[2].hexToBytes() ?: return null
            val hash = parts[3].hexToBytes() ?: return null
            return StoredHash(iter, salt, hash)
        }
    }
}

object PasswordHasher {
    private val random = SecureRandom()

    fun hash(password: CharArray, iterations: Int): StoredHash {
        val salt = ByteArray(Config.SALT_BYTES).also { random.nextBytes(it) }
        val derived = derive(password, salt, iterations)
        return StoredHash(iterations, salt, derived)
    }

    fun verify(password: CharArray, stored: StoredHash): Boolean {
        val derived = derive(password, stored.salt, stored.iterations)
        return constantTimeEquals(derived, stored.hash)
    }

    fun derive(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val bytes = password.concatToString().toByteArray(Charsets.UTF_8)
        return Pbkdf2.derive(bytes, salt, iterations, Config.PBKDF2_DK_LEN)
    }

    fun calibrateIterations(trialPassword: CharArray = charArrayOf('t', 'e', 's', 't')): Int {
        val salt = ByteArray(Config.SALT_BYTES) { 1 }
        var low = Config.PBKDF2_ITER_MIN
        var high = Config.PBKDF2_ITER_MAX
        var best = low
        while (low <= high) {
            val mid = (low + high) / 2
            val start = System.nanoTime()
            derive(trialPassword, salt, mid)
            val ms = (System.nanoTime() - start) / 1_000_000L
            if (ms <= Config.PBKDF2_TARGET_MS) {
                best = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return best.coerceIn(Config.PBKDF2_ITER_MIN, Config.PBKDF2_ITER_MAX)
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }
}

private fun ByteArray.toHex(): String {
    val sb = StringBuilder(size * 2)
    for (b in this) {
        sb.append(String.format("%02x", b.toInt() and 0xff))
    }
    return sb.toString()
}

private fun String.hexToBytes(): ByteArray? {
    if (length % 2 != 0) return null
    return try {
        ByteArray(length / 2) { i ->
            substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    } catch (_: NumberFormatException) {
        null
    }
}
