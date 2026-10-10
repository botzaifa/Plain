package app.plain.launcher

import app.plain.launcher.auth.Lockout
import app.plain.launcher.fakes.FakeClock
import app.plain.launcher.fakes.InMemoryKeyValueStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockoutTest {
    @Test
    fun locksAfterFiveAttempts() {
        val clock = FakeClock(1000L)
        val store = InMemoryKeyValueStore()
        val lockout = Lockout(store, clock)
        repeat(4) {
            val s = lockout.onWrongAttempt()
            assertFalse(s.locked)
        }
        val fifth = lockout.onWrongAttempt()
        assertTrue(fifth.locked)
        assertTrue(fifth.remainingMs >= 29_000L)
    }
}
