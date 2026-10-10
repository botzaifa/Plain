package app.plain.launcher

import app.plain.launcher.auth.UnlockState
import app.plain.launcher.auth.UnlockStateMachine
import app.plain.launcher.fakes.FakeClock
import app.plain.launcher.fakes.InMemoryKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnlockStateMachineTest {
    @Test
    fun tempExpiresAndRebootClearsTemp() {
        val clock = FakeClock(1000L, boot = 5)
        val store = InMemoryKeyValueStore()
        val sm = UnlockStateMachine(store, clock) { 60_000L }
        sm.unlockTemp(1000L)
        assertEquals(UnlockState.TEMP_UNLOCKED, sm.effectiveState(2000L))
        clock.elapsed = 70_000L
        assertEquals(UnlockState.LOCKED, sm.effectiveState(clock.elapsed))
        sm.unlockTemp(70_000L)
        clock.boot = 6
        assertEquals(UnlockState.LOCKED, sm.effectiveState(71_000L))
    }

    @Test
    fun fullSurvivesReboot() {
        val clock = FakeClock(1000L, boot = 1)
        val store = InMemoryKeyValueStore()
        val sm = UnlockStateMachine(store, clock) { 60_000L }
        sm.unlockFull()
        clock.boot = 2
        assertEquals(UnlockState.FULL_UNLOCKED, sm.effectiveState(5000L))
    }

    @Test
    fun shouldWarnOneMinuteBefore() {
        val clock = FakeClock(0L)
        val store = InMemoryKeyValueStore()
        val sm = UnlockStateMachine(store, clock) { 120_000L }
        sm.unlockTemp(0L)
        clock.elapsed = 70_000L
        assertTrue(sm.shouldWarn(clock.elapsed, 60_000L))
    }
}
