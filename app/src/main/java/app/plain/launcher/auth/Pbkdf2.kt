package app.plain.launcher.auth

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * PBKDF2-HMAC-SHA256 (RFC 8018) for API 24+ without PBKDF2WithHmacSHA256 provider.
 */
object Pbkdf2 {
    fun derive(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        dkLen: Int,
    ): ByteArray {
        require(iterations > 0)
        require(dkLen > 0)
        val hLen = 32
        val l = (dkLen + hLen - 1) / hLen
        val dk = ByteArray(dkLen)
        var offset = 0
        for (block in 1..l) {
            val t = f(password, salt, iterations, block)
            val copyLen = minOf(hLen, dkLen - offset)
            System.arraycopy(t, 0, dk, offset, copyLen)
            offset += copyLen
        }
        return dk
    }

    private fun f(password: ByteArray, salt: ByteArray, iterations: Int, blockIndex: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(password, "HmacSHA256"))
        val block = salt + int32Be(blockIndex)
        var u = mac.doFinal(block)
        val result = u.copyOf()
        var i = 1
        while (i < iterations) {
            u = mac.doFinal(u)
            xorInPlace(result, u)
            i++
        }
        return result
    }

    private fun int32Be(i: Int): ByteArray =
        byteArrayOf(
            (i ushr 24).toByte(),
            (i ushr 16).toByte(),
            (i ushr 8).toByte(),
            i.toByte(),
        )

    private fun xorInPlace(a: ByteArray, b: ByteArray) {
        for (i in a.indices) {
            a[i] = (a[i].toInt() xor b[i].toInt()).toByte()
        }
    }
}
