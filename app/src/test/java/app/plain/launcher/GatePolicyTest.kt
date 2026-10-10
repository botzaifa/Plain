package app.plain.launcher

import app.plain.launcher.fakes.FakeClock
import app.plain.launcher.gate.GatePolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GatePolicyTest {
    @Test
    fun waitSeconds_escalatesAndCaps() {
        assertEquals(15, GatePolicy.waitSeconds(0))
        assertEquals(25, GatePolicy.waitSeconds(1))
        assertEquals(90, GatePolicy.waitSeconds(10))
    }

    @Test
    fun todayKey_usesClockZone() {
        val clock = FakeClock(Instant.parse("2026-10-09T20:00:00Z"), ZoneId.of("America/New_York"))
        assertEquals(LocalDate.of(2026, 10, 9).toString(), GatePolicy.todayKey(clock))
    }
}
