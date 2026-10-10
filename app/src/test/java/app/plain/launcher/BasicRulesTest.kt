package app.plain.launcher

import app.plain.launcher.policy.BasicRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BasicRulesTest {
    @Test
    fun dialerAlwaysPresent() {
        val result = BasicRules.ensureDialerPresent(listOf("a"), "dialer")
        assertEquals(listOf("dialer", "a"), result)
    }

    @Test
    fun dialerCannotBeRemoved() {
        assertFalse(BasicRules.canRemoveFromBasic("dialer", "dialer"))
    }
}
