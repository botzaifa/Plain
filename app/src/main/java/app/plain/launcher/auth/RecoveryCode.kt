package app.plain.launcher.auth

import app.plain.launcher.core.Config
import java.security.SecureRandom

object RecoveryCode {
    private const val ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"

    fun generate(random: SecureRandom = SecureRandom()): String {
        val chars = CharArray(Config.RECOVERY_CODE_LENGTH) { i ->
            ALPHABET[random.nextInt(ALPHABET.length)]
        }
        return String(chars)
    }

    fun format(code: String): String {
        val c = canonicalize(code)
        if (c.length != Config.RECOVERY_CODE_LENGTH) return code
        return "${c.substring(0, 4)}-${c.substring(4, 8)}-${c.substring(8, 12)}"
    }

    fun canonicalize(input: String): String {
        return buildString {
            for (ch in input.uppercase()) {
                if (ch == '-' || ch == ' ') continue
                append(ch)
            }
        }
    }
}
