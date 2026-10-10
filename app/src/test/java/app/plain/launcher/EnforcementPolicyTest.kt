package app.plain.launcher

import app.plain.launcher.auth.UnlockState
import app.plain.launcher.policy.BuiltInAllowlist
import app.plain.launcher.policy.EnforcementAction
import app.plain.launcher.policy.EnforcementPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class EnforcementPolicyTest {
    private val launchable = setOf("com.gated.app", "com.basic.app", "com.clock", "com.dialer", "com.recents")
    private val basic = setOf("com.basic.app")
    private val always = setOf("app.plain.launcher", "com.dialer", "com.clock", "com.recents")

    @Test
    fun alarmClockAllowedWhenNotBasic() {
        val action =
            EnforcementPolicy.decide(
                "com.clock",
                UnlockState.LOCKED,
                launchable,
                basic,
                always,
                false,
                true,
                false,
            )
        assertEquals(EnforcementAction.ALLOW, action)
    }

    @Test
    fun categoryHomeAllowed() {
        val action =
            EnforcementPolicy.decide(
                "com.recents",
                UnlockState.LOCKED,
                launchable,
                basic,
                always,
                false,
                true,
                false,
            )
        assertEquals(EnforcementAction.ALLOW, action)
    }

    @Test
    fun gatedSentHomeWhenLocked() {
        val action =
            EnforcementPolicy.decide(
                "com.gated.app",
                UnlockState.LOCKED,
                launchable,
                basic,
                always,
                false,
                true,
                false,
            )
        assertEquals(EnforcementAction.SEND_HOME, action)
    }

    @Test
    fun nonLaunchableSentHomeWhenLocked() {
        val action =
            EnforcementPolicy.decide(
                "com.vendor.camera",
                UnlockState.LOCKED,
                launchable = emptySet(),
                basic,
                always,
                false,
                true,
                false,
            )
        assertEquals(EnforcementAction.SEND_HOME, action)
    }

    @Test
    fun builtInAllowlistHonored() {
        val pkg = BuiltInAllowlist.packages.firstOrNull() ?: return
        val action =
            EnforcementPolicy.decide(
                pkg,
                UnlockState.LOCKED,
                launchable + pkg,
                basic,
                always + pkg,
                false,
                true,
                false,
            )
        assertEquals(EnforcementAction.ALLOW, action)
    }
}
