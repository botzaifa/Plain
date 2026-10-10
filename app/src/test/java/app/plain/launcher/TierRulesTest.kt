package app.plain.launcher

import app.plain.launcher.apps.Slot
import app.plain.launcher.apps.Tier
import app.plain.launcher.data.TierRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TierRulesTest {
    @Test
    fun protectedPackageCannotBeGated() {
        val map = mapOf(Slot.Phone to "com.phone")
        assertFalse(TierRules.canAssignTier("com.phone", Tier.GATED, map))
    }

    @Test
    fun leavingGatedRequiresAuthorization() {
        assertTrue(TierRules.requiresApplyAuthorization(Tier.GATED, Tier.TOOLS))
        assertFalse(TierRules.requiresApplyAuthorization(Tier.TOOLS, Tier.GATED))
    }
}
