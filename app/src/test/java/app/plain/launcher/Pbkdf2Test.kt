package app.plain.launcher

import app.plain.launcher.auth.Pbkdf2
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class Pbkdf2Test {
    @Test
    fun rfc7914_sha256_passwd_salt_oneIteration() {
        val password = "passwd".toByteArray(Charsets.UTF_8)
        val salt = "salt".toByteArray(Charsets.UTF_8)
        val dk = Pbkdf2.derive(password, salt, 1, 32)
        val expected =
            hex(
                "55ac046e56e3089fec1691c22544b605" +
                    "f94185216dde0465e68b9d57c20dacbc",
            )
        assertArrayEquals(expected, dk)
    }

    private fun hex(s: String): ByteArray =
        ByteArray(s.length / 2) { i -> s.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
}
