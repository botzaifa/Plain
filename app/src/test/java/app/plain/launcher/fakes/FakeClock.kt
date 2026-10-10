package app.plain.launcher.fakes

import app.plain.launcher.core.Clock

class FakeClock(
    var elapsed: Long = 0L,
    var boot: Int? = 1,
) : Clock {
    override fun elapsedRealtime(): Long = elapsed

    override fun bootCount(): Int? = boot
}
