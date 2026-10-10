package app.plain.launcher

import app.plain.launcher.auth.RecoveryCode
import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryCodeTest {
    @Test
    fun canonicalizeStripsDashesAndSpaces() {
        assertEquals("ABCD1234EFGH", RecoveryCode.canonicalize("abcd-1234 efgh"))
    }

    @Test
    fun formatGroupsTwelveChars() {
        assertEquals("ABCD-1234-EFGH", RecoveryCode.format("ABCD1234EFGH"))
    }
}
