package app.plain.launcher.fakes

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class FakeClock(
    private var instant: Instant,
    private val zone: ZoneId = ZoneId.of("UTC"),
) : Clock() {
    override fun getZone(): ZoneId = zone
    override fun withZone(zone: ZoneId?): Clock = FakeClock(instant, zone ?: this.zone)
    override fun instant(): Instant = instant

    fun advanceSeconds(seconds: Long) {
        instant = instant.plusSeconds(seconds)
    }

    fun setInstant(newInstant: Instant) {
        instant = newInstant
    }
}
