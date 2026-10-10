package app.plain.launcher

import app.plain.launcher.apps.SlotDecision
import app.plain.launcher.apps.SlotDecisions
import org.junit.Assert.assertEquals
import org.junit.Test

class SlotDecisionTest {
    @Test
    fun resolvesSingleCandidate() {
        val d = SlotDecisions.decide(listOf("com.app"), null)
        assertEquals(SlotDecision.Resolved("com.app"), d)
    }

    @Test
    fun ambiguousWhenMultiple() {
        val d = SlotDecisions.decide(listOf("a", "b"), null)
        assertEquals(SlotDecision.Ambiguous, d)
    }
}
