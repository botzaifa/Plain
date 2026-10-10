package app.plain.launcher

import app.plain.launcher.policy.LoopGuard
import app.plain.launcher.policy.LoopGuardDecision
import org.junit.Assert.assertEquals
import org.junit.Test

class LoopGuardTest {
    @Test
    fun pausesAfterFiveQuickSends() {
        val guard = LoopGuard()
        var decision = LoopGuardDecision.ALLOW_SEND
        for (i in 0 until 5) {
            decision = guard.onSendHome(i * 100L)
        }
        assertEquals(LoopGuardDecision.ALLOW_SEND, decision)
        decision = guard.onSendHome(500L)
        assertEquals(LoopGuardDecision.PAUSED, decision)
    }

    @Test
    fun spacedSendsDoNotPause() {
        val guard = LoopGuard()
        for (i in 0 until 6) {
            guard.onSendHome(i * 1000L)
        }
        assertEquals(LoopGuardDecision.ALLOW_SEND, guard.onSendHome(6000L))
    }
}
